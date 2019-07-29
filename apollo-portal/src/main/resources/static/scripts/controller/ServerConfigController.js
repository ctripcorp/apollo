server_config_module.controller('ServerConfigController',
    ['$scope', '$window', '$translate', 'toastr', 'ServerConfigService', 'AppUtil',
        function ($scope, $window, $translate, toastr, ServerConfigService, AppUtil) {

            $scope.serverConfig = {};
            $scope.saveBtnDisabled = true;

            $scope.create = function () {
                ServerConfigService.create($scope.serverConfig).then(function (result) {
                    toastr.success($translate.instant('SericeConfig.Saved'));
                    $scope.saveBtnDisabled = true;
                    $scope.serverConfig = result;
                }, function (result) {
                    toastr.error(AppUtil.errorMsg(result), $translate.instant('SericeConfig.SaveFailed'));
                });
            };

            $scope.getServerConfigInfo = function () {
                if (!$scope.serverConfig.key) {
                    toastr.warning($translate.instant('SericeConfig.PleaseEnterKey'));
                    return;
                }

                ServerConfigService.getServerConfigInfo($scope.serverConfig.key).then(function (result) {
                    $scope.saveBtnDisabled = false;

                    if (!result.key) {
                        toastr.info($translate.instant('SericeConfig.KeyNotExistsAndCreateTip', { key: $scope.serverConfig.key }));
                        return;
                    }

                    toastr.info($translate.instant('SericeConfig.KeyExistsAndSaveTip', { key: $scope.serverConfig.key }));
                    $scope.serverConfig = result;
                }, function (result) {
                    AppUtil.showErrorMsg(result);
                })
            }

        }]);
