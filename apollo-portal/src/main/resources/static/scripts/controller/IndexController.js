index_module.controller('IndexController', ['$scope', '$window', 'toastr', 'AppUtil', 'AppService',
                                            'UserService', 'FavoriteService',
                                            IndexController]);

function IndexController($scope, $window, toastr, AppUtil, AppService, UserService, FavoriteService) {

    $scope.userId = '';

    $scope.getUserCreatedApps = getUserCreatedApps;
    $scope.getUserFavorites = getUserFavorites;

    $scope.goToAppHomePage = goToAppHomePage;
    $scope.goToCreateAppPage = goToCreateAppPage;
    $scope.toggleOperationBtn = toggleOperationBtn;
    $scope.toTop = toTop;
    $scope.deleteFavorite = deleteFavorite;

    function  initCreateApplicationPermission() {
        AppService.has_create_application_role($scope.userId).then(
            function (value) {
                $scope.hasCreateApplicationPermission = value.hasCreateApplicationPermission;
            },
            function (reason) {
                toastr.warning(AppUtil.errorMsg(reason), "获取创建应用权限信息失败");
            }
        )
    }

    UserService.load_user().then(function (result) {
        $scope.userId = result.userId;

        $scope.createdAppPage = 0;
        $scope.createdApps = [];
        $scope.hasMoreCreatedApps = true;
        $scope.favoritesPage = 0;
        $scope.favorites = [];
        $scope.hasMoreFavorites = true;
        $scope.visitedApps = [];

        initCreateApplicationPermission();

        getUserCreatedApps();

        getUserFavorites();

        initUserVisitedApps();
    });

    function getUserCreatedApps() {
        var size = 10;
        AppService.find_app_by_owner($scope.userId, $scope.createdAppPage, size)
            .then(function (result) {
                $scope.createdAppPage += 1;
                $scope.hasMoreCreatedApps = result.length == size;

                if (!result || result.length == 0) {
                    return;
                }
                result.forEach(function (app) {
                    $scope.createdApps.push(app);
                });

            })
    }

    function getUserFavorites() {
        var size = 11;
        FavoriteService.findFavorites($scope.userId, '', $scope.favoritesPage, size)
            .then(function (result) {
                $scope.favoritesPage += 1;
                $scope.hasMoreFavorites = result.length == size;

                if ($scope.favoritesPage == 1){
                    $("#app-list").removeClass("hidden");
                }

                if (!result || result.length == 0) {
                    return;
                }
                var appIds = [];
                result.forEach(function (favorite) {
                    appIds.push(favorite.appId);
                });


                AppService.find_apps(appIds.join(","))
                    .then(function (apps) {
                        //sort
                        var appIdMapApp = {};
                        apps.forEach(function (app) {
                            appIdMapApp[app.appId] = app;
                        });
                        result.forEach(function (favorite) {
                            var app = appIdMapApp[favorite.appId];
                            if (!app){
                                return;
                            }
                            app.favoriteId = favorite.id;
                            $scope.favorites.push(app);
                        });

                    });
            })
    }

    function initUserVisitedApps() {
        var VISITED_APPS_STORAGE_KEY = "VisitedAppsV2";
        var visitedAppsObject = JSON.parse(localStorage.getItem(VISITED_APPS_STORAGE_KEY));
        if (!visitedAppsObject) {
            visitedAppsObject = {};
        }

        var userVisitedApps = visitedAppsObject[$scope.userId];
        if (userVisitedApps && userVisitedApps.length > 0) {
            AppService.find_apps(userVisitedApps.join(","))
                .then(function (apps) {
                    //sort
                    var appIdMapApp = {};
                    apps.forEach(function (app) {
                        appIdMapApp[app.appId] = app;
                    });

                    userVisitedApps.forEach(function (appId) {
                        var app = appIdMapApp[appId];
                        if (app){
                            $scope.visitedApps.push(app);
                        }
                    });
                });
        }

    }

    function goToCreateAppPage() {
        $window.location.href = "/app.html";
    }

    function goToAppHomePage(appId) {
        $window.location.href = "/config.html?#/appid=" + appId;
    }

    function toggleOperationBtn(app) {
        app.showOperationBtn = !app.showOperationBtn;
    }

    function toTop(favoriteId) {
        FavoriteService.toTop(favoriteId).then(function () {
            toastr.success("置顶成功");
            refreshFavorites();

        })
    }

    function deleteFavorite(favoriteId) {
        FavoriteService.deleteFavorite(favoriteId).then(function () {
            toastr.success("取消收藏成功");
            refreshFavorites();
        })
    }

    function refreshFavorites() {
        $scope.favoritesPage = 0;
        $scope.favorites = [];
        $scope.hasMoreFavorites = true;

        getUserFavorites();
    }

}
