angular.module('systemRole', ['app.service', 'apollo.directive', 'app.util', 'toastr', 'angular-loading-bar'])
    .controller('SystemRoleController',
    ['$scope', '$location', '$window', 'toastr', 'AppService', 'UserService', 'AppUtil', 'EnvService',
        'PermissionService', 'SystemRoleService', function SystemRoleController($scope, $location, $window, toastr, AppService, UserService, AppUtil, EnvService,
          PermissionService, SystemRoleService) {

    $scope.addCreateApplicationBtnDisabled = false;
    $scope.deleteCreateApplicationBtnDisabled = false;

    $scope.modifySystemRoleWidgetId = 'modifySystemRoleWidgetId';
    $scope.modifyManageAppMasterRoleWidgetId = 'modifyManageAppMasterRoleWidgetId';

    $scope.hasCreateApplicationPermissionUserList = [];

    $scope.operateManageAppMasterRoleBtn = true;

    $scope.app = {
        appId: "",
        info: ""
    };

    initPermission();

    $scope.addCreateApplicationRoleToUser = function() {
        var user = $('.' + $scope.modifySystemRoleWidgetId).select2('data')[0];
        if (!user) {
            toastr.warning("请选择用户名");
            return;
        }
        SystemRoleService.add_create_application_role(user.id)
            .then(
                function (value) {
                    toastr.info("添加成功");
                    getCreateApplicationRoleUsers();
                },
                function (reason) {
                    toastr.warning(AppUtil.errorMsg(reason), "添加失败");
                }
            );
    };

    $scope.deleteCreateApplicationRoleFromUser = function(userId) {
        SystemRoleService.delete_create_application_role(userId)
            .then(
                function (value) {
                    toastr.info("删除成功");
                    getCreateApplicationRoleUsers();
                },
                function (reason) {
                    toastr.warn(AppUtil.errorMsg(reason), "删除失败");
                }
            );
    };


    function getCreateApplicationRoleUsers() {
        SystemRoleService.get_create_application_role_users()
            .then(
                function (result) {
                    $scope.hasCreateApplicationPermissionUserList = result;
                },
                function (reason) {
                    toastr.warning(AppUtil.errorMsg(reason), "获取拥有创建项目的用户列表出错");
                }
            )
    }

    function initPermission() {
        PermissionService.has_root_permission()
            .then(function (result) {
                $scope.isRootUser = result.hasPermission;
            });
        getCreateApplicationRoleUsers();
    }

    $scope.getAppInfo = function() {
        if (!$scope.app.appId) {
            toastr.warning("请输入appId");
            $scope.operateManageAppMasterRoleBtn = true;
            return;
        }

        $scope.app.info = "";

        AppService.load($scope.app.appId).then(function (result) {
            if (!result.appId) {
                toastr.warning("AppId: " + $scope.app.appId + " 不存在！");
                $scope.operateManageAppMasterRoleBtn = true;
                return;
            }

            $scope.app.info = "应用名：" + result.name + " 部门：" + result.orgName + '(' + result.orgId + ')' + " 负责人：" + result.ownerName;

            $scope.operateManageAppMasterRoleBtn = false;
        }, function (result) {
            AppUtil.showErrorMsg(result);
            $scope.operateManageAppMasterRoleBt = true;
        });
    };

    $scope.deleteAppMasterAssignRole   = function() {
        if (!$scope.app.appId) {
            toastr.warning("请输入appId");
            return;
        }
        var user = $('.' + $scope.modifyManageAppMasterRoleWidgetId).select2('data')[0];
        if (!user) {
            toastr.warning("请选择用户名");
            return;
        }
        if (confirm("确认删除AppId: " + $scope.app.appId + "的用户: " + user.id + " 分配应用管理员的权限？")) {
            AppService.delete_app_master_assign_role($scope.app.appId, user.id).then(function (result) {
                toastr.success("删除AppId: " + $scope.app.appId + "的用户: " + user.id + " 分配应用管理员的权限成功");
                $scope.operateManageAppMasterRoleBtn = true;
            }, function (result) {
                AppUtil.showErrorMsg(result);
            })
        }
    };

    $scope.allowAppMasterAssignRole = function () {
            if (!$scope.app.appId) {
                toastr.warning("请输入appId");
                return;
            }
            var user = $('.' + $scope.modifyManageAppMasterRoleWidgetId).select2('data')[0];
            if (!user) {
                toastr.warning("请选择用户名");
                return;
            }
            if (confirm("确认添加AppId: " + $scope.app.appId + "的用户: " + user.id + " 分配应用管理员的权限？")) {
                AppService.allow_app_master_assign_role($scope.app.appId, user.id).then(function (result) {
                    toastr.success("添加AppId: " + $scope.app.appId + "的用户: " + user.id + " 分配应用管理员的权限成功");
                    $scope.operateManageAppMasterRoleBtn = true;
                }, function (result) {
                    AppUtil.showErrorMsg(result);
                })
            }
        };
}]);
