directive_module.directive('publishdenymodal', publishDenyDirective);

function publishDenyDirective(AppUtil, EventManager) {
    return {
        restrict: 'E',
        templateUrl: AppUtil.prefixPath() + '/views/component/publish-deny-modal.html',
        transclude: true,
        replace: true,
        scope: {
            env: "="
        },
        link: function (scope) {
            var MODAL_ID = "#publishDenyModal";

            EventManager.subscribe(EventManager.EventType.PUBLISH_DENY, function (context) {
                scope.toReleaseNamespace = context.namespace;
                scope.mergeAndPublish = !!context.mergeAndPublish;
                AppUtil.showModal(MODAL_ID);
            });

            scope.emergencyPublish = emergencyPublish;

            function emergencyPublish() {
                AppUtil.hideModal(MODAL_ID);

                EventManager.emit(EventManager.EventType.EMERGENCY_PUBLISH,
                    {
                        mergeAndPublish: scope.mergeAndPublish,
                        namespace: scope.toReleaseNamespace
                    });

            }
        }
    }
}


