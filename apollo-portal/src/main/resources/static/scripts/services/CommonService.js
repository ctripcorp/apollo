appService.service('CommonService', ['$resource', '$q', 'AppUtil',
                                       function ($resource, $q, AppUtil) {
    var resource = $resource('', {}, {
        page_setting: {
            method: 'GET',
            isArray: false,
            url: AppUtil.prefixPath() + '/page-settings'
        }
    });

                                           
    return {
        getPageSetting: function () {
            return AppUtil.ajax(resource.page_setting, {});
        }
    }
}]);
