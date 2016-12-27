release_history_module.controller("ReleaseHistoryController",
                                  ['$scope', '$location', 'AppUtil',
                                   'ReleaseService', 'ConfigService', 'ReleaseHistoryService', releaseHistoryController
                                  ]);

function releaseHistoryController($scope, $location, AppUtil,
                                  ReleaseService, ConfigService, ReleaseHistoryService) {

    var params = AppUtil.parseParams($location.$$url);
    $scope.pageContext = {
        appId: params.appid,
        env: params.env,
        clusterName: params.clusterName,
        namespaceName: params.namespaceName,
        releaseId: params.releaseId,
        releaseHistoryId: params.releaseHistoryId
    };
    var PAGE_SIZE = 10;
    var CONFIG_VIEW_TYPE = {
        DIFF: 'diff',
        ALL: 'all'
    };

    $scope.page = 0;
    $scope.releaseHistories = [];
    $scope.hasLoadAll = false;
    $scope.selectedReleaseHistory = 0;
    $scope.isTextNamespace = false;

    $scope.showReleaseHistoryDetail = showReleaseHistoryDetail;
    $scope.switchConfigViewType = switchConfigViewType;
    $scope.findReleaseHistory = findReleaseHistory;
    $scope.showText = showText;

    init();

    function init() {

        findReleaseHistory();

        loadNamespace();
    }

    function findReleaseHistory() {
        if ($scope.hasLoadAll) {
            return;
        }
        ReleaseHistoryService.findReleaseHistoryByNamespace($scope.pageContext.appId,
                                                            $scope.pageContext.env,
                                                            $scope.pageContext.clusterName,
                                                            $scope.pageContext.namespaceName,
                                                            $scope.page, PAGE_SIZE)
            .then(function (result) {
                if ($scope.page == 0) {
                    $(".release-history").removeClass('hidden');
                }

                if (!result || result.length < PAGE_SIZE) {
                    $scope.hasLoadAll = true;
                }

                if (result.length == 0) {
                    return;
                }

                $scope.releaseHistories = $scope.releaseHistories.concat(result);

                if ($scope.page == 0) {
                    var defaultToShowReleaseHistory = result[0];
                    $scope.releaseHistories.forEach(function (history) {
                        if ($scope.pageContext.releaseHistoryId == history.id) {
                            defaultToShowReleaseHistory = history;
                        } else if ($scope.pageContext.releaseId == history.releaseId) {
                            history.viewType = CONFIG_VIEW_TYPE.ALL;
                            defaultToShowReleaseHistory = history;
                        }
                    });

                    showReleaseHistoryDetail(defaultToShowReleaseHistory);
                }

                $scope.page = $scope.page + 1;

            }, function (result) {
                AppUtil.showErrorMsg(result, "加载发布历史信息出错");
            });
    }

    function loadNamespace() {
        ConfigService.load_namespace($scope.pageContext.appId,
                                     $scope.pageContext.env,
                                     $scope.pageContext.clusterName,
                                     $scope.pageContext.namespaceName)
            .then(function (result) {
                $scope.isTextNamespace = result.format != "properties";
            })
    }

    function showReleaseHistoryDetail(history) {

        $scope.history = history;
        $scope.selectedReleaseHistory = history.id;
        if (!history.viewType) {//default view type
            history.viewType = CONFIG_VIEW_TYPE.DIFF;
            getReleaseDiffConfiguration(history);
        }

    }

    function switchConfigViewType(history, viewType) {
        history.viewType = viewType;

        if (viewType == CONFIG_VIEW_TYPE.DIFF) {
            getReleaseDiffConfiguration(history);
        }

    }

    function getReleaseDiffConfiguration(history) {

        if (!history.changes) {

            //Set previous release id to master latest release id when branch first gray release.
            if (history.operation == 2 && history.previousReleaseId == 0) {
                history.previousReleaseId = history.operationContext.baseReleaseId;
            }

            ReleaseService.compare($scope.pageContext.env,
                                   history.previousReleaseId,
                                   history.releaseId)
                .then(function (result) {
                    history.changes = result.changes;
                })
        }
    }

    function showText(text) {
        $scope.text = text;
        AppUtil.showModal("#showTextModal");
    }
}

