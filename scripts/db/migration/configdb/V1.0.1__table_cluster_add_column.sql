Use ApolloConfigDB;

ALTER TABLE `Cluster`
ADD COLUMN `AssociateClusterId`  int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '关联cluster' AFTER `ParentClusterId`;
ALTER TABLE `Cluster` ADD KEY `IX_AssociateClusterId` (`AssociateClusterId`);