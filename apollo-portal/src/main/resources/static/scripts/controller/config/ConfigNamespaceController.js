application_module.controller("ConfigNamespaceController",
                              ['$rootScope', '$scope', '$window', '$location', 'toastr', 'AppUtil', 'ConfigService',
                               'PermissionService',
                               'CommitService', 'NamespaceLockService', 'UserService', 'ReleaseService',
                               function ($rootScope, $scope, $window, $location, toastr, AppUtil, ConfigService,
                                         PermissionService,
                                         CommitService, NamespaceLockService, UserService, ReleaseService) {

                                   var namespace_view_type = {
                                       TEXT: 'text',
                                       TABLE: 'table',
                                       LOG: 'log'
                                   };

                                   var TABLE_VIEW_OPER_TYPE = {
                                       RETRIEVE: 'retrieve',
                                       CREATE: 'create',
                                       UPDATE: 'update'
                                   };

                                   $rootScope.refreshNamespaces = refreshNamespaces;

                                   $scope.commitChange = commitChange;

                                   $scope.prepareReleaseNamespace = prepareReleaseNamespace;

                                   $scope.release = release;

                                   $scope.showRollbackTips = showRollbackTips;

                                   $scope.preRollback = preRollback;

                                   $scope.rollback = rollback;

                                   $scope.retrieveItem = retrieveItem;

                                   $scope.preDeleteItem = preDeleteItem;

                                   $scope.deleteItem = deleteItem;

                                   $scope.editItem = editItem;

                                   $scope.createItem = createItem;

                                   $scope.doItem = doItem;

                                   $scope.releaseBtnDisabled = false;
                                   $scope.rollbackBtnDisabled = false;
                                   $scope.addItemBtnDisabled = false;
                                   $scope.commitChangeBtnDisabled = false;

                                   PermissionService.get_app_role_users($rootScope.pageContext.appId)
                                       .then(function (result) {
                                           var masterUsers = '';
                                           result.masterUsers.forEach(function (user) {
                                               masterUsers += user.userId + ',';
                                           });
                                           $scope.masterUsers = masterUsers.substring(0, masterUsers.length - 1);
                                       }, function (result) {

                                       });

                                   UserService.load_user().then(function (result) {
                                       $scope.currentUser = result.userId;
                                   });

                                   function refreshNamespaces(viewType) {
                                       if ($rootScope.pageContext.env == '') {
                                           return;
                                       }
                                       ConfigService.load_all_namespaces($rootScope.pageContext.appId,
                                                                         $rootScope.pageContext.env,
                                                                         $rootScope.pageContext.clusterName,
                                                                         viewType).then(
                                           function (result) {

                                               $scope.namespaces = result;

                                           }, function (result) {
                                               toastr.error(AppUtil.errorMsg(result), "加载配置信息出错");
                                           });
                                   }

                                   function commitChange(namespace) {
                                       var model = {
                                           configText: namespace.editText,
                                           namespaceId: namespace.baseInfo.id,
                                           format: namespace.format
                                       };

                                       //prevent repeat submit
                                       if ($scope.commitChangeBtnDisabled) {
                                           return;
                                       }
                                       $scope.commitChangeBtnDisabled = true;
                                       ConfigService.modify_items($rootScope.pageContext.appId,
                                                                  $rootScope.pageContext.env,
                                                                  $rootScope.pageContext.clusterName,
                                                                  namespace.baseInfo.namespaceName,
                                                                  model).then(
                                           function (result) {
                                               toastr.success("更新成功, 如需生效请发布");
                                               //refresh all namespace items
                                               $rootScope.refreshNamespaces();
                                               $scope.commitChangeBtnDisabled = false;
                                               return true;

                                           }, function (result) {
                                               toastr.error(AppUtil.errorMsg(result), "更新失败");
                                               $scope.commitChangeBtnDisabled = false;
                                               return false;
                                           }
                                       );
                                   }

                                   var releaseModal = $('#releaseModal');
                                   $scope.toReleaseNamespace = {};
                                   function prepareReleaseNamespace(namespace) {
                                       if (!namespace.hasReleasePermission) {
                                           $('#releaseNoPermissionDialog').modal('show');
                                           return;
                                       } else if (namespace.lockOwner && $scope.currentUser == namespace.lockOwner) {
                                           //自己修改不能自己发布
                                           $('#releaseDenyDialog').modal('show');
                                       } else {
                                           $('#releaseModal').modal('show');
                                       }
                                       $scope.releaseTitle = new Date().Format("yyyyMMddhhmmss") + "-release";
                                       $scope.toReleaseNamespace = namespace;
                                   }

                                   $scope.releaseComment = '';
                                   function release() {
                                       $scope.releaseBtnDisabled = true;
                                       ReleaseService.release($rootScope.pageContext.appId, $rootScope.pageContext.env,
                                                              $rootScope.pageContext.clusterName,
                                                              $scope.toReleaseNamespace.baseInfo.namespaceName,
                                                              $scope.releaseTitle,
                                                              $scope.releaseComment).then(
                                           function (result) {
                                               releaseModal.modal('hide');
                                               toastr.success("发布成功");
                                               //refresh all namespace items
                                               $scope.releaseBtnDisabled = false;
                                               $rootScope.refreshNamespaces();

                                           }, function (result) {
                                               $scope.releaseBtnDisabled = false;
                                               toastr.error(AppUtil.errorMsg(result), "发布失败");

                                           }
                                       );
                                   }

                                   var toRollbackNamespace = {};
                                   function showRollbackTips(namespace) {
                                       toRollbackNamespace = namespace;
                                       $("#rollbackTips").modal('show');
                                   }

                                   

                                   function preRollback() {
                                       
                                       //load latest two active releases
                                       ReleaseService.findActiveRelease($rootScope.pageContext.appId,
                                                                        $rootScope.pageContext.env,
                                                                        $rootScope.pageContext.clusterName,
                                                                        toRollbackNamespace.baseInfo.namespaceName, 0, 2)
                                           .then(function (result) {
                                               if (result.length <= 1) {
                                                   toastr.error("没有可以回滚的发布历史");
                                                   return;
                                               }
                                               $scope.firstRelease = result[0];
                                               $scope.secondRelease = result[1];

                                               ReleaseService.compare($rootScope.pageContext.env,
                                                                      $scope.firstRelease.id,
                                                                      $scope.secondRelease.id)
                                                   .then(function (result) {
                                                       $scope.releaseCompareResult = result.changes;
                                                       $("#rollbackModal").modal('show');
                                                   }, function (result) {
                                                       toastr.error(AppUtil.errorMsg(result), "对比失败");
                                                   })
                                           }, function (result) {
                                               toastr.error(AppUtil.errorMsg(result), "加载最近两次发布失败");
                                           });
                                   }

                                   function rollback() {
                                       $scope.rollbackBtnDisabled = true;
                                       ReleaseService.rollback(
                                                               $rootScope.pageContext.env,
                                                               $scope.firstRelease.id)
                                           .then(function (result) {
                                               toastr.success("回滚成功");
                                               $scope.rollbackBtnDisabled = false;
                                               $("#rollbackModal").modal("hide");
                                               $rootScope.refreshNamespaces();
                                           }, function (result) {
                                               $scope.rollbackBtnDisabled = false;
                                               toastr.error(AppUtil.errorMsg(result), "回滚失败");
                                           })
                                   }

                                   $scope.tableViewOperType = '', $scope.item = {};
                                   var toOperationNamespace;

                                   //查看配置
                                   function retrieveItem(namespace, item, oldValue) {
                                       switchTableViewOperType(TABLE_VIEW_OPER_TYPE.RETRIEVE);
                                       $scope.item = item;
                                       $scope.item.oldValue = oldValue;
                                       toOperationNamespace = namespace;
                                       $scope.hasModifyPermission = namespace.hasModifyPermission;
                                   }

                                   var toDeleteItemId = 0;

                                   function preDeleteItem(namespace, itemId) {
                                       if (!lockCheck(namespace)) {
                                           return;
                                       }

                                       toOperationNamespace = namespace;
                                       toDeleteItemId = itemId;

                                       $("#deleteConfirmDialog").modal("show");
                                   }

                                   function deleteItem() {
                                       ConfigService.delete_item($rootScope.pageContext.appId,
                                                                 $rootScope.pageContext.env,
                                                                 $rootScope.pageContext.clusterName,
                                                                 toOperationNamespace.baseInfo.namespaceName,
                                                                 toDeleteItemId).then(
                                           function (result) {
                                               toastr.success("删除成功!");
                                               $rootScope.refreshNamespaces();
                                           }, function (result) {
                                               toastr.error(AppUtil.errorMsg(result), "删除失败");
                                           });
                                   }

                                   //修改配置
                                   function editItem(namespace, item) {
                                       if (!lockCheck(namespace)) {
                                           return;
                                       }
                                       switchTableViewOperType(TABLE_VIEW_OPER_TYPE.UPDATE);
                                       $scope.item = item;
                                       toOperationNamespace = namespace;

                                       $("#itemModal").modal("show");
                                   }

                                   //新增配置
                                   function createItem(namespace) {
                                       if (!lockCheck(namespace)) {
                                           return;
                                       }

                                       switchTableViewOperType(TABLE_VIEW_OPER_TYPE.CREATE);
                                       $scope.item = {};
                                       toOperationNamespace = namespace;
                                       $('#itemModal').modal('show');
                                   }

                                   var selectedClusters = [];
                                   $scope.collectSelectedClusters = function (data) {
                                       selectedClusters = data;
                                   };

                                   function switchTableViewOperType(type) {
                                       $scope.tableViewOperType = type;
                                   }

                                   var itemModal = $("#itemModal");

                                   function doItem() {

                                       if (selectedClusters.length == 0) {
                                           toastr.error("请选择集群");
                                       } else {
                                           if (!$scope.item.value) {
                                               $scope.item.value = "";
                                           }
                                           selectedClusters.forEach(function (cluster) {
                                               if ($scope.tableViewOperType == TABLE_VIEW_OPER_TYPE.CREATE) {
                                                   //check key unique
                                                   var hasRepeatKey = false;
                                                   toOperationNamespace.items.forEach(function (item) {
                                                       if (!item.isDeleted && $scope.item.key == item.item.key) {
                                                           toastr.error("key=" + $scope.item.key + " 已存在");
                                                           hasRepeatKey = true;
                                                           return;
                                                       }
                                                   });
                                                   if (hasRepeatKey) {
                                                       return;
                                                   }

                                                   $scope.addItemBtnDisabled = true;
                                                   ConfigService.create_item($rootScope.pageContext.appId,
                                                                             cluster.env,
                                                                             cluster.name,
                                                                             toOperationNamespace.baseInfo.namespaceName,
                                                                             $scope.item).then(
                                                       function (result) {
                                                           toastr.success(cluster.env + " , " + $scope.item.key,
                                                                          "添加成功");
                                                           itemModal.modal('hide');
                                                           $scope.addItemBtnDisabled = false;
                                                           $rootScope.refreshNamespaces(namespace_view_type.TABLE);
                                                       }, function (result) {
                                                           $scope.addItemBtnDisabled = false;
                                                           toastr.error(AppUtil.errorMsg(result), "添加失败");
                                                       });

                                               } else if ($scope.tableViewOperType == TABLE_VIEW_OPER_TYPE.UPDATE) {

                                                   if (!$scope.item.comment) {
                                                       $scope.item.comment = "";
                                                   }
                                                   ConfigService.update_item($rootScope.pageContext.appId,
                                                                             cluster.env,
                                                                             cluster.name,
                                                                             toOperationNamespace.baseInfo.namespaceName,
                                                                             $scope.item).then(
                                                       function (result) {
                                                           toastr.success("更新成功, 如需生效请发布");
                                                           itemModal.modal('hide');
                                                           $rootScope.refreshNamespaces(namespace_view_type.TABLE);
                                                       }, function (result) {
                                                           toastr.error(AppUtil.errorMsg(result), "更新失败");
                                                       });
                                               }
                                           });
                                       }

                                   }

                                   function lockCheck(namespace) {
                                       if (namespace.lockOwner && $scope.currentUser != namespace.lockOwner) {
                                           $scope.lockOwner = namespace.lockOwner;
                                           $('#namespaceLockedDialog').modal('show');
                                           return false;
                                       }
                                       return true;
                                   }

                                   $('.config-item-container').removeClass('hide');
                               }]);

