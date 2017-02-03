/** navbar */
directive_module.directive('apollonav',
                           function ($compile, $window, toastr, AppUtil, AppService, EnvService, UserService) {
                               return {
                                   restrict: 'E',
                                   templateUrl: '../../views/common/nav.html',
                                   transclude: true,
                                   replace: true,
                                   link: function (scope, element, attrs) {

                                       scope.sourceApps = [];
                                       scope.copyedApps = [];

                                       AppService.find_apps().then(function (result) {
                                           result.forEach(function (app) {
                                               app.selected = false;
                                               scope.sourceApps.push(app);
                                           });
                                           scope.copyedApps = angular.copy(scope.sourceApps);
                                       }, function (result) {
                                           toastr.error(AppUtil.errorMsg(result), "load apps error");
                                       });

                                       scope.searchKey = '';
                                       scope.shouldShowAppList = false;

                                       var selectedApp = {};
                                       scope.selectApp = function (app) {
                                           select(app);
                                           scope.jumpToConfigPage();
                                       };

                                       scope.changeSearchKey = function () {
                                           scope.copyedApps = [];
                                           var searchKey = scope.searchKey.toLocaleLowerCase();
                                           scope.sourceApps.forEach(function (app) {
                                               if (app.name.toLocaleLowerCase().indexOf(searchKey) > -1
                                                   || app.appId.toLocaleLowerCase().indexOf(searchKey) > -1) {
                                                   scope.copyedApps.push(app);
                                               }
                                           });
                                           scope.shouldShowAppList = true;
                                       };

                                       scope.jumpToConfigPage = function () {
                                           if (selectedApp.appId) {
                                               if ($window.location.href.indexOf("config.html") > -1) {
                                                   $window.location.hash = "appid=" + selectedApp.appId;
                                                   $window.location.reload();
                                               } else {
                                                   $window.location.href = '/config.html?#appid=' + selectedApp.appId;
                                               }
                                           }
                                       };

                                       //up:38 down:40 enter:13
                                       var selectedAppIdx = -1;
                                       element.bind("keydown keypress", function (event) {

                                           if (event.keyCode == 40) {
                                               if (selectedAppIdx < scope.copyedApps.length - 1) {
                                                   clearAppsSelectedStatus();
                                                   scope.copyedApps[++selectedAppIdx].selected = true;
                                               }
                                           } else if (event.keyCode == 38) {
                                               if (selectedAppIdx >= 1) {
                                                   clearAppsSelectedStatus();
                                                   scope.copyedApps[--selectedAppIdx].selected = true;
                                               }
                                           } else if (event.keyCode == 13) {
                                               if (scope.shouldShowAppList && selectedAppIdx > -1) {
                                                   select(scope.copyedApps[selectedAppIdx]);
                                                   event.preventDefault();
                                               } else {
                                                   scope.jumpToConfigPage();
                                               }

                                           }
                                           //强制刷新
                                           scope.$apply(function () {
                                               scope.copyedApps = scope.copyedApps;
                                           });
                                       });

                                       $(".search-input").on("click", function (event) {
                                           event.stopPropagation();
                                       });

                                       $(document).on('click', function () {
                                           scope.$apply(function () {
                                               scope.shouldShowAppList = false;
                                           });
                                       });

                                       function clearAppsSelectedStatus() {
                                           scope.copyedApps.forEach(function (app) {
                                               app.selected = false;
                                           })

                                       }

                                       function select(app) {
                                           selectedApp = app;
                                           scope.searchKey = app.name;
                                           scope.shouldShowAppList = false;
                                           clearAppsSelectedStatus();
                                           selectedAppIdx = -1;

                                       }

                                       UserService.load_user().then(function (result) {
                                           scope.userName = result.userId;
                                       }, function (result) {

                                       });
                                   }
                               }

                           });

