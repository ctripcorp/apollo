user_module.controller('UserController',
                      ['$scope', '$window', 'toastr', 'AppUtil', 'UserService',
                       UserController]);

function UserController($scope, $window, toastr, AppUtil, UserService) {

    $scope.user = {};
    
    $scope.createOrUpdateUser = function () {
        UserService.createOrUpdateUser($scope.user).then(function (result) {
            toastr.success("创建用户成功");
        }, function (result) {
            AppUtil.showErrorMsg(result, "创建用户失败");
        })

    }
}
