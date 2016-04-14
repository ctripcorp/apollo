appService.service("ConfigService", ['$resource', '$q', function ($resource, $q) {
    var config_source = $resource("", {}, {
        load_all_groups: {
            method: 'GET',
            isArray: true,
            url: '/apps/:appId/env/:env/clusters/:clusterName/namespaces'
        },
        //todo post
        modify_items: {
            method: 'POST',
            url: '/namespace/modify'
        },
        release: {
            method: 'POST',
            url:'/namespace/release'
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

        modify_items: function (appId, env, clusterName, namespaceName, configText, namespaceId, modifyBy) {
            var d = $q.defer();
            config_source.modify_items({}, {
                appId: appId,
                env: env,
                clusterName: clusterName,
                namespaceName: namespaceName,
                configText: configText,
                namespaceId: namespaceId,
                modifyBy:modifyBy
            }, function (result) {
                d.resolve(result);

            }, function (result) {
                d.reject(result);
            });
            return d.promise;
        },
        
        release: function (appId, env, clusterName, namespaceName, releaseBy, comment) {
            var d = $q.defer();
            config_source.release({},{
                appId: appId,
                env: env,
                clusterName: clusterName,
                namespaceName: namespaceName,
                releaseBy: releaseBy,
                releaseComment: comment
            }, function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
            return d.promise;
        }
    }

}]);
