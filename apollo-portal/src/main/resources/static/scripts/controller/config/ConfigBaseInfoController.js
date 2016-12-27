application_module.controller("ConfigBaseInfoController",
                              ['$rootScope', '$scope', '$location', 'toastr', 'EventManager', 'UserService',
                               'AppService',
                               'FavoriteService',
                               'PermissionService',
                               'AppUtil', ConfigBaseInfoController]);

function ConfigBaseInfoController($rootScope, $scope, $location, toastr, EventManager, UserService, AppService,
                                  FavoriteService,
                                  PermissionService,
                                  AppUtil) {

    var appId = AppUtil.parseParams($location.$$url).appid;

    initPage();

    function initPage() {
        $rootScope.hideTip = JSON.parse(localStorage.getItem("hideTip"));

        //load session storage to recovery scene
        var scene = JSON.parse(sessionStorage.getItem(appId));
        $rootScope.pageContext = {
            appId: appId,
            env: scene ? scene.env : '',
            clusterName: scene ? scene.cluster : 'default'
        };

        UserService.load_user().then(function (result) {
            $rootScope.pageContext.userId = result.userId;
            loadAppInfo();
            handleFavorite();
        }, function (result) {
            toastr.error(AppUtil.errorMsg(result), "获取用户登录信息失败");
        });

        handlePermission();
    }

    function loadAppInfo() {
        $scope.notFoundApp = true;
        AppService.load($rootScope.pageContext.appId).then(function (result) {
            $scope.notFoundApp = false;

            $scope.appBaseInfo = result;
            $scope.appBaseInfo.orgInfo = result.orgName + '(' + result.orgId + ')';

            loadNavTree();
            recordVisitApp();

            $(".J_appFound").removeClass("hidden");
        }, function (result) {
            $(".J_appNotFound").removeClass("hidden");
        });

        ////// 补缺失的环境 //////
        $scope.missEnvs = [];
        AppService.find_miss_envs($rootScope.pageContext.appId).then(function (result) {
            $scope.missEnvs = AppUtil.collectData(result);
        }, function (result) {

        });

        $scope.createAppInMissEnv = function () {
            var count = 0;
            $scope.missEnvs.forEach(function (env) {
                AppService.create_remote(env, $scope.appBaseInfo).then(function (result) {
                    toastr.success(env, '创建成功');
                    count++;
                    if (count == $scope.missEnvs.length) {
                        location.reload(true);
                    }
                }, function (result) {
                    toastr.error(AppUtil.errorMsg(result), '创建失败:' + env);
                    count++;
                    if (count == $scope.missEnvs.length) {
                        location.reload(true);
                    }
                });
            });
        };
    }

    function recordVisitApp() {
        //save user recent visited apps
        var VISITED_APPS_STORAGE_KEY = "VisitedAppsV2";
        var visitedAppsObject = JSON.parse(localStorage.getItem(VISITED_APPS_STORAGE_KEY));
        var hasSaved = false;

        if (!visitedAppsObject) {
            visitedAppsObject = {};
        }

        if (!visitedAppsObject[$rootScope.pageContext.userId]) {
            visitedAppsObject[$rootScope.pageContext.userId] = [];
        }

        var visitedApps = visitedAppsObject[$rootScope.pageContext.userId];
        if (visitedApps && visitedApps.length > 0) {
            visitedApps.forEach(function (app) {
                if (app == appId) {
                    hasSaved = true;
                    return;
                }
            });
        }

        var currentUserVisitedApps = visitedAppsObject[$rootScope.pageContext.userId];
        if (!hasSaved) {
            //if queue's length bigger than 6 will remove oldest app
            if (currentUserVisitedApps.length >= 6) {
                currentUserVisitedApps.splice(0, 1);
            }
            currentUserVisitedApps.push($rootScope.pageContext.appId);

            localStorage.setItem(VISITED_APPS_STORAGE_KEY,
                                 JSON.stringify(visitedAppsObject));
        }

    }

    function loadNavTree() {

        AppService.load_nav_tree($rootScope.pageContext.appId).then(function (result) {
            var navTree = [];
            var nodes = AppUtil.collectData(result);

            if (!nodes || nodes.length == 0) {
                toastr.error("系统出错,请重试或联系系统负责人");
                return;
            }
            //default first env if session storage is empty
            if (!$rootScope.pageContext.env) {
                $rootScope.pageContext.env = nodes[0].env;
            }

            EventManager.emit(EventManager.EventType.REFRESH_NAMESPACE);

            nodes.forEach(function (env) {
                if (!env.clusters || env.clusters.length == 0) {
                    return;
                }
                var node = {};
                node.text = env.env;

                var clusterNodes = [];

                //如果env下面只有一个default集群则不显示集群列表
                if (env.clusters && env.clusters.length == 1 && env.clusters[0].name
                                                                == 'default') {
                    if ($rootScope.pageContext.env == env.env) {
                        node.state = {};
                        node.state.selected = true;
                    }
                    node.selectable = true;

                } else {
                    node.selectable = false;
                    //cluster list
                    env.clusters.forEach(function (cluster) {
                        var clusterNode = {},
                            parentNode = [];

                        //default selection from session storage or first env & first cluster
                        if ($rootScope.pageContext.env == env.env && $rootScope.pageContext.clusterName
                                                                     == cluster.name) {
                            clusterNode.state = {};
                            clusterNode.state.selected = true;
                        }

                        clusterNode.text = cluster.name;
                        parentNode.push(node.text);
                        clusterNode.tags = ['集群'];
                        clusterNode.parentNode = parentNode;
                        clusterNodes.push(clusterNode);

                    });
                }
                node.nodes = clusterNodes;
                navTree.push(node);
            });

            //init treeview
            $('#treeview').treeview({
                                        color: "#797979",
                                        showBorder: true,
                                        data: navTree,
                                        levels: 99,
                                        expandIcon: '',
                                        collapseIcon: '',
                                        showTags: true,
                                        onNodeSelected: function (event, data) {
                                            if (!data.parentNode) {//first nav node
                                                $rootScope.pageContext.env = data.text;
                                                $rootScope.pageContext.clusterName =
                                                    'default';
                                            } else {//second cluster node
                                                $rootScope.pageContext.env =
                                                    data.parentNode[0];
                                                $rootScope.pageContext.clusterName =
                                                    data.text;
                                            }
                                            //storage scene
                                            sessionStorage.setItem(
                                                $rootScope.pageContext.appId,
                                                JSON.stringify({
                                                                   env: $rootScope.pageContext.env,
                                                                   cluster: $rootScope.pageContext.clusterName
                                                               }));

                                            EventManager.emit(EventManager.EventType.REFRESH_NAMESPACE);
                                        }
                                    });

            var envMapClusters = {};
            navTree.forEach(function (node) {
                if (node.nodes && node.nodes.length > 0) {

                    var clusterNames = [];
                    node.nodes.forEach(function (cluster) {
                        if (cluster.text != 'default') {
                            clusterNames.push(cluster.text);
                        }

                    });

                    envMapClusters[node.text] = clusterNames.join(",");

                }
            });

            $rootScope.envMapClusters = envMapClusters;

        }, function (result) {
            toastr.error(AppUtil.errorMsg(result), "系统出错,请重试或联系系统负责人");
        });

    }

    function handleFavorite() {

        FavoriteService.findFavorites($rootScope.pageContext.userId,
                                      $rootScope.pageContext.appId)
            .then(function (result) {
                if (result && result.length) {
                    $scope.favoriteId = result[0].id;
                }

            });

        $scope.addFavorite = function () {
            var favorite = {
                userId: $rootScope.pageContext.userId,
                appId: $rootScope.pageContext.appId
            };

            FavoriteService.addFavorite(favorite)
                .then(function (result) {
                    $scope.favoriteId = result.id;
                    toastr.success("收藏成功");
                }, function (result) {
                    toastr.error(AppUtil.errorMsg(result), "收藏失败");
                })
        };

        $scope.deleteFavorite = function () {
            FavoriteService.deleteFavorite($scope.favoriteId)
                .then(function (result) {
                    $scope.favoriteId = 0;
                    toastr.success("取消收藏成功");
                }, function (result) {
                    toastr.error(AppUtil.errorMsg(result), "取消收藏失败");
                })
        };
    }

    function handlePermission() {
        //permission
        PermissionService.has_create_namespace_permission(appId).then(function (result) {
            $scope.hasCreateNamespacePermission = result.hasPermission;
        }, function (result) {

        });

        PermissionService.has_create_cluster_permission(appId).then(function (result) {
            $scope.hasCreateClusterPermission = result.hasPermission;
        }, function (result) {

        });

        PermissionService.has_assign_user_permission(appId).then(function (result) {
            $scope.hasAssignUserPermission = result.hasPermission;
        }, function (result) {

        });

        $scope.showMasterPermissionTips = function () {
            $("#masterNoPermissionDialog").modal('show');
        };
    }

}

