application_module.controller("AppInfoController", ["$scope", '$rootScope', '$state', '$location', 'toastr', 'AppService',
    function ($scope, $rootScope, $state, $location, toastr, AppService) {
        $rootScope.breadcrumb.nav = '应用信息';
        $rootScope.breadcrumb.env = '';

        AppService.load($scope.appId).then(function (result) {
            $scope.app = result;
        }, function (result) {
            toastr.error("加载出错");
        });

    }]);
