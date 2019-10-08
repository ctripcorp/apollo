user_module.controller('UserController',
    ['$scope', '$window', '$translate', 'toastr', 'AppUtil', 'UserService',
        UserController]);

function UserController($scope, $window, $translate, toastr, AppUtil, UserService) {

    $scope.user = {};

    $scope.createOrUpdateUser = function () {
        UserService.createOrUpdateUser($scope.user).then(function (result) {
            toastr.success($translate.instant('UserMange.Created'));
        }, function (result) {
            AppUtil.showErrorMsg(result, $translate.instant('UserMange.CreateFailed'));
        })

    }
}
