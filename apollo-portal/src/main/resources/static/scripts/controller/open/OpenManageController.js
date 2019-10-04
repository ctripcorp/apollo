open_manage_module.controller('OpenManageController',
    ['$scope', '$translate', 'toastr', 'AppUtil', 'OrganizationService', 'ConsumerService', 'PermissionService', 'EnvService',
        OpenManageController]);

function OpenManageController($scope, $translate, toastr, AppUtil, OrganizationService, ConsumerService, PermissionService, EnvService) {

    var $orgWidget = $('#organization');

    $scope.consumer = {};
    $scope.consumerRole = {
        type: 'NamespaceRole'
    };

    $scope.submitBtnDisabled = false;
    $scope.userSelectWidgetId = 'toAssignMasterRoleUser';

    $scope.getTokenByAppId = getTokenByAppId;
    $scope.createConsumer = createConsumer;
    $scope.assignRoleToConsumer = assignRoleToConsumer;

    function init() {
        initOrganization();
        initPermission();
        initEnv();
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
                placeholder: $translate.instant('Common.PleaseChooseDepartment'),
                width: '100%',
                data: organizations
            });
        }, function (result) {
            toastr.error(AppUtil.errorMsg(result), "load organizations error");
        });
    }

    function initPermission() {
        PermissionService.has_root_permission()
            .then(function (result) {
                $scope.isRootUser = result.hasPermission;
            });
    }

    function initEnv() {
        EnvService.find_all_envs()
            .then(function (result) {
                $scope.envs = new Array();
                for (var iLoop = 0; iLoop < result.length; iLoop++) {
                    $scope.envs.push({ checked: false, env: result[iLoop] });
                    $scope.envsChecked = new Array();
                }

                $scope.switchSelect = function (item) {
                    item.checked = !item.checked;
                    $scope.envsChecked = new Array();
                    for (var iLoop = 0; iLoop < $scope.envs.length; iLoop++) {
                        var env = $scope.envs[iLoop];
                        if (env.checked) {
                            $scope.envsChecked.push(env.env);
                        }
                    }
                };
            });
    }

    function getTokenByAppId() {
        if (!$scope.consumer.appId) {
            toastr.warning($translate.instant('Open.Manage.PleaseEnterAppId'));
            return;
        }

        ConsumerService.getConsumerTokenByAppId($scope.consumer.appId)
            .then(function (consumerToken) {

                if (consumerToken.token) {
                    $scope.consumerToken = consumerToken;
                    $scope.consumerRole.token = consumerToken.token;
                } else {
                    $scope.consumerToken = {
                        token: $translate.instant('Open.Manage.AppNotCreated', { appId: $scope.consumer.appId })
                    };
                }
            });
    }

    function createConsumer() {
        $scope.submitBtnDisabled = true;

        if (!$scope.consumer.appId) {
            toastr.warning($translate.instant('Open.Manage.PleaseEnterAppId'));
            return;
        }
        var selectedOrg = $orgWidget.select2('data')[0];

        if (!selectedOrg.id) {
            toastr.warning($translate.instant('Common.PleaseChooseDepartment'));
            return;
        }

        $scope.consumer.orgId = selectedOrg.id;
        $scope.consumer.orgName = selectedOrg.name;

        // owner
        var owner = $('.ownerSelector').select2('data')[0];
        if (!owner) {
            toastr.warning($translate.instant('Common.PleaseChooseOwner'));
            return;
        }
        $scope.consumer.ownerName = owner.id;

        ConsumerService.createConsumer($scope.consumer)
            .then(function (consumerToken) {
                toastr.success($translate.instant('Common.Created'));
                $scope.consumerToken = consumerToken;
                $scope.consumerRole.token = consumerToken.token;
                $scope.submitBtnDisabled = false;
                $scope.consumer = {};
            }, function (response) {
                AppUtil.showErrorMsg(response, $translate.instant('Common.CreateFailed'));
                $scope.submitBtnDisabled = false;
            })

    }

    function assignRoleToConsumer() {
        ConsumerService.assignRoleToConsumer($scope.consumerRole.token,
            $scope.consumerRole.type,
            $scope.consumerRole.appId,
            $scope.consumerRole.namespaceName,
            $scope.envsChecked)
            .then(function (consumerRoles) {
                toastr.success($translate.instant('Open.Manage.GrantSuccessfully'));
            }, function (response) {
                AppUtil.showErrorMsg(response, $translate.instant('Open.Manage.GrantFailed'));
            })
    }

    init();
}
