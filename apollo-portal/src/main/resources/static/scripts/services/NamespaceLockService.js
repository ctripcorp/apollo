appService.service('NamespaceLockService', ['$resource', '$q', function ($resource, $q) {
    var resource = $resource('', {}, {
        get_namespace_lock: {
            method: 'GET',
            url: 'apps/:appId/envs/:env/clusters/:clusterName/namespaces/:namespaceName/lock-info'
        }
    });

    return {
        get_namespace_lock: function (appId, env, clusterName, namespaceName) {
            var d = $q.defer();
            resource.get_namespace_lock({
                                            appId: appId,
                                            env: env,
                                            clusterName: clusterName,
                                            namespaceName: namespaceName
                                        }, function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
            return d.promise;
        }
    }
}]);
