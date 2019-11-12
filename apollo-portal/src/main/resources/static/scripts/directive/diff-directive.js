directive_module.directive('apollodiff',
    function ($compile, $window, AppUtil) {
        return {
            restrict: 'E',
            templateUrl: AppUtil.prefixPath() + '/views/component/diff.html',
            transclude: true,
            replace: true,
            scope: {
                oldStr: '=',
                newStr: '=',
                apolloId: '='
            },
            link: function (scope, element, attrs) {

                scope.$watch('oldStr', makeDiff);
                scope.$watch('newStr', makeDiff);



                function makeDiff() {
                    var displayArea = document.getElementById(scope.apolloId);
                    if (!displayArea) {
                        return;
                    }
                    //clear
                    displayArea.innerHTML = '';

                    var color = '',
                        span = null,
                        pre = '';

                    var oldStr = scope.oldStr == undefined ? '' : scope.oldStr;
                    var newStr = scope.newStr == undefined ? '' : scope.newStr;

                    var diff = JsDiff.diffLines(oldStr, newStr),
                        fragment = document.createDocumentFragment();

                    diff.forEach(function (part) {
                        // green for additions, red for deletions
                        // grey for common parts
                        color = part.added ? 'green' :
                            part.removed ? 'red' : 'grey';
                        span = document.createElement('span');
                        span.style.color = color;
                        pre = part.added ? '+' :
                            part.removed ? '-' : '';
                        span.appendChild(document.createTextNode(pre + part.value));
                        fragment.appendChild(span);
                    });

                    displayArea.appendChild(fragment);

                }

            }
        }

    });
