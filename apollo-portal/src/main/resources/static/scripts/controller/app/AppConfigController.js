
application_module.controller("AppConfigController", ["$scope", '$state', '$location', 'AppService',
    function ($scope, $state, $location, AppService) {

        //model定义
        $scope.env = {
            fat: false,
            uat: true,
            product: false

        };

        //mock data
        $scope.config = {
            baseConfigs: [
                {
                    key: 'pageSize',
                    value: 10,
                    lastUpdateTime: '2016-01-14'
                },
                {
                    key: 'pageCount',
                    value: 20,
                    lastUpdateTime: '2016-01-14'
                }
            ],
            overrideConfigs: [
                {
                    project: 'cat',
                    configs: [
                        {
                            key: 'pageSize',
                            value: 10,
                            lastUpdateTime: '2016-01-14'
                        },
                        {
                            key: 'pageCount',
                            value: 20,
                            lastUpdateTime: '2016-01-14'
                        }
                    ]
                },
                {
                    project: 'hermas',
                    configs: [
                        {
                            key: 'pageSize',
                            value: 20,
                            lastUpdateTime: '2016-01-14'
                        },
                        {
                            key: 'pageCount',
                            value: 30,
                            lastUpdateTime: '2016-01-14'
                        }
                    ]
                }
            ]
        };

        $scope.switchEnv = function (env) {
            clearEnvNav();
            if ('fat' == env) {
                switchToFat();
            } else if ('uat' == env) {
                switchToUat();
            } else if ('product' == env) {
                switchToProduct();
            }

        };

        function switchToFat() {
            $scope.env.fat = true;
        }

        function switchToUat() {
            $scope.env.uat = true;
        }

        function switchToProduct() {
            $scope.env.product = true;
        }

        function clearEnvNav() {
            $scope.env = {
                fat: false,
                uat: false,
                product: false

            };
        }


    }]);
