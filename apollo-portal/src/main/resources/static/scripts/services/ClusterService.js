appService.service('ClusterService', ['$resource', '$q', function ($resource, $q) {
    var cluster_resource = $resource('', {}, {
        create_cluster: {
            method: 'POST',
            url: 'apps/:appId/envs/:env/clusters'
        },
        load_cluster: {
            method: 'GET',
            url: 'apps/:appId/envs/:env/clusters/:clusterName'
        },
        delete_cluster: {
            method: 'DELETE',
            url: 'apps/:appId/envs/:env/clusters/:clusterName'
        }
    });
    return {
        create_cluster: function (appId, env, cluster) {
            var d = $q.defer();
            cluster_resource.create_cluster({
                                                appId: appId,
                                                env: env
                                            }, cluster,
                                            function (result) {
                                                d.resolve(result);
                                            }, function (result) {
                    d.reject(result);
                });
            return d.promise;
        },
        load_cluster: function (appId, env, clusterName) {
            var d = $q.defer();
            cluster_resource.load_cluster({
                appId: appId,
                env: env,
                clusterName: clusterName
            },
            function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
            return d.promise;
        },
        delete_cluster: function (appId, env, clusterName) {
            var d = $q.defer();
            cluster_resource.delete_cluster({
                appId: appId,
                env: env,
                clusterName: clusterName
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
