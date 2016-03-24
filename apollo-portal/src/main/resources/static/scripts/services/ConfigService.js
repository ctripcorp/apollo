appService.service("ConfigService", ['$resource', '$q', function ($resource, $q) {
    var config_source = $resource("/configs/:appId", {}, {
        load_config: {
            method: 'GET',
            isArray: false
        }
    });

    return {
        load: function (appId, env, versionId) {
            var d = $q.defer();
            config_source.load_config({
                appId: appId,
                env: env,
                versionId: versionId
            }, function (result) {
                d.resolve(result);
            }, function (result) {
                de.reject(result);
            });
            return d.promise;
        }
    }

}]);
