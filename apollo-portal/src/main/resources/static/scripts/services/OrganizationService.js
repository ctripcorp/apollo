appService.service("OrganizationService", ['$resource', '$q', 'AppUtil', function ($resource, $q, AppUtil) {
    var organization_source = $resource("", {}, {
        find_organizations: {
            method: 'GET',
            isArray: true,
            url: AppUtil.prefixPath() + '/organizations'
        }
    });

    return {
        find_organizations: function () {
            var d = $q.defer();
            organization_source.find_organizations({}, function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
            return d.promise;
        }
    }

}]);
