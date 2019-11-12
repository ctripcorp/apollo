appService.service('EnvService', ['$resource', '$q', 'AppUtil', function ($resource, $q, AppUtil) {
    var env_resource = $resource(AppUtil.prefixPath() + '/envs', {}, {
        find_all_envs:{
            method: 'GET',
            isArray: true,
            url: AppUtil.prefixPath() + '/envs'
        }
    });
    return {
        find_all_envs: function () {
            var d = $q.defer();
            env_resource.find_all_envs({
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
