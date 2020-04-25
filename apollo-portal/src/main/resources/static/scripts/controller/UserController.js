user_module.controller('UserController',
    ['$scope', '$window', '$translate', 'toastr', 'AppUtil', 'UserService', 'PermissionService',
        UserController]);

function UserController($scope, $window, $translate, toastr, AppUtil, UserService, PermissionService) {

    $scope.user = {};

    initPermission();

    function initPermission() {
        PermissionService.has_root_permission()
        .then(function (result) {
            $scope.isRootUser = result.hasPermission;
        })
    }

    $scope.createOrUpdateUser = function () {
        UserService.createOrUpdateUser($scope.user).then(function (result) {
            toastr.success($translate.instant('UserMange.Created'));
        }, function (result) {
            AppUtil.showErrorMsg(result, $translate.instant('UserMange.CreateFailed'));
        })

    }
}
