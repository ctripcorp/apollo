login_module.controller('LoginController',
    ['$scope', '$window', '$location', '$translate', 'toastr', 'AppUtil',
        LoginController]);

function LoginController($scope, $window, $location, $translate, toastr, AppUtil) {
    if ($location.$$url) {
        var params = AppUtil.parseParams($location.$$url);
        if (params.error) {
            $translate('Login.UserNameOrPasswordIncorrect').then(function(result)  {
                $scope.info = result;
            })
        }
        if (params.logout) {
            $scope.info = $translate.instant('Login.LogoutSuccessfully');
        }
    }

}
