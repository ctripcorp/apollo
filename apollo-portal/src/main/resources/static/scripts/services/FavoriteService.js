appService.service('FavoriteService', ['$resource', '$q', 'AppUtil', function ($resource, $q, AppUtil) {
    var resource = $resource('', {}, {
        find_favorites: {
            method: 'GET',
            url: AppUtil.prefixPath() + '/favorites',
            isArray: true
        },
        add_favorite: {
            method: 'POST',
            url: AppUtil.prefixPath() + '/favorites'
        },
        delete_favorite: {
            method: 'DELETE',
            url: AppUtil.prefixPath() + '/favorites/:favoriteId'
        },
        to_top: {
            method: 'PUT',
            url: AppUtil.prefixPath() + '/favorites/:favoriteId'
        }
    });
    return {
        findFavorites: function (userId, appId, page, size) {
            var d = $q.defer();
            resource.find_favorites({
                                        userId: userId,
                                        appId: appId,
                                        page: page,
                                        size: size
                                    }, function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
            return d.promise;
        },
        addFavorite: function (favorite) {
            var d = $q.defer();
            resource.add_favorite({}, favorite, function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
            return d.promise;
        },
        deleteFavorite: function (favoriteId) {
            var d = $q.defer();
            resource.delete_favorite({
                                          favoriteId: favoriteId
                                      }, function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
            return d.promise;
        },
        toTop: function (favoriteId) {
            var d = $q.defer();
            resource.to_top({
                                favoriteId: favoriteId
                            }, {}, function (result) {
                d.resolve(result);
            }, function (result) {
                d.reject(result);
            });
            return d.promise;
        }
    }
}]);
