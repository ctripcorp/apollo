application_module.controller("AppInfoController", ["$scope", '$state', '$location', 'AppService',
    function ($scope, $state, $location, AppService) {
        $scope.appId = $location.$$url.split("=")[1];

        AppService.load($scope.appId).then(function (result) {
            $scope.app = result;
        }, function(result){
            alert("加载出错");
        });

    }]);
