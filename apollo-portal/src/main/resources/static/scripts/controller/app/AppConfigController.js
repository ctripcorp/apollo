application_module.controller("AppConfigController",
    ['$scope', '$rootScope', '$state', '$location', 'toastr',
        'AppService', 'ConfigService', 'VersionService',
        function ($scope, $rootScope, $state, $location, toastr, AppService, ConfigService, VersionService) {

            var configLocation = {
                appId: $rootScope.appId,
                env: 'uat',
                versionId: -1
            };

            $rootScope.breadcrumb.nav = '配置';
            $rootScope.breadcrumb.env = configLocation.env;

            $scope.configLocation = configLocation;

            /**env*/
            $scope.envs = ['dev', 'fws', 'fat', 'uat', 'lpt', 'prod', 'tools'];

            $scope.switchEnv = function (selectedEnv) {
                configLocation.env = selectedEnv;
                $rootScope.breadcrumb.env = configLocation.env;
                refreshConfigs();

            };

            /**version*/
            $scope.releaseVersions = [];
            $scope.currentVersionIsRelease = false;

            VersionService.load(configLocation.appId, configLocation.env).then(function (result) {
                $scope.releaseVersions = result;
            }, function (result) {
                toastr.error("获取版本失败", result);
            });

            $scope.switchVersion = function (versionId) {
                configLocation.versionId = versionId;
                $scope.currentVersionIsRelease = configLocation.versionId > 0;
                refreshConfigs();
            };

            /**config*/
            refreshConfigs();
            //refresh app config infomations
            function refreshConfigs() {
                ConfigService.load(configLocation.appId, configLocation.env, configLocation.versionId).then(function (result) {
                    $scope.config = result;

                    $scope.showClusterConfigs = false;
                    if (result.overrideClusterConfigs && result.overrideClusterConfigs[0]) {
                        $scope.showClusterConfigs = true;

                        //default selected
                        $scope.config.selectedCluster = result.overrideClusterConfigs[0];
                        $scope.config.selectedClusterKVs = result.overrideClusterConfigs[0].configs;

                        //build map clusterName -> configs for switch cluster
                        $scope.config.overrideClusters = {};
                        $.each(result.overrideClusterConfigs, function (index, value) {
                            $scope.config.overrideClusters[value.clusterName] = value.configs;
                        });
                    }
                }, function (result) {
                    toastr.error("加载配置出错", result);
                });
            }

            //switch cluster
            $scope.selectCluster = function () {
                $scope.config.selectedClusterKVs = $scope.config.overrideClusters[$scope.config.selectedCluster.clusterName];
            };

        }]);
