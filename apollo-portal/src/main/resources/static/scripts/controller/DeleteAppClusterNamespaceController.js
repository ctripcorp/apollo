delete_app_cluster_namespace_module.controller('DeleteAppClusterNamespaceController',
                              ['$scope', 'toastr', 'AppUtil', 'AppService', 'ClusterService', 'NamespaceService', 'PermissionService',
                               DeleteAppClusterNamespaceController]);

function DeleteAppClusterNamespaceController($scope, toastr, AppUtil, AppService, ClusterService, NamespaceService, PermissionService) {

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
            toastr.warning("请输入appId");
            return;
        }

        $scope.app.info = "";

        AppService.load($scope.app.appId).then(function (result) {
            if (!result.appId) {
                toastr.warning("AppId: " + $scope.app.appId + " 不存在！");
                $scope.deleteAppBtnDisabled = true;
                return;
            }

            $scope.app.info = "应用名：" + result.name + " 部门：" + result.orgName + '(' + result.orgId + ')' + " 负责人：" + result.ownerName;

            $scope.deleteAppBtnDisabled = false;
        }, function (result) {
           AppUtil.showErrorMsg(result);
        });
    }

    function deleteApp() {
       if (!$scope.app.appId) {
          toastr.warning("请输入appId");
          return;
       }
       if (confirm("确认删除AppId: " + $scope.app.appId + "？")) {
          AppService.delete_app($scope.app.appId).then(function (result) {
              toastr.success("删除成功");
              $scope.deleteAppBtnDisabled = true;
          }, function (result) {
              AppUtil.showErrorMsg(result);
          })
       }
    }

    function getClusterInfo() {
        if (!$scope.cluster.appId || !$scope.cluster.env || !$scope.cluster.name) {
            toastr.warning("请输入appId、环境和集群名称");
            return;
        }

        $scope.cluster.info = "";

        ClusterService.load_cluster($scope.cluster.appId, $scope.cluster.env, $scope.cluster.name).then(function (result) {
            $scope.cluster.info = "AppId：" + result.appId+ " 环境：" + $scope.cluster.env + " 集群名称：" + result.name;

            $scope.deleteClusterBtnDisabled = false;
        }, function (result) {
           AppUtil.showErrorMsg(result);
        });
    }

    function deleteCluster() {
        if (!$scope.cluster.appId || !$scope.cluster.env || !$scope.cluster.name) {
            toastr.warning("请输入appId、环境和集群名称");
            return;
        }
       if (confirm("确认删除集群？appId: " + $scope.cluster.appId + " 环境：" + $scope.cluster.env + " 集群名称：" + $scope.cluster.name)) {
          ClusterService.delete_cluster($scope.cluster.appId, $scope.cluster.env, $scope.cluster.name).then(function (result) {
              toastr.success("删除成功");
              $scope.deleteClusterBtnDisabled = true;
          }, function (result) {
              AppUtil.showErrorMsg(result);
          })
       }
    }

    function getAppNamespaceInfo() {
        if (!$scope.appNamespace.appId || !$scope.appNamespace.name) {
            toastr.warning("请输入appId和AppNamespace名称");
            return;
        }

        $scope.appNamespace.info = "";

        NamespaceService.loadAppNamespace($scope.appNamespace.appId, $scope.appNamespace.name).then(function (result) {
            $scope.appNamespace.info = "AppId：" + result.appId+ " AppNamespace名称：" + result.name + " isPublic：" + result.isPublic;

            $scope.deleteAppNamespaceBtnDisabled = false;
        }, function (result) {
           AppUtil.showErrorMsg(result);
        });
    }

    function deleteAppNamespace() {
        if (!$scope.appNamespace.appId || !$scope.appNamespace.name) {
            toastr.warning("请输入appId和AppNamespace名称");
            return;
        }
       if (confirm("确认删除所有环境的AppNamespace和Namespace？appId: " + $scope.appNamespace.appId + " 环境：所有环境" + " AppNamespace名称：" + $scope.appNamespace.name)) {
          NamespaceService.deleteAppNamespace($scope.appNamespace.appId, $scope.appNamespace.name).then(function (result) {
              toastr.success("删除成功");
              $scope.deleteAppNamespaceBtnDisabled = true;
          }, function (result) {
              AppUtil.showErrorMsg(result);
          })
       }
    }
}
