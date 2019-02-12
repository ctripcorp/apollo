role_module.controller('NamespaceRoleController',
    ['$scope', '$location', '$window', 'toastr', 'AppService', 'UserService', 'AppUtil', 'EnvService',
        'PermissionService',
        function ($scope, $location, $window, toastr, AppService, UserService, AppUtil, EnvService,
                  PermissionService) {

            var params = AppUtil.parseParams($location.$$url);
            $scope.pageContext = {
                appId: params.appid,
                namespaceName: params.namespaceName
            };

            $scope.modifyRoleSubmitBtnDisabled = false;
            $scope.ReleaseRoleSubmitBtnDisabled = false;
            $scope.viewerRoleSubmitBtnDisabled = false;

            $scope.releaseRoleWidgetId = 'releaseRoleWidgetId';
            $scope.modifyRoleWidgetId = 'modifyRoleWidgetId';
            $scope.viewerRoleWidgetId = 'viewerRoleWidgetId';

            $scope.modifyRoleSelectedEnv = "";
            $scope.releaseRoleSelectedEnv = "";
            $scope.viewerRoleSelectedEnv = "";

            PermissionService.init_app_namespace_permission($scope.pageContext.appId, $scope.pageContext.namespaceName)
                .then(function (result) {

                }, function(result) {
                    toastr.warn(AppUtil.errorMsg(result), "初始化授权出错");
                });

            PermissionService.has_assign_user_permission($scope.pageContext.appId)
                .then(function (result) {
                    $scope.hasAssignUserPermission = result.hasPermission;
                }, function (reslt) {

                });

            EnvService.find_all_envs()
                .then(function (result){
                    $scope.envs = result;
                    $scope.envRolesAssignedUsers = {};
                    for (var iLoop = 0; iLoop < result.length; iLoop++) {
                        var env = result[iLoop];
                        PermissionService.get_namespace_env_role_users($scope.pageContext.appId, env, $scope.pageContext.namespaceName)
                            .then(function (result) {
                                $scope.envRolesAssignedUsers[result.env] = result;
                            }, function(result) {
                                toastr.error(AppUtil.errorMsg(result), "加载" + env + "授权用户出错");
                            });
                    }
                });

            PermissionService.get_namespace_role_users($scope.pageContext.appId,
                $scope.pageContext.namespaceName)
                .then(function (result) {
                    $scope.rolesAssignedUsers = result;
                }, function (result) {
                    toastr.error(AppUtil.errorMsg(result), "加载授权用户出错");
                });

            /**
             * 给用户授权
             * @param roleType
             */
            $scope.assignRoleToUser = function (roleType) {

                // 发布权
                if ("ReleaseNamespace" === roleType) {
                    var user = $('.' + $scope.releaseRoleWidgetId).select2('data')[0];
                    if (!user) {
                        toastr.warning("请选择用户");
                        return;
                    }
                    $scope.ReleaseRoleSubmitBtnDisabled = true;
                    var toAssignReleaseNamespaceRoleUser = user.id;

                    var assignReleaseNamespaceRoleFunc = $scope.releaseRoleSelectedEnv === "" ?
                        PermissionService.assign_release_namespace_role :
                        function (appId, namespaceName, user) {
                            return PermissionService.assign_release_namespace_env_role(appId, $scope.releaseRoleSelectedEnv, namespaceName, user);
                        };

                    assignReleaseNamespaceRoleFunc($scope.pageContext.appId,
                        $scope.pageContext.namespaceName,
                        toAssignReleaseNamespaceRoleUser)
                        .then(function (result) {
                            toastr.success("添加成功");
                            $scope.ReleaseRoleSubmitBtnDisabled = false;
                            if ($scope.releaseRoleSelectedEnv === "") {
                                $scope.rolesAssignedUsers.releaseRoleUsers.push(
                                    {userId: toAssignReleaseNamespaceRoleUser});
                            } else {
                                $scope.envRolesAssignedUsers[$scope.releaseRoleSelectedEnv].releaseRoleUsers.push(
                                    {userId: toAssignReleaseNamespaceRoleUser});
                            }

                            $('.' + $scope.releaseRoleWidgetId).select2("val", "");
                            $scope.releaseRoleSelectedEnv = "";
                        }, function (result) {
                            $scope.ReleaseRoleSubmitBtnDisabled = false;
                            toastr.error(AppUtil.errorMsg(result), "添加失败");
                        });
                }

                // 修改权
                if('ModifyNamespace' === roleType) {
                    var user = $('.' + $scope.modifyRoleWidgetId).select2('data')[0];
                    if (!user) {
                        toastr.warning("请选择用户");
                        return;
                    }
                    $scope.modifyRoleSubmitBtnDisabled = true;
                    var toAssignModifyNamespaceRoleUser = user.id;

                    var assignModifyNamespaceRoleFunc = $scope.modifyRoleSelectedEnv === "" ?
                        PermissionService.assign_modify_namespace_role :
                        function (appId, namespaceName, user) {
                            return PermissionService.assign_modify_namespace_env_role(appId, $scope.modifyRoleSelectedEnv, namespaceName, user);
                        };

                    assignModifyNamespaceRoleFunc($scope.pageContext.appId,
                        $scope.pageContext.namespaceName,
                        toAssignModifyNamespaceRoleUser)
                        .then(function (result) {
                            toastr.success("添加成功");
                            $scope.modifyRoleSubmitBtnDisabled = false;
                            if ($scope.modifyRoleSelectedEnv === "") {
                                $scope.rolesAssignedUsers.modifyRoleUsers.push(
                                    {userId: toAssignModifyNamespaceRoleUser});
                            } else {
                                $scope.envRolesAssignedUsers[$scope.modifyRoleSelectedEnv].modifyRoleUsers.push(
                                    {userId: toAssignModifyNamespaceRoleUser});
                            }
                            $('.' + $scope.modifyRoleWidgetId).select2("val", "");
                            $scope.modifyRoleSelectedEnv = "";
                        }, function (result) {
                            $scope.modifyRoleSubmitBtnDisabled = false;
                            toastr.error(AppUtil.errorMsg(result), "添加失败");
                        });
                }

                // 查看权
                if("Viewer" === roleType) {
                    var user = $('.' + $scope.viewerRoleWidgetId).select2('data')[0];
                    if (!user) {
                        toastr.warning("请选择用户");
                        return;
                    }
                    $scope.viewerRoleSubmitBtnDisabled = true;
                    var toAssignViewerRoleUser = user.id;

                    var assignViewerRoleFunc =  PermissionService.assign_viewer_role;

                    assignViewerRoleFunc($scope.pageContext.appId, toAssignViewerRoleUser).then(function (result) {
                            toastr.success("添加成功");
                            $scope.viewerRoleSubmitBtnDisabled = false;
                            $scope.rolesAssignedUsers.viewerRoleUsers.push({userId: toAssignViewerRoleUser});
                            $('.' + $scope.viewerRoleWidgetId).select2("val", "");
                            $scope.viewerRoleSelectedEnv = "";
                        }, function (result) {
                            $scope.viewerRoleSubmitBtnDisabled = false;
                            toastr.error(AppUtil.errorMsg(result), "添加失败");
                        });
                }

            };

            $scope.removeUserRole = function (roleType, user, env) {
                if ("ReleaseNamespace" === roleType) {
                    var removeReleaseNamespaceRoleFunc = !env ?
                        PermissionService.remove_release_namespace_role :
                        function (appId, namespaceName, user) {
                            return PermissionService.remove_release_namespace_env_role(appId, env, namespaceName, user);
                        };

                    removeReleaseNamespaceRoleFunc($scope.pageContext.appId,
                        $scope.pageContext.namespaceName,
                        user)
                        .then(function (result) {
                            toastr.success("删除成功");
                            if (!env) {
                                removeUserFromList($scope.rolesAssignedUsers.releaseRoleUsers, user);
                            } else {
                                removeUserFromList($scope.envRolesAssignedUsers[env].releaseRoleUsers, user);
                            }
                        }, function (result) {
                            toastr.error(AppUtil.errorMsg(result), "删除失败");
                        });
                }

                if('ModifyNamespace' === roleType) {
                    var removeModifyNamespaceRoleFunc = !env ?
                        PermissionService.remove_modify_namespace_role :
                        function (appId, namespaceName, user) {
                            return PermissionService.remove_modify_namespace_env_role(appId, env, namespaceName, user);
                        };

                    removeModifyNamespaceRoleFunc($scope.pageContext.appId,
                        $scope.pageContext.namespaceName,
                        user)
                        .then(function (result) {
                            toastr.success("删除成功");
                            if (!env) {
                                removeUserFromList($scope.rolesAssignedUsers.modifyRoleUsers, user);
                            } else {
                                removeUserFromList($scope.envRolesAssignedUsers[env].modifyRoleUsers, user);
                            }
                        }, function (result) {
                            toastr.error(AppUtil.errorMsg(result), "删除失败");
                        });
                }

                if('Viewer' === roleType) {
                    var removeViewerRoleFunc = PermissionService.remove_viewer_role;

                    removeViewerRoleFunc($scope.pageContext.appId,
                        $scope.pageContext.namespaceName,
                        user)
                        .then(function (result) {
                            toastr.success("删除成功");
                            if (!env) {
                                removeUserFromList($scope.rolesAssignedUsers.modifyRoleUsers, user);
                            } else {
                                removeUserFromList($scope.envRolesAssignedUsers[env].modifyRoleUsers, user);
                            }
                        }, function (result) {
                            toastr.error(AppUtil.errorMsg(result), "删除失败");
                        });
                }
            };

            function removeUserFromList(list, user) {
                var index = 0;
                for (var i = 0; i < list.length; i++) {
                    if (list[i].userId === user) {
                        index = i;
                        break;
                    }
                }
                list.splice(index, 1);
            }



        }]);
