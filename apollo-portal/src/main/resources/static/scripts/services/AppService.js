appService.service('AppService', ['$resource', '$q', function ($resource, $q) {
    var app_resource = $resource('/apps/:appId', {}, {
        load_app: {
            method: 'GET',
            isArray: false
        },
        add_app: {
            method: 'POST',
            url: '/apps'
        }
    });
    return {
        add: function add(app) {
            var d = $q.defer();
            app_resource.add_app({}, app, function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
            return d.promise;
        },
        load: function load(appId) {
            var d = $q.defer();
            app_resource.load_app({
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
