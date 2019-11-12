appService.service('ReleaseHistoryService', ['$resource', '$q', 'AppUtil', function ($resource, $q, AppUtil) {
    var resource = $resource('', {}, {
        find_release_history_by_namespace: {
            method: 'GET',
            url: AppUtil.prefixPath() + '/apps/:appId/envs/:env/clusters/:clusterName/namespaces/:namespaceName/releases/histories',
            isArray: true
        }
    });

    function findReleaseHistoryByNamespace(appId, env, clusterName, namespaceName, page, size) {
        var d = $q.defer();
        resource.find_release_history_by_namespace({
                                                       appId: appId,
                                                       env: env,
                                                       clusterName: clusterName,
                                                       namespaceName: namespaceName,
                                                       page: page,
                                                       size: size
                                                   }, function (result) {
            d.resolve(result);
        }, function (result) {
            d.reject(result);
        });
        return d.promise;
    }

    
    return {
        findReleaseHistoryByNamespace: findReleaseHistoryByNamespace
    }
}]);
