application_module.controller("AppConfigController",
                              ['$scope', '$location', 'toastr', 'AppService', 'ConfigService',
                               function ($scope, $location, toastr, AppService, ConfigService) {

                                   var appId = $location.$$url.split("=")[1];
                                   var currentUser = 'lepdou';
                                   var pageContext = {
                                       appId: appId,
                                       env: 'LOCAL',
                                       clusterName: 'default'
                                   };

                                   $scope.pageContext = pageContext;

                                   ///////////// load cluster nav tree /////////

                                   AppService.load_nav_tree($scope.pageContext.appId).then(function (result) {
                                       var navTree = [];
                                       var nodes = result.nodes;
                                       nodes.forEach(function (item) {
                                           var node = {};
                                           //first nav
                                           node.text = item.env;
                                           node.selectable = false;
                                           //second nav
                                           var clusterNodes = [];
                                           item.clusters.forEach(function (item) {
                                               var clusterNode = {},
                                                   parentNode = [];

                                               clusterNode.text = item.name;
                                               parentNode.push(node.text);
                                               clusterNode.tags = parentNode;
                                               clusterNodes.push(clusterNode);
                                           });

                                           node.nodes = clusterNodes;
                                           navTree.push(node);
                                       });
                                       $('#treeview').treeview({
                                                                   color: "#428bca",
                                                                   showBorder: true,
                                                                   data: navTree,
                                                                   levels: 99,
                                                                   onNodeSelected: function (event, data) {
                                                                       $scope.pageContext.env = data.tags[0];
                                                                       $scope.pageContext.clusterName = data.text;
                                                                       refreshNamespaces();
                                                                   }
                                                               });
                                   }, function (result) {
                                       toastr.error("加载导航出错:" + result);
                                   });

                                   /////////// namespace ////////////

                                   var namespace_view_type = {
                                       TEXT:'text',
                                       TABLE: 'table',
                                       LOG: 'log'
                                   };

                                   refreshNamespaces();

                                   function refreshNamespaces(viewType) {
                                       ConfigService.load_all_namespaces($scope.pageContext.appId, $scope.pageContext.env,
                                                                         $scope.pageContext.clusterName, viewType).then(
                                           function (result) {
                                               $scope.namespaces = result;

                                               //初始化视图
                                               if ($scope.namespaces) {
                                                   $scope.namespaces.forEach(function (item) {
                                                       item.isModify = false;
                                                       if (!viewType){//default text view
                                                           $scope.switchView(item, namespace_view_type.TEXT);
                                                       }else if (viewType == namespace_view_type.TABLE){
                                                           item.viewType = namespace_view_type.TABLE;
                                                       }


                                                       item.isTextEditing = false;
                                                   })
                                               }

                                           }, function (result) {
                                               toastr.error("加载配置信息出错");
                                           });
                                   }

                                   ////////////global view oper /////////////

                                   $scope.switchView = function (namespace, viewType) {

                                       if (namespace_view_type.TEXT == viewType) {
                                           namespace.text = parseModel2Text(namespace);
                                       } else if (namespace_view_type.TABLE == viewType) {

                                       }
                                       namespace.viewType = viewType;
                                   };
                                   
                                   //把表格内容解析成文本
                                   function parseModel2Text(namespace) {
                                       if (!namespace.items) {
                                           return "无配置信息";
                                       }
                                       var result = "";
                                       namespace.items.forEach(function (item) {
                                           if (item.item.key) {
                                               result +=
                                                   item.item.key + " = " + item.item.value + "\n";
                                           } else {
                                               result += item.item.comment + "\n";
                                           }

                                       });

                                       return result;
                                   }

                                   ////////// text view oper /////////

                                   $scope.draft = {};
                                   //保存草稿
                                   $scope.saveDraft = function (namespace) {
                                       $scope.draft = namespace;
                                   };

                                   //更新配置
                                   $scope.commitChange = function () {
                                       ConfigService.modify_items($scope.pageContext.appId, $scope.pageContext.env, $scope.pageContext.clusterName,
                                                                  $scope.draft.namespace.namespaceName, $scope.draft.text,
                                                                  $scope.draft.namespace.id, 'lepdou').then(
                                           function (result) {
                                               toastr.success("更新成功");
                                               //refresh all namespace items
                                               refreshNamespaces();

                                               $scope.draft.backupText = '';//清空备份文本
                                               $scope.toggleTextEditStatus($scope.draft);

                                           }, function (result) {
                                               toastr.error(result.data.msg, "更新失败");

                                           }
                                       );
                                   };

                                   //文本编辑框状态切换
                                   $scope.toggleTextEditStatus = function (namespace) {
                                       namespace.isTextEditing = !namespace.isTextEditing;
                                       if (namespace.isTextEditing){//切换为编辑状态,保存一下原来值
                                           $scope.draft.backupText = namespace.text;
                                       }else {
                                           if ($scope.draft.backupText){//取消编辑,则复原
                                               namespace.text = $scope.draft.backupText;
                                           }
                                       }
                                   };

                                   ////////// table view oper /////////

                                   //查看旧值
                                   $scope.queryOldValue = function (key, oldValue) {
                                       $scope.queryKey = key;
                                       if (oldValue == '') {
                                           $scope.OldValue = key + "是新添加的key";
                                       } else {
                                           $scope.OldValue = oldValue;
                                       }
                                   };
                                   
                                   /////// release ///////
                                   var releaseNamespace = {};
                                   
                                   $scope.prepareReleaseNamespace = function (namespace) {
                                       releaseNamespace = namespace;        
                                   };
                                   $scope.releaseComment = '';
                                   $scope.release = function () {
                                       ConfigService.release($scope.pageContext.appId, $scope.pageContext.env, 
                                                             $scope.pageContext.clusterName,
                                                             releaseNamespace.namespace.namespaceName, currentUser,
                                                             $scope.releaseComment).then(
                                           function (result) {
                                               toastr.success("发布成功");
                                               //refresh all namespace items
                                               refreshNamespaces();

                                           }, function (result) {
                                               toastr.error(result.data.msg, "发布失败");

                                           }
                                       );    
                                   }

                               }]);

