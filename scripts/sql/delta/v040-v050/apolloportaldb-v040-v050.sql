# delta schema to upgrade apollo portal db from v0.4.0 to v0.5.0

Use ApolloPortalDB;

ALTER TABLE `AppNamespace` ADD KEY `IX_AppId` (`AppId`);
ALTER TABLE `App` DROP INDEX `Name`;
ALTER TABLE `App` ADD KEY `Name` (`Name`);