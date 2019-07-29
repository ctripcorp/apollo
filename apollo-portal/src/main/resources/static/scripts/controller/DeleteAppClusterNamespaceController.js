delete_app_cluster_namespace_module.controller('DeleteAppClusterNamespaceController',
    ['$scope', '$translate', 'toastr', 'AppUtil', 'AppService', 'ClusterService', 'NamespaceService', 'PermissionService',
        DeleteAppClusterNamespaceController]);

function DeleteAppClusterNamespaceController($scope, $translate, toastr, AppUtil, AppService, ClusterService, NamespaceService, PermissionService) {

    $scope.app = {};
    $scope.deleteAppBtnDisabled = true;
    $scope.getAppInfo = getAppInfo;
    $scope.deleteApp = deleteApp;

    $scope.cluster = {};
    $scope.deleteClusterBtnDisabled = true;
    $scope.getClusterInfo = getClusterInfo;
    $scope.deleteCluster = deleteCluster;

    $scope.appNamespace = {};
    $scope.deleteAppNamespaceBtnDisabled = true;
    $scope.getAppNamespaceInfo = getAppNamespaceInfo;
    $scope.deleteAppNamespace = deleteAppNamespace;

    initPermission();

    function initPermission() {
        PermissionService.has_root_permission()
            .then(function (result) {
                $scope.isRootUser = result.hasPermission;
            })
    }

    function getAppInfo() {
        if (!$scope.app.appId) {
            toastr.warning($translate.instant('Delete.PleaseEnterAppId'));
            return;
        }

        $scope.app.info = "";

        AppService.load($scope.app.appId).then(function (result) {
            if (!result.appId) {
                toastr.warning($translate.instant('Delete.AppIdNotFound', { appId: $scope.app.appId }));
                $scope.deleteAppBtnDisabled = true;
                return;
            }

            $scope.app.info = $translate.instant('Delete.AppInfoContent', {
                appName: result.name,
                departmentName: result.orgName,
                departmentId: result.orgId,
                ownerName: result.ownerName
            });


            $scope.deleteAppBtnDisabled = false;
        }, function (result) {
            AppUtil.showErrorMsg(result);
        });
    }

    function deleteApp() {
        if (!$scope.app.appId) {
            toastr.warning($translate.instant('Delete.PleaseEnterAppId'));
            return;
        }
        if (confirm($translate.instant('Delete.PleaseEnterAppId', { appId: $scope.app.appId }))) {
            AppService.delete_app($scope.app.appId).then(function (result) {
                toastr.success($translate.instant('Delete.Deleted'));
                $scope.deleteAppBtnDisabled = true;
            }, function (result) {
                AppUtil.showErrorMsg(result);
            })
        }
    }

    function getClusterInfo() {
        if (!$scope.cluster.appId || !$scope.cluster.env || !$scope.cluster.name) {
            toastr.warning($translate.instant('Delete.PleaseEnterAppIdAndEnvAndCluster'));
            return;
        }

        $scope.cluster.info = "";

        ClusterService.load_cluster($scope.cluster.appId, $scope.cluster.env, $scope.cluster.name).then(function (result) {
            $scope.cluster.info = $translate.instant('Delete.ClusterInfoContent', {
                appId: result.appId,
                env: $scope.cluster.env,
                clusterName: result.name
            });

            $scope.deleteClusterBtnDisabled = false;
        }, function (result) {
            AppUtil.showErrorMsg(result);
        });
    }

    function deleteCluster() {
        if (!$scope.cluster.appId || !$scope.cluster.env || !$scope.cluster.name) {
            toastr.warning($translate.instant('Delete.PleaseEnterAppIdAndEnvAndCluster'));
            return;
        }
        var confirmTip = $translate.instant('Delete.ConfirmDeleteCluster', {
            appId: result.appId,
            env: $scope.cluster.env,
            clusterName: result.name
        });

        if (confirm(confirmTip)) {
            ClusterService.delete_cluster($scope.cluster.appId, $scope.cluster.env, $scope.cluster.name).then(function (result) {
                toastr.success($translate.instant('Delete.Deleted'));
                $scope.deleteClusterBtnDisabled = true;
            }, function (result) {
                AppUtil.showErrorMsg(result);
            })
        }
    }

    function getAppNamespaceInfo() {
        if (!$scope.appNamespace.appId || !$scope.appNamespace.name) {
            toastr.warning($translate.instant('Delete.PleaseEnterAppIdAndNamespace'));
            return;
        }

        $scope.appNamespace.info = "";

        NamespaceService.loadAppNamespace($scope.appNamespace.appId, $scope.appNamespace.name).then(function (result) {
            $scope.appNamespace.info = $translate.instant('Delete.AppNamespaceInfoContent', {
                appId: result.appId,
                namespace: result.name,
                isPublic: result.isPublic
            });

            $scope.deleteAppNamespaceBtnDisabled = false;
        }, function (result) {
            AppUtil.showErrorMsg(result);
        });
    }

    function deleteAppNamespace() {
        if (!$scope.appNamespace.appId || !$scope.appNamespace.name) {
            toastr.warning($translate.instant('Delete.PleaseEnterAppIdAndNamespace'));
            return;
        }
        var confirmTip = $translate.instant('Delete.ConfirmDeleteNamespace', {
            appId: $scope.appNamespace.appId,
            namespace: $scope.appNamespace.name
        });
        if (confirm(confirmTip)) {
            NamespaceService.deleteAppNamespace($scope.appNamespace.appId, $scope.appNamespace.name).then(function (result) {
                toastr.success($translate.instant('Delete.Deleted'));
                $scope.deleteAppNamespaceBtnDisabled = true;
            }, function (result) {
                AppUtil.showErrorMsg(result);
            })
        }
    }
}
