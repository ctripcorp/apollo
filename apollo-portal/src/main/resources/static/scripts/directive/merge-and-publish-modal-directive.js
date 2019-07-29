directive_module.directive('mergeandpublishmodal', mergeAndPublishDirective);

function mergeAndPublishDirective(AppUtil, EventManager) {
    return {
        restrict: 'E',
        templateUrl: '../../views/component/merge-and-publish-modal.html',
        transclude: true,
        replace: true,
        scope: {
            appId: '=',
            env: '=',
            cluster: '='
        },
        link: function (scope) {

            scope.showReleaseModal = showReleaseModal;

            EventManager.subscribe(EventManager.EventType.MERGE_AND_PUBLISH_NAMESPACE,
                function (context) {
                    var branch = context.branch;
                    scope.toReleaseNamespace = branch;
                    scope.toDeleteBranch = branch;
                    scope.isEmergencyPublish =
                        context.isEmergencyPublish ? context.isEmergencyPublish : false;

                    var branchStatusMerge = 2;
                    branch.branchStatus = branchStatusMerge;
                    branch.mergeAndPublish = true;

                    AppUtil.showModal('#mergeAndPublishModal');
                });

            function showReleaseModal() {
                EventManager.emit(EventManager.EventType.PUBLISH_NAMESPACE,
                    {
                        namespace: scope.toReleaseNamespace,
                        isEmergencyPublish: scope.isEmergencyPublish
                    });
            }

        }
    }
}


