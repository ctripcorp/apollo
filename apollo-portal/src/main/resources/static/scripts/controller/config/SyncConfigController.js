sync_item_module.controller("SyncItemController",
                            ['$scope', '$location', '$window', 'toastr', 'AppService', 'AppUtil', 'ConfigService',
                             function ($scope, $location, $window, toastr, AppService, AppUtil, ConfigService) {

                                 var params = AppUtil.parseParams($location.$$url);
                                 $scope.pageContext = {
                                     appId: params.appid,
                                     env: params.env,
                                     clusterName: params.clusterName,
                                     namespaceName: params.namespaceName
                                 };

                                 $scope.syncBtnDisabled = false;

                                 ////// load items //////
                                 ConfigService.find_items($scope.pageContext.appId, $scope.pageContext.env,
                                                          $scope.pageContext.clusterName,
                                                          $scope.pageContext.namespaceName,
                                                          "lastModifyTime")
                                     .then(function (result) {

                                         $scope.sourceItems = [];
                                         result.forEach(function (item) {
                                             if (item.key) {
                                                 item.checked = false;
                                                 $scope.sourceItems.push(item);
                                             }
                                         })

                                     }, function (result) {
                                         toastr.error(AppUtil.errorMsg(result), "加载配置出错");
                                     });

                                 var itemAllSelected = false;
                                 $scope.toggleItemsCheckedStatus = function () {
                                     itemAllSelected = !itemAllSelected;
                                     $scope.sourceItems.forEach(function (item) {
                                         item.checked = itemAllSelected;
                                     })
                                 };

                                 var syncData = {
                                     syncToNamespaces: [],
                                     syncItems: []
                                 };
                                 $scope.diff = function () {
                                     parseSyncSourceData();
                                     if (syncData.syncItems.length == 0) {
                                         toastr.warning("请选择需要同步的配置");
                                         return;
                                     }
                                     if (syncData.syncToNamespaces.length == 0) {
                                         toastr.warning("请选择集群");
                                         return;
                                     }
                                     $scope.hasDiff = false;
                                     ConfigService.diff($scope.pageContext.namespaceName, syncData).then(
                                         function (result) {

                                             $scope.clusterDiffs = result;

                                             $scope.clusterDiffs.forEach(function (clusterDiff) {
                                                 if (!$scope.hasDiff) {
                                                     $scope.hasDiff =
                                                         clusterDiff.diffs.createItems.length + clusterDiff.diffs.updateItems.length
                                                         > 0;
                                                 }

                                                 if (clusterDiff.diffs.updateItems.length > 0){
                                                     //赋予同步前的值
                                                     ConfigService.find_items(clusterDiff.namespace.appId,
                                                                              clusterDiff.namespace.env,
                                                                              clusterDiff.namespace.clusterName,
                                                                              clusterDiff.namespace.namespaceName)
                                                         .then(function (result) {
                                                             var oldItemMap = {};
                                                             result.forEach(function (item) {
                                                                 oldItemMap[item.key] = item.value;
                                                             });
                                                             clusterDiff.diffs.updateItems.forEach(function (item) {
                                                                item.oldValue = oldItemMap[item.key];    
                                                             })   
                                                         });    
                                                 }
                                                 
                                             });
                                             $scope.syncItemNextStep(1);
                                         }, function (result) {
                                             toastr.error(AppUtil.errorMsg(result));
                                         });
                                 };

                                 $scope.removeItem = function (diff, type, toRemoveItem) {
                                     var syncDataResult = [],
                                         diffSetResult = [],
                                         diffSet;
                                     if (type == 'create') {
                                         diffSet = diff.createItems;
                                     } else {
                                         diffSet = diff.updateItems;
                                     }
                                     diffSet.forEach(function (item) {
                                         if (item.key != toRemoveItem.key) {
                                             diffSetResult.push(item);
                                         }
                                     });
                                     if (type == 'create') {
                                         diff.createItems = diffSetResult;
                                     } else {
                                         diff.updateItems = diffSetResult;
                                     }

                                     syncData.syncItems.forEach(function (item) {
                                         if (item.key != toRemoveItem.key) {
                                             syncDataResult.push(item);
                                         }
                                     });
                                     syncData.syncItems = syncDataResult;
                                 };

                                 $scope.syncItems = function () {
                                     $scope.syncBtnDisabled = true;
                                     ConfigService.sync_items($scope.pageContext.appId,
                                                              $scope.pageContext.namespaceName,
                                                              syncData).then(function (result) {
                                         $scope.syncItemStep += 1;
                                         $scope.syncSuccess = true;
                                         $scope.syncBtnDisabled = false;
                                     }, function (result) {
                                         $scope.syncSuccess = false;
                                         $scope.syncBtnDisabled = false;
                                         toastr.error(AppUtil.errorMsg(result));
                                     });
                                 };

                                 var selectedClusters = [];
                                 $scope.collectSelectedClusters = function (data) {
                                     selectedClusters = data;
                                 };

                                 function parseSyncSourceData() {
                                     syncData = {
                                         syncToNamespaces: [],
                                         syncItems: []
                                     };
                                     var namespaceName = $scope.pageContext.namespaceName;
                                     selectedClusters.forEach(function (cluster) {
                                         if (cluster.checked) {
                                             cluster.clusterName = cluster.name;
                                             cluster.namespaceName = namespaceName;
                                             syncData.syncToNamespaces.push(cluster);
                                         }
                                     });

                                     $scope.sourceItems.forEach(function (item) {
                                         if (item.checked) {
                                             syncData.syncItems.push(item);
                                         }
                                     });
                                     return syncData;
                                 }

                                 ////// flow control ///////

                                 $scope.syncItemStep = 1;
                                 $scope.syncItemNextStep = function (offset) {
                                     $scope.syncItemStep += offset;
                                 };

                                 $scope.backToAppHomePage = function () {
                                     $window.location.href = '/config.html?#appid=' + $scope.pageContext.appId;
                                 };

                                 $scope.switchSelect = function (o) {
                                     o.checked = !o.checked;
                                 };


                             }]);

