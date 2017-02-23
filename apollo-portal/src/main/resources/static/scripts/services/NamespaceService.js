appService.service("NamespaceService", ['$resource', '$q', function ($resource, $q) {
    var namespace_source = $resource("", {}, {
        find_public_namespaces: {
            method: 'GET',
            isArray: true,
            url: '/appnamespaces/public'
        },
        createNamespace: {
            method: 'POST',
            url: '/apps/:appId/namespaces',
            isArray: false
        },
        createAppNamespace: {
            method: 'POST',
            url: '/apps/:appId/appnamespaces',
            isArray: false
        },
        getNamespacePublishInfo: {
            method: 'GET',
            url: '/apps/:appId/namespaces/publish_info'
        },
        deleteNamespace: {
            method: 'DELETE',
            url: '/apps/:appId/envs/:env/clusters/:clusterName/namespaces/:namespaceName'
        },
        getPublicAppNamespaceAllNamespaces: {
            method: 'GET',
            url: '/envs/:env/appnamespaces/:publicNamespaceName/namespaces',
            isArray: true
        }
    });

    function find_public_namespaces() {
        var d = $q.defer();
        namespace_source.find_public_namespaces({}, function (result) {
            d.resolve(result);
        }, function (result) {
            d.reject(result);
        });
        return d.promise;
    }

    function createNamespace(appId, namespaceCreationModel) {
        var d = $q.defer();
        namespace_source.createNamespace({
                                             appId: appId
                                         }, namespaceCreationModel, function (result) {
            d.resolve(result);
        }, function (result) {
            d.reject(result);
        });
        return d.promise;
    }

    function createAppNamespace(appId, appnamespace) {
        var d = $q.defer();
        namespace_source.createAppNamespace({
                                                appId: appId
                                            }, appnamespace, function (result) {
            d.resolve(result);
        }, function (result) {
            d.reject(result);
        });
        return d.promise;
    }

    function getNamespacePublishInfo(appId) {
        var d = $q.defer();
        namespace_source.getNamespacePublishInfo({
                                                     appId: appId
                                                 }, function (result) {
            d.resolve(result);
        }, function (result) {
            d.reject(result);
        });

        return d.promise;
    }

    function deleteNamespace(appId, env, clusterName, namespaceName) {
        var d = $q.defer();
        namespace_source.deleteNamespace({
                                             appId: appId,
                                             env: env,
                                             clusterName: clusterName,
                                             namespaceName: namespaceName
                                         },
                                         function (result) {
                                             d.resolve(result);
                                         },
                                         function (result) {
                                             d.reject(result);
                                         });

        return d.promise;
    }

    function getPublicAppNamespaceAllNamespaces(env, publicNamespaceName, page, size) {
        var d = $q.defer();
        namespace_source.getPublicAppNamespaceAllNamespaces({
                                                                env: env,
                                                                publicNamespaceName: publicNamespaceName,
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
        find_public_namespaces: find_public_namespaces,
        createNamespace: createNamespace,
        createAppNamespace: createAppNamespace,
        getNamespacePublishInfo: getNamespacePublishInfo,
        deleteNamespace: deleteNamespace,
        getPublicAppNamespaceAllNamespaces: getPublicAppNamespaceAllNamespaces
    }

}]);
