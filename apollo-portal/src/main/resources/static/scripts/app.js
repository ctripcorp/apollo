/**service module 定义*/
var appService = angular.module('app.service', ['ngResource']);

/** page module 定义*/
//项目主页
var application_module = angular.module('application', ['ngResource', 'ui.router', 'app.service']);
//创建项目页面
var create_app_module = angular.module('create_app', ['ngResource', 'app.service']);


/**router*/
application_module.config(['$stateProvider',
    function ($stateProvider) {
        $stateProvider
            .state('config', {
                templateUrl: '../../views/app/config.html',
                controller: 'AppConfigController'
            }).state('info', {
                templateUrl: '../../views/app/info.html',
                controller: 'AppInfoController'
            }).state('setting', {
                templateUrl: '../../views/app/setting.html'
            });
    }]).run(function ($state) {
    $state.go('config');
});




