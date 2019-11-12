appService.service('ConsumerService', ['$resource', '$q', 'AppUtil', 
                                       function ($resource, $q, AppUtil) {
    var resource = $resource('', {}, {
        create_consumer: {
            method: 'POST',
            isArray: false,
            url: AppUtil.prefixPath() + '/consumers'
        },
        get_consumer_token_by_appId: {
            method: 'GET',
            isArray: false,
            url: AppUtil.prefixPath() + '/consumers/by-appId'
        },
        assign_role_to_consumer: {
            method: 'POST',
            isArray: true,
            url: AppUtil.prefixPath() + '/consumers/:token/assign-role'
        }

    });

                                           
    return {
        createConsumer: function (consumer) {
            return AppUtil.ajax(resource.create_consumer, {}, consumer);
        },
        getConsumerTokenByAppId: function (appId) {
            return AppUtil.ajax(resource.get_consumer_token_by_appId, {
                appId: appId
            });
        },
        assignRoleToConsumer: function (token, type, appId, namespaceName, envs) {
            return AppUtil.ajax(resource.assign_role_to_consumer, 
                                {
                                    token: token,
                                    type: type,
                                    envs: envs
                                },
                                {
                                    appId: appId,
                                    namespaceName: namespaceName
                                }
            )
        }
    }
}]);
