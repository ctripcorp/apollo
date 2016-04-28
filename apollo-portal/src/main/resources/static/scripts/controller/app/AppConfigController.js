application_module.controller("AppConfigController",
                              ['$scope', '$location', 'toastr', 'AppService', 'AppUtil', 'ConfigService',
                               function ($scope, $location, toastr, AppService, AppUtil, ConfigService) {


                                   var appId = AppUtil.parseParams($location.$$url).appid;
                                   var currentUser = 'test_user';
                                   var pageContext = {
                                       appId: appId,
                                       env: '',
                                       clusterName: 'default'
                                   };

                                   $scope.pageContext = pageContext;

                                   ////// load cluster nav tree //////

                                   AppService.load_nav_tree($scope.pageContext.appId).then(function (result) {
                                       var navTree = [];
                                       var nodes = result.nodes;
                                       nodes.forEach(function (item) {
                                           var node = {};
                                           //first nav
                                           node.text = item.env;
                                           var clusterNodes = [];
                                           //如果env下面只有一个default集群则不显示集群列表
                                           if (item.clusters && item.clusters.length == 1 && item.clusters[0].name == 'default'){
                                               node.selectable = true;
                                           }else {
                                               node.selectable = false;
                                               //second nav
                                               item.clusters.forEach(function (item) {
                                                   var clusterNode = {},
                                                       parentNode = [];

                                                   clusterNode.text = item.name;
                                                   parentNode.push(node.text);
                                                   clusterNode.tags = parentNode;
                                                   clusterNodes.push(clusterNode);
                                               });
                                           }
                                           node.nodes = clusterNodes;
                                           navTree.push(node);
                                       });
                                       $('#treeview').treeview({
                                                                   color: "#428bca",
                                                                   showBorder: true,
                                                                   data: navTree,
                                                                   levels: 99,
                                                                   onNodeSelected: function (event, data) {
                                                                       if (!data.tags){//first nav node
                                                                           $scope.pageContext.env = data.text;
                                                                           $scope.pageContext.clusterName = 'default';
                                                                       }else {//second cluster node
                                                                           $scope.pageContext.env = data.tags[0];
                                                                           $scope.pageContext.clusterName = data.text;
                                                                       }
                                                                       refreshNamespaces();
                                                                   }
                                                               });
                                   }, function (result) {
                                       toastr.error(AppUtil.errorMsg(result), "加载导航出错");
                                   });

                                   ////// app info //////

                                   AppService.load($scope.pageContext.appId).then(function (result) {
                                       $scope.appBaseInfo = result.app;
                                       $scope.missEnvs = result.missEnvs;
                                       $scope.selectedEnvs = angular.copy($scope.missEnvs);
                                   },function (result) {
                                       toastr.error(AppUtil.errorMsg(result), "加载App信息出错");    
                                   });

                                   ////// namespace //////

                                   var namespace_view_type = {
                                       TEXT:'text',
                                       TABLE: 'table',
                                       LOG: 'log'
                                   };

                                   refreshNamespaces();

                                   function refreshNamespaces(viewType) {
                                       if ($scope.pageContext.env == ''){
                                           return;
                                       }
                                       ConfigService.load_all_namespaces($scope.pageContext.appId, $scope.pageContext.env,
                                                                         $scope.pageContext.clusterName, viewType).then(
                                           function (result) {
                                               $scope.namespaces = result;

                                               //初始化视图
                                               if ($scope.namespaces) {
                                                   
                                                   $scope.namespaces.forEach(function (item) {
                                                       item.isModify = false;
                                                       if (!viewType) {//default text view
                                                           $scope.switchView(item, namespace_view_type.TEXT);
                                                       } else if (viewType == namespace_view_type.TABLE) {
                                                           item.viewType = namespace_view_type.TABLE;
                                                       }

                                                       item.isTextEditing = false;
                                                   });
                                               }

                                           }, function (result) {
                                               toastr.error(AppUtil.errorMsg(result), "加载配置信息出错");
                                           });
                                   }

                                   ////// global view oper //////

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
                                       var itemCnt = 0;
                                       namespace.items.forEach(function (item) {
                                           if (item.item.key) {
                                               result +=
                                                   item.item.key + " = " + item.item.value + "\n";
                                           } else {
                                               result += item.item.comment + "\n";
                                           }
                                           itemCnt ++;
                                       });

                                       itemCnt > 30 ? 30 : itemCnt;
                                       itemCnt < 9 ? 8 : itemCnt;
                                       namespace.itemCnt = itemCnt + 3;
                                       return result;
                                   }

                                   ////// text view oper //////

                                   $scope.draft = {};
                                   //保存草稿
                                   $scope.saveDraft = function (namespace) {
                                       $scope.draft = namespace;
                                   };

                                   $scope.commitComment = '';
                                   //更新配置
                                   $scope.commitChange = function () {
                                       ConfigService.modify_items($scope.pageContext.appId, $scope.pageContext.env, $scope.pageContext.clusterName,
                                                                  $scope.draft.namespace.namespaceName, $scope.draft.text,
                                                                  $scope.draft.namespace.id, $scope.commitComment, currentUser).then(
                                           function (result) {
                                               toastr.success("更新成功");
                                               //refresh all namespace items
                                               refreshNamespaces();

                                               $scope.draft.backupText = '';//清空备份文本
                                               $scope.toggleTextEditStatus($scope.draft);

                                           }, function (result) {
                                               toastr.error(AppUtil.errorMsg(result), "更新失败");

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

                                   ////// table view oper //////

                                   $scope.watch = {};
                                   //查看配置
                                   $scope.watchItem = function (key, value, oldValue) {
                                       $scope.watch.key = key;
                                       $scope.watch.value = value;
                                       $scope.watch.oldValue = oldValue;
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
                                               toastr.error(AppUtil.errorMsg(result),  "发布失败");

                                           }
                                       );    
                                   }
                                   
                                   ////// create env //////

                                   $scope.toggleSelection = function toggleSelection(env) {
                                       var idx = $scope.selectedEnvs.indexOf(env);

                                       // is currently selected
                                       if (idx > -1) {
                                           $scope.selectedEnvs.splice(idx, 1);
                                       }

                                       // is newly selected
                                       else {
                                           $scope.selectedEnvs.push(env);
                                       }
                                   };

                                   $scope.createEnvs = function () {
                                       var count = 0;
                                       $scope.selectedEnvs.forEach(function (env) {
                                           AppService.create(env, $scope.appBaseInfo).then(function (result) {
                                               toastr.success(env, '创建成功');
                                               count ++;
                                               if (count == $scope.selectedEnvs){
                                                 $route.reload();
                                               }
                                           }, function (result) {
                                               toastr.error(AppUtil.errorMsg(result), '创建失败:' + env);
                                               count ++;
                                               if (count == $scope.selectedEnvs){
                                                   $route.reload();
                                               }
                                           });
                                       });
                                   };
                                   
                               }]);

