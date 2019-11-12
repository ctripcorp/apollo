appService.service('SystemInfoService', ['$resource', '$q', 'AppUtil', function ($resource, $q, AppUtil) {
    var system_info_resource = $resource('', {}, {
        load_system_info: {
            method: 'GET',
            url: AppUtil.prefixPath() + '/system-info'
        },
        check_health: {
            method: 'GET',
            url: AppUtil.prefixPath() + '/system-info/health'
        }
    });
    return {
        load_system_info: function () {
            var d = $q.defer();
            system_info_resource.load_system_info({},
            function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
            return d.promise;
        },
        check_health: function (instanceId, host) {
            var d = $q.defer();
            system_info_resource.check_health({
                  instanceId: instanceId
            },
            function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
            return d.promise;
        }
    }
}]);
