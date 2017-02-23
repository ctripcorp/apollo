# delta schema to upgrade apollo config db from v0.4.0 to v0.5.0

Use ApolloConfigDB;

DROP TABLE `Privilege`;
ALTER TABLE `Release` DROP `Status`;
ALTER TABLE `Namespace` ADD KEY `IX_NamespaceName` (`NamespaceName`(191));
ALTER TABLE `Cluster` ADD KEY `IX_ParentClusterId` (`ParentClusterId`);
ALTER TABLE `AppNamespace` ADD KEY `IX_AppId` (`AppId`);
ALTER TABLE `App` DROP INDEX `Name`;
ALTER TABLE `App` ADD KEY `Name` (`Name`);