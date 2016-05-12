appService.service('AppService', ['$resource', '$q', function ($resource, $q) {
    var app_resource = $resource('/apps/:appId', {}, {
        find_all_app:{
            method: 'GET',
            isArray: true,
            url:'/apps/envs/:env'
        },
        load_navtree:{
            methode: 'GET',
            isArray:false,
            url:'/apps/:appId/navtree'
        },
        load_app: {
            method: 'GET',
            isArray: false
        },
        create_app: {
            method: 'POST',
            url: '/apps/envs/:env'
        },
        find_miss_envs: {
            method: 'GET',
            url: '/apps/:appId/miss_envs'
        }
    });
    return {
        find_all_app: function (env) {
            var d = $q.defer();
            app_resource.find_all_app({
                                          env: env
                                      }, function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
            return d.promise;
        },
        load_nav_tree: function (appId){
            var d = $q.defer();
            app_resource.load_navtree({
                appId: appId
            }, function(result){
                d.resolve(result);
            }, function(result){
                d.reject(result);
            });
            return d.promise;
        },
        create: function (env, app) {
            var d = $q.defer();
            app_resource.create_app({env:env}, app, function (result) {
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
        }
    }
}]);