/** env cluster selector*/
directive_module.directive('apolloclusterselector', function ($compile, $window, AppService, AppUtil, toastr) {
    return {
        restrict: 'E',
        templateUrl: '../../views/component/env-selector.html',
        transclude: true,
        replace: true,
        scope: {
            appId: '=apolloAppId',
            defaultAllChecked: '=apolloDefaultAllChecked',
            select: '=apolloSelect',
            defaultCheckedEnv: '=apolloDefaultCheckedEnv',
            defaultCheckedCluster: '=apolloDefaultCheckedCluster',
            notCheckedEnv: '=apolloNotCheckedEnv',
            notCheckedCluster: '=apolloNotCheckedCluster'
        },
        link: function (scope, element, attrs) {

            scope.$watch("defaultCheckedEnv", refreshClusterList);
            scope.$watch("defaultCheckedCluster", refreshClusterList);

            refreshClusterList();

            function refreshClusterList() {
                AppService.load_nav_tree(scope.appId).then(function (result) {
                    scope.clusters = [];
                    var envClusterInfo = AppUtil.collectData(result);
                    envClusterInfo.forEach(function (node) {
                        var env = node.env;
                        node.clusters.forEach(function (cluster) {
                            cluster.env = env;
                            //default checked
                            cluster.checked = scope.defaultAllChecked ||
                                              (cluster.env == scope.defaultCheckedEnv && cluster.name
                                                                                         == scope.defaultCheckedCluster);
                            //not checked
                            if (cluster.env == scope.notCheckedEnv && cluster.name == scope.notCheckedCluster) {
                                cluster.checked = false;
                            }

                            scope.clusters.push(cluster);
                        })
                    });
                    scope.select(collectSelectedClusters());
                });
            }

            scope.envAllSelected = scope.defaultAllChecked;

            scope.toggleEnvsCheckedStatus = function () {
                scope.envAllSelected = !scope.envAllSelected;
                scope.clusters.forEach(function (cluster) {
                    cluster.checked = scope.envAllSelected;
                });
                scope.select(collectSelectedClusters());
            };

            scope.switchSelect = function (o, $event) {
                o.checked = !o.checked;
                $event.stopPropagation();
                scope.select(collectSelectedClusters());
            };

            scope.toggleClusterCheckedStatus = function (cluster) {
                cluster.checked = !cluster.checked;
                scope.select(collectSelectedClusters());
            };

            function collectSelectedClusters() {
                var selectedClusters = [];
                scope.clusters.forEach(function (cluster) {
                    if (cluster.checked) {
                        cluster.clusterName = cluster.name;
                        selectedClusters.push(cluster);
                    }
                });
                return selectedClusters;
            }

        }
    }

});

/** 必填项*/
directive_module.directive('apollorequiredfield', function ($compile, $window) {
    return {
        restrict: 'E',
        template: '<strong style="color: red">*</strong>',
        transclude: true,
        replace: true
    }
});

/**  确认框 */
directive_module.directive('apolloconfirmdialog', function ($compile, $window) {
    return {
        restrict: 'E',
        templateUrl: '../../views/component/confirm-dialog.html',
        transclude: true,
        replace: true,
        scope: {
            dialogId: '=apolloDialogId',
            title: '=apolloTitle',
            detail: '=apolloDetail',
            showCancelBtn: '=apolloShowCancelBtn',
            doConfirm: '=apolloConfirm',
            cancel: '='
        },
        link: function (scope, element, attrs) {

            scope.confirm = function () {
                if (scope.doConfirm) {
                    scope.doConfirm();
                }
            }

        }
    }
});

/** entrance */
directive_module.directive('apolloentrance', function ($compile, $window) {
    return {
        restrict: 'E',
        templateUrl: '../../views/component/entrance.html',
        transclude: true,
        replace: true,
        scope: {
            imgSrc: '=apolloImgSrc',
            title: '=apolloTitle',
            href: '=apolloHref'
        },
        link: function (scope, element, attrs) {
        }
    }
});

/** entrance */
directive_module.directive('apollouserselector', function ($compile, $window) {
    return {
        restrict: 'E',
        templateUrl: '../../views/component/user-selector.html',
        transclude: true,
        replace: true,
        scope: {
            id: '=apolloId'
        },
        link: function (scope, element, attrs) {

            scope.$watch("id", initSelect2);

            var searchUsersAjax = {
                ajax: {
                    url: '/users',
                    dataType: 'json',
                    delay: 250,
                    data: function (params) {
                        return {
                            keyword: params.term ? params.term : '',
                            limit: 100
                        }
                    },
                    processResults: function (data, params) {
                        var users = [];
                        data.forEach(function (user) {
                            users.push({
                                           id: user.userId,
                                           text: user.userId + " | " + user.name
                                       })
                        });
                        return {
                            results: users
                        }

                    },
                    cache: true,
                    minimumInputLength: 5
                }
            };

            function initSelect2() {
                $('.' + scope.id).select2(searchUsersAjax);
            }

        }
    }
});

directive_module.directive('apollomultipleuserselector', function ($compile, $window) {
    return {
        restrict: 'E',
        templateUrl: '../../views/component/multiple-user-selector.html',
        transclude: true,
        replace: true,
        scope: {
            id: '=apolloId'
        },
        link: function (scope, element, attrs) {

            scope.$watch("id", initSelect2);

            var searchUsersAjax = {
                ajax: {
                    url: '/users',
                    dataType: 'json',
                    delay: 250,
                    data: function (params) {
                        return {
                            keyword: params.term ? params.term : '',
                            limit: 100
                        }
                    },
                    processResults: function (data, params) {
                        var users = [];
                        data.forEach(function (user) {
                            users.push({
                                           id: user.userId,
                                           text: user.userId + " | " + user.name
                                       })
                        });
                        return {
                            results: users
                        }

                    },
                    cache: true,
                    minimumInputLength: 5
                }
            };

            function initSelect2() {
                $('.' + scope.id).select2(searchUsersAjax);
            }
        }
    }
});


