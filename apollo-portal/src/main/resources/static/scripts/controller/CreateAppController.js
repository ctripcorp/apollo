create_app_module.controller('CreateAppController', ['$scope', '$window', 'AppService', function ($scope, $window, AppService) {

    $scope.app = {
        appId: 1001,
        name: 'lepdou',
        ownerPhone: '1111',
        ownerMail: 'qqq@qq.com',
        owner: 'le'
    };

    $scope.save = function(){
        AppService.add($scope.app).then(function(result){
            $window.location.href = '/views/app/index.html?#appid=' + result.appId;

        },function(result){
            console.log("add fail");
        });
    };

}]);
