appService.service('SystemRoleService', ['$resource', '$q', 'AppUtil', function ($resource, $q,AppUtil) {
    var system_role_service = $resource('', {}, {
        add_create_application_role: {
            method: 'POST',
            url: AppUtil.prefixPath() + '/system/role/createApplication'
        },
        delete_create_application_role: {
            method: 'DELETE',
            url: AppUtil.prefixPath() + '/system/role/createApplication/:userId'
        },
        get_create_application_role_users: {
            method: 'GET',
            url: AppUtil.prefixPath() + '/system/role/createApplication',
            isArray: true
        },
        has_open_manage_app_master_role_limit: {
            method: 'GET',
            url: AppUtil.prefixPath() + '/system/role/manageAppMaster'
        }
    });
    return {
        add_create_application_role: function (userId) {
            var finished = false;
            var d = $q.defer();
            system_role_service.add_create_application_role([
                   userId
                ],
                function (result) {
                    finished = true;
                    d.resolve(result);
                },
                function (result) {
                    finished = true;
                    d.reject(result);
                });
            return d.promise;
        },
        delete_create_application_role: function (userId) {
            var finished = false;
            var d = $q.defer();
            system_role_service.delete_create_application_role({
                    "userId" : userId
                },
                function (result) {
                    finished = true;
                    d.resolve(result);
                },
                function (result) {
                    finished = true;
                    d.reject(result);
                });
            return d.promise;
        },
        get_create_application_role_users: function () {
            var finished = false;
            var d = $q.defer();
            system_role_service.get_create_application_role_users({},
                function (result) {
                    finished = true;
                    d.resolve(result);
                },
                function (result) {
                    finished = true;
                    d.reject(result);
                });
            return d.promise;
        },
        has_open_manage_app_master_role_limit: function () {
            var finished = false;
            var d = $q.defer();
            system_role_service.has_open_manage_app_master_role_limit({},
                function (result) {
                    finished = true;
                    d.resolve(result);
                },
                function (result) {
                    finished = true;
                    d.reject(result);
                });
            return d.promise;
        }

    }
}]);
