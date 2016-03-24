appService.service("VersionService", ['$resource', '$q', function ($resource, $q) {
    var config_source = $resource("/version/:appId/:env", {}, {
        load_config: {
            method: 'GET',
            isArray: true
        }
    });

    return {
        load: function (appId, env) {
            var d = $q.defer();
            config_source.load_config({
                appId: appId,
                env: env
            }, function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
            return d.promise;
        }
    }

}]);
