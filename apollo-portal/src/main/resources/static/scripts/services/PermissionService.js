appService.service('PermissionService', ['$resource', '$q', function ($resource, $q) {
    var permission_resource = $resource('', {}, {
        init_app_namespace_permission: {
            method: 'POST',
            url: '/apps/:appId/initPermission?namespace=:namespace'
        },
        has_app_permission: {
            method: 'GET',
            url: '/apps/:appId/permissions/:permissionType'
        },
        has_namespace_permission: {
            method: 'GET',
            url: '/apps/:appId/namespaces/:namespaceName/permissions/:permissionType'
        },
        has_namespace_env_permission: {
            method: 'GET',
            url: '/apps/:appId/envs/:env/namespaces/:namespaceName/permissions/:permissionType'
        },
        has_root_permission:{
            method: 'GET',
            url: '/permissions/root'
        },
        get_namespace_role_users: {
            method: 'GET',
            url: '/apps/:appId/namespaces/:namespaceName/role_users'
        },
        get_namespace_env_role_users: {
            method: 'GET',
            url: '/apps/:appId/envs/:env/namespaces/:namespaceName/role_users'
        },
        assign_namespace_role_to_user: {
            method: 'POST',
            url: '/apps/:appId/namespaces/:namespaceName/roles/:roleType'
        },
        assign_namespace_env_role_to_user: {
            method: 'POST',
            url: '/apps/:appId/envs/:env/namespaces/:namespaceName/roles/:roleType'
        },
        remove_namespace_role_from_user: {
            method: 'DELETE',
            url: '/apps/:appId/namespaces/:namespaceName/roles/:roleType?user=:user'
        },
        remove_namespace_env_role_from_user: {
            method: 'DELETE',
            url: '/apps/:appId/envs/:env/namespaces/:namespaceName/roles/:roleType?user=:user'
        },
        get_app_role_users: {
            method: 'GET',
            url: '/apps/:appId/role_users'    
        },
        assign_app_role_to_user: {
            method: 'POST',
            url: '/apps/:appId/roles/:roleType'
        },
        remove_app_role_from_user: {
            method: 'DELETE',
            url: '/apps/:appId/roles/:roleType?user=:user'
        }
    });

    function initAppNamespacePermission(appId, namespace) {
        var d = $q.defer();
        permission_resource.init_app_namespace_permission({
                appId: appId,
                namespace: namespace
            }, namespace,
            function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
        return d.promise;
    }

    function hasAppPermission(appId, permissionType) {
        var d = $q.defer();
        permission_resource.has_app_permission({
                                                   appId: appId,
                                                   permissionType: permissionType
                                               },
                                               function (result) {
                                                   d.resolve(result);
                                               }, function (result) {
                d.reject(result);
            });
        return d.promise;
    }

    function hasNamespacePermission(appId, namespaceName, permissionType) {
        var d = $q.defer();
        permission_resource.has_namespace_permission({
                                                         appId: appId,
                                                         namespaceName: namespaceName,
                                                         permissionType: permissionType
                                                     },
                                                     function (result) {
                                                         d.resolve(result);
                                                     }, function (result) {
                d.reject(result);
            });
        return d.promise;
    }

    function hasNamespaceEnvPermission(appId, env, namespaceName, permissionType) {
        var d = $q.defer();
        permission_resource.has_namespace_env_permission({
                appId: appId,
                namespaceName: namespaceName,
                permissionType: permissionType,
                env: env
            },
            function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
        return d.promise;
    }

    function assignNamespaceRoleToUser(appId, namespaceName, roleType, user) {
        var d = $q.defer();
        permission_resource.assign_namespace_role_to_user({
                                                              appId: appId,
                                                              namespaceName: namespaceName,
                                                              roleType: roleType
                                                          }, user,
                                                          function (result) {
                                                              d.resolve(result);
                                                          }, function (result) {
                d.reject(result);
            });
        return d.promise;
    }

    function assignNamespaceEnvRoleToUser(appId, env, namespaceName, roleType, user) {
        var d = $q.defer();
        permission_resource.assign_namespace_env_role_to_user({
                appId: appId,
                namespaceName: namespaceName,
                roleType: roleType,
                env: env
            }, user,
            function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
        return d.promise;
    }

    function removeNamespaceRoleFromUser(appId, namespaceName, roleType, user) {
        var d = $q.defer();
        permission_resource.remove_namespace_role_from_user({
                                                                appId: appId,
                                                                namespaceName: namespaceName,
                                                                roleType: roleType,
                                                                user: user
                                                            },
                                                            function (result) {
                                                                d.resolve(result);
                                                            }, function (result) {
                d.reject(result);
            });
        return d.promise;
    }

    function removeNamespaceEnvRoleFromUser(appId, env, namespaceName, roleType, user) {
        var d = $q.defer();
        permission_resource.remove_namespace_env_role_from_user({
                appId: appId,
                namespaceName: namespaceName,
                roleType: roleType,
                user: user,
                env: env
            },
            function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
        return d.promise;
    }

    return {
        init_app_namespace_permission: function (appId, namespace) {
            return initAppNamespacePermission(appId, namespace);
        },
        has_create_namespace_permission: function (appId) {
            return hasAppPermission(appId, 'CreateNamespace');
        },
        has_create_cluster_permission: function (appId) {
            return hasAppPermission(appId, 'CreateCluster');
        },
        has_assign_user_permission: function (appId) {
            return hasAppPermission(appId, 'AssignRole');
        },
        has_modify_namespace_permission: function (appId, namespaceName) {
            return hasNamespacePermission(appId, namespaceName, 'ModifyNamespace');
        },
        has_modify_namespace_env_permission: function (appId, env, namespaceName) {
            return hasNamespaceEnvPermission(appId, env, namespaceName, 'ModifyNamespace');
        },
        has_release_namespace_permission: function (appId, namespaceName) {
            return hasNamespacePermission(appId, namespaceName, 'ReleaseNamespace');
        },
        has_release_namespace_env_permission: function (appId, env, namespaceName) {
            return hasNamespaceEnvPermission(appId, env, namespaceName, 'ReleaseNamespace');
        },
        has_root_permission: function () {
            var d = $q.defer();
            permission_resource.has_root_permission({ },
                                                         function (result) {
                                                             d.resolve(result);
                                                         }, function (result) {
                    d.reject(result);
                });
            return d.promise;    
            
        },
        assign_modify_namespace_role: function (appId, namespaceName, user) {
            return assignNamespaceRoleToUser(appId, namespaceName, 'ModifyNamespace', user);
        },
        assign_modify_namespace_env_role: function (appId, env, namespaceName, user) {
            return assignNamespaceEnvRoleToUser(appId, env, namespaceName, 'ModifyNamespace', user);
        },
        assign_release_namespace_role: function (appId, namespaceName, user) {
            return assignNamespaceRoleToUser(appId, namespaceName, 'ReleaseNamespace', user);
        },
        assign_release_namespace_env_role: function (appId, env, namespaceName, user) {
            return assignNamespaceEnvRoleToUser(appId, env, namespaceName, 'ReleaseNamespace', user);
        },
        remove_modify_namespace_role: function (appId, namespaceName, user) {
            return removeNamespaceRoleFromUser(appId, namespaceName, 'ModifyNamespace', user);
        },
        remove_modify_namespace_env_role: function (appId, env, namespaceName, user) {
            return removeNamespaceEnvRoleFromUser(appId, env, namespaceName, 'ModifyNamespace', user);
        },
        remove_release_namespace_role: function (appId, namespaceName, user) {
            return removeNamespaceRoleFromUser(appId, namespaceName, 'ReleaseNamespace', user);
        },
        remove_release_namespace_env_role: function (appId, env, namespaceName, user) {
            return removeNamespaceEnvRoleFromUser(appId, env, namespaceName, 'ReleaseNamespace', user);
        },
        get_namespace_role_users: function (appId, namespaceName) {
            var d = $q.defer();
            permission_resource.get_namespace_role_users({
                                                              appId: appId,
                                                              namespaceName: namespaceName,
                                                          },
                                                         function (result) {
                                                              d.resolve(result);
                                                          }, function (result) {
                    d.reject(result);
                });
            return d.promise;
        },
        get_namespace_env_role_users: function (appId, env, namespaceName) {
            var d = $q.defer();
            permission_resource.get_namespace_env_role_users({
                    appId: appId,
                    namespaceName: namespaceName,
                    env: env
                },
                function (result) {
                    d.resolve(result);
                }, function (result) {
                    d.reject(result);
                });
            return d.promise;
        },
        get_app_role_users: function (appId) {
            var d = $q.defer();
            permission_resource.get_app_role_users({
                                                        appId: appId
                                                    },
                                                   function (result) {
                                                        d.resolve(result);
                                                    }, function (result) {
                    d.reject(result);
                });
            return d.promise;    
        },
        assign_master_role: function (appId, user) {
            var d = $q.defer();
            permission_resource.assign_app_role_to_user({
                                                            appId: appId,
                                                            roleType: 'Master'
                                                        }, user,
                                                        function (result) {
                                                            d.resolve(result);
                                                        }, function (result) {
                    d.reject(result);
                });
            return d.promise;
        },
        remove_master_role: function (appId, user) {
            var d = $q.defer();
            permission_resource.remove_app_role_from_user({
                                                              appId: appId,
                                                              roleType: 'Master',
                                                              user: user
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
