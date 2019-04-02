system_info_module.controller('SystemInfoController',
                              ['$scope', 'toastr', 'AppUtil', 'AppService', 'ClusterService', 'NamespaceService', 'PermissionService', 'SystemInfoService',
                               SystemInfoController]);

function SystemInfoController($scope, toastr, AppUtil, AppService, ClusterService, NamespaceService, PermissionService, SystemInfoService) {

    $scope.systemInfo = {};
    $scope.check = check;

    initPermission();

    function initPermission() {
        PermissionService.has_root_permission()
            .then(function (result) {
                  $scope.isRootUser = result.hasPermission;

                  if (result.hasPermission) {
                      loadSystemInfo();
                  }
            })
    }

    function loadSystemInfo() {
        SystemInfoService.load_system_info().then(function (result) {
            $scope.systemInfo = result;
        }, function (result) {
            AppUtil.showErrorMsg(result);
        });
    }

    function check(instanceId) {
        SystemInfoService.check_health(instanceId).then(function (result) {
            var status = result.status.code;
            if (status === 'UP') {
                toastr.success(instanceId + ' is healthy!');
            } else {
                toastr.error(instanceId + ' is not healthy, please check /health of ' + instanceId + 'for more information!');
            }
        }, function (result) {
            AppUtil.showErrorMsg(result);
        });
    }
}
