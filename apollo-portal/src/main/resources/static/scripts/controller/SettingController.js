setting_module.controller('SettingController',
                          ['$scope', '$location', 'toastr',
                           'AppService', 'AppUtil', 'PermissionService',
                           'OrganizationService',
                           SettingController]);

function SettingController($scope, $location, toastr,
                           AppService, AppUtil, PermissionService,
                           OrganizationService) {

    var params = AppUtil.parseParams($location.$$url);
    var $orgWidget = $('#organization');

    $scope.pageContext = {
        appId: params.appid
    };
    $scope.display = {
        app: {
            edit: false
        }
    };

    $scope.submitBtnDisabled = false;
    $scope.userSelectWidgetId = 'toAssignMasterRoleUser';

    $scope.assignMasterRoleToUser = assignMasterRoleToUser;
    $scope.removeMasterRoleFromUser = removeMasterRoleFromUser;
    $scope.toggleEditStatus = toggleEditStatus;
    $scope.updateAppInfo = updateAppInfo;

    init();

    function init() {
        initOrganization();
        initPermission();
        initAdmins();
        initApplication();

    }

    function initOrganization() {
        OrganizationService.find_organizations().then(function (result) {
            var organizations = [];
            result.forEach(function (item) {
                var org = {};
                org.id = item.orgId;
                org.text = item.orgName + '(' + item.orgId + ')';
                org.name = item.orgName;
                organizations.push(org);
            });
            $orgWidget.select2({
                                   placeholder: '请选择部门',
                                   width: '100%',
                                   data: organizations
                               });
        }, function (result) {
            toastr.error(AppUtil.errorMsg(result), "load organizations error");
        });
    }

    function initPermission() {
        PermissionService.has_assign_user_permission($scope.pageContext.appId)
            .then(function (result) {
                $scope.hasAssignUserPermission = result.hasPermission;
            });
    }

    function initAdmins() {
        PermissionService.get_app_role_users($scope.pageContext.appId)
            .then(function (result) {
                $scope.appRoleUsers = result;
                $scope.admins = [];
                $scope.appRoleUsers.masterUsers.forEach(function (user) {
                    $scope.admins.push(user.userId);
                });

            });
    }

    function initApplication() {
        AppService.load($scope.pageContext.appId).then(function (app) {
            $scope.app = app;
            $scope.viewApp = _.clone(app);
            initAppForm(app);
            $('.project-setting .panel').removeClass('hidden');
        })

    }

    function initAppForm(app) {
        $orgWidget.val(app.orgId).trigger("change");

        var $ownerSelector = $('.ownerSelector');
        var defaultSelectedDOM = '<option value="' + app.ownerName + '" selected="selected">' + app.ownerName
                                 + '</option>';
        $ownerSelector.append(defaultSelectedDOM);
        $ownerSelector.trigger('change');
    }

    function assignMasterRoleToUser() {
        var user = $('.' + $scope.userSelectWidgetId).select2('data')[0];
        if (!user) {
            toastr.warning("请选择用户");
            return;
        }
        var toAssignMasterRoleUser = user.id;
        $scope.submitBtnDisabled = true;
        PermissionService.assign_master_role($scope.pageContext.appId,
                                             toAssignMasterRoleUser)
            .then(function (result) {
                $scope.submitBtnDisabled = false;
                toastr.success("添加成功");
                $scope.appRoleUsers.masterUsers.push({userId: toAssignMasterRoleUser});
                $('.' + $scope.userSelectWidgetId).select2("val", "");
            }, function (result) {
                $scope.submitBtnDisabled = false;
                toastr.error(AppUtil.errorMsg(result), "添加失败");
            });
    }

    function removeMasterRoleFromUser(user) {
        if ($scope.appRoleUsers.masterUsers.length <= 1) {
            $('#warning').modal('show');
            return;
        }
        PermissionService.remove_master_role($scope.pageContext.appId, user)
            .then(function (result) {
                toastr.success("删除成功");
                removeUserFromList($scope.appRoleUsers.masterUsers, user);
            }, function (result) {
                toastr.error(AppUtil.errorMsg(result), "删除失败");
            });
    }

    function removeUserFromList(list, user) {
        var index = 0;
        for (var i = 0; i < list.length; i++) {
            if (list[i].userId == user) {
                index = i;
                break;
            }
        }
        list.splice(index, 1);
    }

    function toggleEditStatus() {
        if ($scope.display.app.edit) {//cancel edit
            $scope.viewApp = _.clone($scope.app);
            initAppForm($scope.viewApp);
        } else {//edit

        }

        $scope.display.app.edit = !$scope.display.app.edit;

    }

    function updateAppInfo() {
        $scope.submitBtnDisabled = true;
        var app = $scope.viewApp;

        var selectedOrg = $orgWidget.select2('data')[0];

        if (!selectedOrg.id) {
            toastr.warning("请选择部门");
            return;
        }

        app.orgId = selectedOrg.id;
        app.orgName = selectedOrg.name;

        // owner
        var owner = $('.ownerSelector').select2('data')[0];
        if (!owner) {
            toastr.warning("请选择应用负责人");
            return;
        }
        app.ownerName = owner.id;

        AppService.update(app).then(function (app) {
            toastr.success("修改成功");
            initApplication();
            $scope.display.app.edit = false;
            $scope.submitBtnDisabled = false;
        }, function (result) {
            AppUtil.showErrorMsg(result);
            $scope.submitBtnDisabled = false;
        })

    }
}
