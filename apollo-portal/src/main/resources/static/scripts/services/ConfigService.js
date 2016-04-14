appService.service("ConfigService", ['$resource', '$q', function ($resource, $q) {
    var config_source = $resource("", {}, {
        load_all_groups: {
            method: 'GET',
            isArray: true,
            url: '/apps/:appId/env/:env/clusters/:clusterName/namespaces'
        },
        modify_items: {
            method: 'GET',
            isArray: false,
            url: '/apps/:appId/env/:env/clusters/:clusterName/namespaces/:namespaceName/modify',
            params: {
                configText: '@configText',
                namespaceId: '@namespaceId'
            }
        }
    });

    return {
        load_all_namespaces: function (appId, env, clusterName) {
            var d = $q.defer();
            config_source.load_all_groups({
                                              appId: appId,
                                              env: env,
                                              clusterName: clusterName
                                          }, function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
            return d.promise;
        },
        modify_items: function (appId, env, clusterName, namespaceName, configText, namespaceId) {
            var d = $q.defer();
            config_source.modify_items({
                                           appId: appId,
                                           env: env,
                                           clusterName: clusterName,
                                           namespaceName: namespaceName,
                                           configText: configText,
                                           namespaceId:namespaceId 
                                       }, function (result) {
                d.resolve(result);

            }, function (result) {
                d.reject(result);
            });
            return d.promise;
        }
    }

}]);
