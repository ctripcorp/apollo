login_module.controller('LoginController',
    ['$scope', '$window', '$location', 'toastr', 'AppUtil', '$translate',
        LoginController]);

function LoginController($scope, $window, $location, toastr, AppUtil, $translate) {
    if ($location.$$url) {
        var params = AppUtil.parseParams($location.$$url);
        if (params.error) {
            $translate('Login.UserNameOrPasswordIncorrect').then(result => {
                $scope.info = result;
            })
        }
        if (params.logout) {
            $scope.info = "登出成功";
        }
    }

}
