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

    function check(instanceId, host) {
        SystemInfoService.check_health(instanceId, host).then(function (result) {
            var status = result.status.code;
            if (status === 'UP') {
                toastr.success(host + ' is healthy!');
            } else {
                toastr.error(host + ' is not healthy, please check ' + host + '/health for more information!');
            }
        }, function (result) {
            AppUtil.showErrorMsg(result);
        });
    }
}
