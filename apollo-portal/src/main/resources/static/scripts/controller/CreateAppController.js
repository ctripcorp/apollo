create_app_module.controller('CreateAppController', ['$scope', '$window', 'toastr', 'AppService',
    function ($scope, $window, toastr, AppService) {

        $scope.save = function () {
            AppService.add($scope.app).then(function (result) {
                toastr.success('添加成功!');
                setInterval(function () {
                    $window.location.href = '/views/app.html?#appid=' + result.appId;
                }, 1000);
            }, function (result) {
                toastr.error(result.status + result.data.message, '添加失败!');
            });
        };

    }]);
