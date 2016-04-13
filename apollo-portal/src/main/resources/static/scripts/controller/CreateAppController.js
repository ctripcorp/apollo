create_app_module.controller('CreateAppController', ['$scope', '$window', 'toastr', 'AppService',
    function ($scope, $window, toastr, AppService) {

        //todo 便于测试，后续删掉
        $scope.app = {
            appId: 1001,
            name: 'lepdou',
            ownerPhone: '1111',
            ownerMail: 'qqq@qq.com',
            owner: 'le'
        };

        $scope.save = function () {
            AppService.add($scope.app).then(function (result) {
                toastr.success('添加成功!');
                setInterval(function () {
                    $window.location.href = '/views/app.html?#appid=' + result.appId;
                }, 1000);
            }, function (result) {
                toastr.error('添加失败!');
            });
        };

    }]);
