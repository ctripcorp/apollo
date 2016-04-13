application_module.controller("AppConfigController",
                              ['$scope', '$location', 'toastr', 'AppService', 'ConfigService',
                               function ($scope, $location, toastr, AppService, ConfigService) {

                                   var appId = $location.$$url.split("=")[1];

                                   var pageContext = {
                                       appId: appId,
                                       env: 'LOCAL',
                                       clusterName: 'default'
                                   };

                                   $scope.pageEnv = pageContext;
                                   /////////////

                                   AppService.load_nav_tree($scope.pageEnv.appId).then(function (result) {
                                       var navTree = [];
                                       var nodes = result.nodes;
                                       nodes.forEach(function (item) {
                                           var node = {};
                                           //first nav
                                           node.text = item.env;

                                           //second nav
                                           var clusterNodes = [];
                                           item.clusters.forEach(function (item) {
                                               var clusterNode = {};
                                               clusterNode.text = item.name;
                                               clusterNodes.push(clusterNode);
                                           });

                                           node.nodes = clusterNodes;
                                           navTree.push(node);
                                       });
                                       $('#treeview').treeview({
                                                                   color: "#428bca",
                                                                   showBorder: true,
                                                                   data: navTree,
                                                                   levels: 99
                                                               });
                                   }, function (result) {
                                       toastr.error("加载导航出错:" + result);
                                   });

                                   ///////////

                                   ConfigService.load_all_namespaces($scope.pageEnv.appId, $scope.pageEnv.env,
                                                                     $scope.pageEnv.clusterName).then(
                                       function (result) {
                                           $scope.namespaces = result;

                                           //初始化视图
                                           if ($scope.namespaces) {
                                               $scope.namespaces.forEach(function (item) {
                                                   item.isModify = false;
                                                   item.viewType = 'table';
                                                   item.isTextEditing = false;
                                               })
                                           }

                                       }, function (result) {
                                           toastr.error("加载配置信息出错");
                                       });

                                   
                                   $scope.draft = {};
                                   //保存草稿
                                   $scope.saveDraft = function (namespace) {
                                       $scope.draft = namespace;
                                   };

                                   //更新配置
                                   $scope.commitChange = function () {
                                       ConfigService.modify_items($scope.pageEnv.appId, $scope.pageEnv.env, $scope.pageEnv.clusterName,
                                                                  $scope.draft.namespace.namespaceName, $scope.draft.text).then(
                                           function (result) {
                                               toastr.success("更新成功");
                                               $scope.draft.backupText = '';//清空备份文本
                                               $scope.toggleTextEditStatus($scope.draft);
                                           }, function (result) {
                                               toastr.error(result.data.msg, "更新失败");

                                           }
                                       );
                                   };

                                   /////////
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
                                   
                                   $scope.queryOldValue = function (key, oldValue) {
                                       $scope.queryKey = key;
                                       if (oldValue == '') {
                                           $scope.OldValue = key + "是新添加的key";
                                       } else {
                                           $scope.OldValue = oldValue;
                                       }
                                   };

                                   $scope.switchView = function (namespace, viewType) {

                                       if ('textarea' == viewType) {
                                           namespace.text = parseTableModel2Text(namespace);
                                       } else if ('table' == viewType) {

                                       }
                                       namespace.viewType = viewType;
                                   };

                                   //把表格内容解析成文本
                                   function parseTableModel2Text(namespace) {
                                       if (!namespace.items) {
                                           return "无配置信息";
                                       }
                                       var result = "";
                                       namespace.items.forEach(function (item) {
                                           // if (item.modified) {
                                           //     result += "**";
                                           // }
                                           if (item.item.key) {
                                               result +=
                                                   item.item.key + " = " + item.item.value + "\n";
                                           } else {
                                               result += item.item.comment + "\n";
                                           }

                                       });

                                       return result;
                                   }

                                   //把文本内容解析成表格

                               }]);

