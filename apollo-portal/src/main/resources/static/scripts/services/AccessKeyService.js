appService.service('AccessKeyService', ['$resource', '$q', function ($resource, $q) {
    var access_key_resource = $resource('', {}, {
        load_access_keys: {
            method: 'GET',
            isArray: true,
            url: '/apps/:appId/envs/:env/accesskeys'
        },
        create_access_key: {
            method: 'POST',
            url: '/apps/:appId/envs/:env/accesskeys'
        },
        remove_access_key: {
            method: 'DELETE',
            url: '/apps/:appId/envs/:env/accesskeys/:id'
        },
        enable_access_key: {
            method: 'PUT',
            url: '/apps/:appId/envs/:env/accesskeys/:id/enable'
        },
        disable_access_key: {
            method: 'PUT',
            url: '/apps/:appId/envs/:env/accesskeys/:id/disable'
        }
    });
    return {
        load_access_keys: function (appId, env) {
            var d = $q.defer();
            access_key_resource.load_access_keys({
                    appId: appId,
                    env: env
                },
                function (result) {
                    d.resolve(result);
                }, function (result) {
                    d.reject(result);
                });
            return d.promise;
        },
        create_access_key: function (appId, env, user) {
            var d = $q.defer();
            access_key_resource.create_access_key({
                    appId: appId,
                    env: env
                }, { dataChangeCreatedBy: user },
                function (result) {
                    d.resolve(result);
                }, function (result) {
                    d.reject(result);
                });
            return d.promise;
        },
        remove_access_key: function (appId, env, id) {
            var d = $q.defer();
            access_key_resource.remove_access_key({
                    appId: appId,
                    env: env,
                    id: id
                },
                function (result) {
                    d.resolve(result);
                }, function (result) {
                    d.reject(result);
                });
            return d.promise;
        },
        enable_access_key: function (appId, env, id) {
            var d = $q.defer();
            access_key_resource.enable_access_key({
                    appId: appId,
                    env: env,
                    id: id
                }, {},
                function (result) {
                    d.resolve(result);
                }, function (result) {
                    d.reject(result);
                });
            return d.promise;
        },
        disable_access_key: function (appId, env, id) {
            var d = $q.defer();
            access_key_resource.disable_access_key({
                    appId: appId,
                    env: env,
                    id: id
                }, {},
                function (result) {
                    d.resolve(result);
                }, function (result) {
                    d.reject(result);
                });
            return d.promise;
        }
    }
}]);
