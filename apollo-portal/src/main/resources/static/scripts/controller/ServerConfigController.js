server_config_module.controller('ServerConfigController',
                                ['$scope', '$window', 'toastr', 'ServerConfigService', 'AppUtil',
                                 function ($scope, $window, toastr, ServerConfigService, AppUtil) {

                                     $scope.serverConfig = {};
                                     $scope.saveBtnDisabled = true;

                                     $scope.create = function () {
                                         ServerConfigService.create($scope.serverConfig).then(function (result) {
                                             toastr.success("保存成功");
                                             $scope.saveBtnDisabled = true;
                                             $scope.serverConfig = result;
                                         }, function (result) {
                                             toastr.error(AppUtil.errorMsg(result), "保存失败");
                                         });
                                     };

                                     $scope.getServerConfigInfo = function () {
                                         if (!$scope.serverConfig.key) {
                                            toastr.warning("请输入key");
                                            return;
                                         }

                                         ServerConfigService.getServerConfigInfo($scope.serverConfig.key).then(function (result) {
                                            $scope.saveBtnDisabled = false;

                                            if (!result.key) {
                                                toastr.info("Key: " + $scope.serverConfig.key + " 不存在，点击保存后会创建该配置项");
                                                return;
                                            }

                                            toastr.info("Key: " + $scope.serverConfig.key + " 已存在，点击保存后会覆盖该配置项");
                                            $scope.serverConfig = result;
                                         }, function (result) {
                                            AppUtil.showErrorMsg(result);
                                         })
                                     }

                                 }]);
