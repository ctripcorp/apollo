appService.service('ServerConfigService', ['$resource', '$q', function ($resource, $q) {
    var server_config_resource = $resource('', {}, {
        create_server_config: {
            method: 'POST',
            url: '/server/config'
        },
        get_server_config_info: {
            method: 'GET',
            url: '/server/config/:key'
        }
    });
    return {
        create: function (serverConfig) {
            var d = $q.defer();
            server_config_resource.create_server_config({}, serverConfig, function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
            return d.promise;
        },
        getServerConfigInfo: function (key) {
            var d = $q.defer();
            server_config_resource.get_server_config_info({
                key: key
            }, function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
            return d.promise;
        }
    }
}]);
