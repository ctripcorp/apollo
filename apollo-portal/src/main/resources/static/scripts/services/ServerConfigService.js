appService.service('ServerConfigService', ['$resource', '$q', function ($resource, $q) {
    var server_config_resource = $resource('', {}, {
        create_server_config: {
            method: 'POST',
            url: '/server/config'
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
        }
    }
}]);
