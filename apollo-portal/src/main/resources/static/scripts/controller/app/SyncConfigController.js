sync_item_module.controller("SyncItemController",
                              ['$scope', '$location', 'toastr', 'AppService', 'AppUtil', 'ConfigService',
                               function ($scope, $location, toastr, AppService, AppUtil, ConfigService) {

                                   var params = AppUtil.parseParams($location.$$url);
                                   var currentUser = 'test_user';
                                   $scope.pageContext = {
                                       appId: params.appid,
                                       env: params.env,
                                       clusterName: params.clusterName,
                                       namespaceName: params.namespaceName
                                   };
                                   
                                   ////// load env //////
                                   AppService.load_nav_tree($scope.pageContext.appId).then(function (result) {
                                       $scope.clusters = result.nodes;
                                       $scope.clusters = [];
                                       result.nodes.forEach(function (node) {
                                           var env = node.env;
                                           node.clusters.forEach(function (cluster) {
                                               cluster.env = env;
                                               cluster.checked = false;
                                               $scope.clusters.push(cluster);
                                           }) 
                                       });
                                   }, function (result) {
                                       toastr.error(AppUtil.errorMsg(result), "加载环境出错");    
                                   });

                                   var envAllSelected = false;
                                   $scope.toggleEnvsCheckedStatus = function () {
                                       envAllSelected = !envAllSelected;
                                       $scope.clusters.forEach(function (cluster) {
                                           cluster.checked = envAllSelected;
                                       })
                                   };
                                   
                                   ////// load items //////
                                   ConfigService.find_items($scope.pageContext.appId, $scope.pageContext.env,
                                                            $scope.pageContext.clusterName, $scope.pageContext.namespaceName).then(function (result) {

                                       $scope.sourceItems = result;
                                       $scope.sourceItems.forEach(function (item) {
                                           item.checked = false;
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

                                   ////// flow control ///////

                                   $scope.syncItemStep = 1;
                                   $scope.syncItemNextStep = function (offset) {
                                       $scope.syncItemStep += offset;
                                   };

                                   $scope.syncItems = function () {
                                       $scope.syncItemStep += 1;
                                   };

                                   $scope.destorySync = function () {
                                       $scope.syncItemStep = 1;
                                   }


                               }]);

