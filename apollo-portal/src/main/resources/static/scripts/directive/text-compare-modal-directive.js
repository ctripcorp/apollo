directive_module.directive('textcomparemodal', textCompareModalDirective);

function textCompareModalDirective($translate, toastr, AppUtil, EventManager, ReleaseService, NamespaceBranchService) {
    return {
        restrict: 'E',
        templateUrl: '../../views/component/text-compare-modal.html',
        transclude: true,
        replace: true,
        scope: {
            appId: '=',
            env: '=',
            cluster: '='
        },
        link: function (scope) {

            let textCompareMode = 0;
            let jsonCompareMode = 1;

            EventManager.subscribe(EventManager.EventType.TEXT_COMPARE,
                function (context) {
                    let compareMode = context.mode;
                    let leftHtmlDiff;
                    let rightHtmlDiff;
                    let htmlDiff;
                    scope.mode = compareMode;
                    switch (compareMode) {
                        case textCompareMode:
                            leftHtmlDiff = "<span class='warn'>old value is empty</span>";
                            rightHtmlDiff = "<span class='warn'>new value is empty</span>";
                            let leftIsEmpty = !context.oldV;
                            let rightIsEmpty = !context.newV;
                            htmlDiff = AppUtil.htmlDiff(context.oldV, context.newV);
                            if (!leftIsEmpty) {
                                leftHtmlDiff = getLeftHtml(htmlDiff);
                            }
                            if (!rightIsEmpty) {
                                rightHtmlDiff = getRightHtml(htmlDiff);
                            }
                            break;
                        case jsonCompareMode:
                            scope.leftIsJson = AppUtil.isJson(context.oldV);
                            scope.rightIsJson = AppUtil.isJson(context.newV);
                            if (!scope.leftIsJson) {
                                leftHtmlDiff = "<span class='warn'>old value is not json</span>";
                            } else {
                                context.oldV = jsl.format.formatJson(context.oldV)
                            }
                            if (!scope.rightIsJson) {
                                rightHtmlDiff = "<span class='warn'>new value is not json</span>";
                            } else {
                                context.newV = jsl.format.formatJson(context.newV)
                            }
                            htmlDiff = AppUtil.htmlDiff(context.oldV, context.newV);
                            if (scope.leftIsJson) {
                                leftHtmlDiff = getLeftHtml(htmlDiff);
                            }
                            if (scope.rightIsJson) {
                                rightHtmlDiff = getRightHtml(htmlDiff);
                            }
                            break;
                        default:
                            alert("expected mode in 0 or 1 , do not support this compare mode:" + context.mode);
                            throw new Error("do not support this compare mode");
                    }
                    scope.leftHtmlDiff = leftHtmlDiff;
                    scope.rightHtmlDiff = rightHtmlDiff;
                    AppUtil.showModal('#textCompareModal');
                });

            /**
             * 非贪婪模式
             * @param originalHtmlDiff
             * @returns {*}
             */
            function getLeftHtml(originalHtmlDiff) {
                return originalHtmlDiff.replace(/<ins>[\W\w]*?<\/ins>/gm, "");
            }

            /**
             * 非贪婪模式
             * @param originalHtmlDiff
             * @returns {*}
             */
            function getRightHtml(originalHtmlDiff) {
                return originalHtmlDiff.replace(/<del>[\W\w]*?<\/del>/gm, "");
            }
        }
    }
}


