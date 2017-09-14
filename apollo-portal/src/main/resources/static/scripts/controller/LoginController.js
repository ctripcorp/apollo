login_module.controller('LoginController', ['$scope', '$http', '$window', 'toastr',
        LoginController]);

    function LoginController($scope, $http, $window, toastr){

        $scope.postData = {};

    $scope.loginIn = function(){
        $http({
            method: 'POST',
            url: '/doLogin',
            data: $scope.postData,
            headers:{'Content-Type': 'application/x-www-form-urlencoded'},
            transformRequest: function(obj) {
                var str = [];
                for(var p in obj){
                    str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
                }
                return str.join("&");
            }
        }).then(function successCallback(response) {
            if(response.data.code == 100){
                return toastr.error(response.data.message);
            };
            $window.location.href = '/';
        }, function errorCallback(response) {
        });
    }

}   
