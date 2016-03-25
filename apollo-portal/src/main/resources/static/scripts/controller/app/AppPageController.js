//page context ctl
application_module.controller("AppPageController", ['$rootScope', '$location',
    function ($rootScope, $location) {

        $rootScope.appId = $location.$$url.split("=")[1];

        if(!$rootScope.appId){
            $rootScope.appId = 6666;
        }

        $rootScope.breadcrumb = {
            project: '6666-apollo',
            nav: '配置',
            env: 'uat'
        }
    }]);
