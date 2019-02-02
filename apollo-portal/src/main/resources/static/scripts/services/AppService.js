appService.service('AppService', ['$resource', '$q', function ($resource, $q) {
    var app_resource = $resource('/apps/:appId', {}, {
        find_apps: {
            method: 'GET',
            isArray: true,
            url: '/apps'
        },
        find_app_by_owner: {
            method: 'GET',
            isArray: true,
            url: '/apps/by-owner'
        },
        load_navtree: {
            method: 'GET',
            isArray: false,
            url: '/apps/:appId/navtree'
        },
        load_app: {
            method: 'GET',
            isArray: false
        },
        create_app: {
            method: 'POST',
            url: '/apps'
        },
        update_app: {
            method: 'PUT',
            url: '/apps/:appId'
        },
        create_app_remote: {
            method: 'POST',
            url: '/apps/envs/:env'
        },
        find_miss_envs: {
            method: 'GET',
            url: '/apps/:appId/miss_envs'
        },
        create_missing_namespaces: {
            method: 'POST',
            url: '/apps/:appId/envs/:env/clusters/:clusterName/missing-namespaces'
        },
        find_missing_namespaces: {
            method: 'GET',
            url: '/apps/:appId/envs/:env/clusters/:clusterName/missing-namespaces'
        },
        delete_app: {
            method: 'DELETE',
            isArray: false
        }
    });
    return {
        find_apps: function (appIds) {
            if (!appIds) {
                appIds = '';
            }
            var d = $q.defer();
            app_resource.find_apps({appIds: appIds}, function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
            return d.promise;
        },
        find_app_by_owner: function (owner, page, size) {
            var d = $q.defer();
            app_resource.find_app_by_owner({
               owner: owner,
               page: page,
               size: size
            }, function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
            return d.promise;
        },
        load_nav_tree: function (appId) {
            var d = $q.defer();
            app_resource.load_navtree({
                                          appId: appId
                                      }, function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
            return d.promise;
        },
        create: function (app) {
            var d = $q.defer();
            app_resource.create_app({}, app, function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
            return d.promise;
        },
        update: function (app) {
            var d = $q.defer();
            app_resource.update_app({
                                        appId: app.appId
                                    }, app, function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
            return d.promise;
        },
        create_remote: function (env, app) {
            var d = $q.defer();
            app_resource.create_app_remote({env: env}, app, function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
            return d.promise;
        },
        load: function (appId) {
            var d = $q.defer();
            app_resource.load_app({
                                      appId: appId
                                  }, function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
            return d.promise;
        },
        find_miss_envs: function (appId) {
            var d = $q.defer();
            app_resource.find_miss_envs({
                                            appId: appId
                                        }, function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
            return d.promise;
        },
        create_missing_namespaces: function (appId, env, clusterName) {
            var d = $q.defer();
            app_resource.create_missing_namespaces({
                                            appId: appId,
                                            env: env,
                                            clusterName: clusterName
                                        }, null, function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
            return d.promise;
        },
        find_missing_namespaces: function (appId, env, clusterName) {
            var d = $q.defer();
            app_resource.find_missing_namespaces({
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
        delete_app: function (appId) {
            var d = $q.defer();
            app_resource.delete_app({
                appId: appId
            }, function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
            return d.promise;
        }
    }
}]);
