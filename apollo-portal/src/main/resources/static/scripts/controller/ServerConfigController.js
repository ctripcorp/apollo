server_config_module.controller('ServerConfigController',
    ['$scope', '$window', '$translate', 'toastr', 'ServerConfigService', 'AppUtil', 'PermissionService',
        function ($scope, $window, $translate, toastr, ServerConfigService, AppUtil, PermissionService) {

            $scope.serverConfig = {};
            $scope.saveBtnDisabled = true;

            initPermission();

            function initPermission() {
                PermissionService.has_root_permission()
                .then(function (result) {
                    $scope.isRootUser = result.hasPermission;
                })
            }

            $scope.create = function () {
                ServerConfigService.create($scope.serverConfig).then(function (result) {
                    toastr.success($translate.instant('ServiceConfig.Saved'));
                    $scope.saveBtnDisabled = true;
                    $scope.serverConfig = result;
                }, function (result) {
                    toastr.error(AppUtil.errorMsg(result), $translate.instant('ServiceConfig.SaveFailed'));
                });
            };

            $scope.getServerConfigInfo = function () {
                if (!$scope.serverConfig.key) {
                    toastr.warning($translate.instant('ServiceConfig.PleaseEnterKey'));
                    return;
                }

                ServerConfigService.getServerConfigInfo($scope.serverConfig.key).then(function (result) {
                    $scope.saveBtnDisabled = false;

                    if (!result.key) {
                        toastr.info($translate.instant('ServiceConfig.KeyNotExistsAndCreateTip', { key: $scope.serverConfig.key }));
                        return;
                    }

                    toastr.info($translate.instant('ServiceConfig.KeyExistsAndSaveTip', { key: $scope.serverConfig.key }));
                    $scope.serverConfig = result;
                }, function (result) {
                    AppUtil.showErrorMsg(result);
                })
            }

        }]);
