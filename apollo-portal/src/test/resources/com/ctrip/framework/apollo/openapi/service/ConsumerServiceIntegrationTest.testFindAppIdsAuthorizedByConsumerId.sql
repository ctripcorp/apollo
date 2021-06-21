--
-- Copyright 2021 Apollo Authors
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

/*
	This sql is dumped from a apollo portal database.

	The logic is as follows

	create app：
		consumer-test-app-id-0
		consumer-test-app-id-1
	    consumer-test-app-id-2

	create consumer:
		consumer-test-app-role

	Authorization, let consumer-test-app-role manage:
		consumer-test-app-id-0:
			Authorization type: App
		consumer-test-app-id-1:
			Authorization type: Namespace
			Managed Namespace: application
*/

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

/*!40000 ALTER TABLE `App` DISABLE KEYS */;
INSERT INTO `App` (`Id`, `AppId`, `Name`, `OrgId`, `OrgName`, `OwnerName`, `OwnerEmail`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(1, 'consumer-test-app-id-0', 'consumer-test-app-id-0', 'TEST1', '样例部门1', 'apollo', 'apollo@acme.com', 'apollo', 'apollo');
INSERT INTO `App` (`Id`, `AppId`, `Name`, `OrgId`, `OrgName`, `OwnerName`, `OwnerEmail`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(2, 'consumer-test-app-id-1', 'consumer-test-app-id-1', 'TEST2', '样例部门2', 'apollo', 'apollo@acme.com', 'apollo', 'apollo');
INSERT INTO `App` (`Id`, `AppId`, `Name`, `OrgId`, `OrgName`, `OwnerName`, `OwnerEmail`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(3, 'consumer-test-app-id-2', 'consumer-test-app-id-2', 'TEST2', '样例部门2', 'apollo', 'apollo@acme.com', 'apollo', 'apollo');
/*!40000 ALTER TABLE `App` ENABLE KEYS */;

/*!40000 ALTER TABLE `AppNamespace` DISABLE KEYS */;
INSERT INTO `AppNamespace` (`Id`, `Name`, `AppId`, `Format`, `Comment`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(1, 'application', 'consumer-test-app-id-0', 'properties', 'default app namespace', 'apollo', 'apollo');
INSERT INTO `AppNamespace` (`Id`, `Name`, `AppId`, `Format`, `Comment`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(2, 'application', 'consumer-test-app-id-1', 'properties', 'default app namespace', 'apollo', 'apollo');
INSERT INTO `AppNamespace` (`Id`, `Name`, `AppId`, `Format`, `Comment`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(3, 'application', 'consumer-test-app-id-2', 'properties', 'default app namespace', 'apollo', 'apollo');
/*!40000 ALTER TABLE `AppNamespace` ENABLE KEYS */;

/*!40000 ALTER TABLE `Consumer` DISABLE KEYS */;
INSERT INTO `Consumer` (`Id`, `AppId`, `Name`, `OrgId`, `OrgName`, `OwnerName`, `OwnerEmail`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(1, 'consumer-test-app-role', 'consumer-test-app-role', 'TEST2', '样例部门2', 'apollo', 'apollo@acme.com', 'apollo', 'apollo');
/*!40000 ALTER TABLE `Consumer` ENABLE KEYS */;

/*!40000 ALTER TABLE `ConsumerAudit` DISABLE KEYS */;
/*!40000 ALTER TABLE `ConsumerAudit` ENABLE KEYS */;

/*!40000 ALTER TABLE `ConsumerRole` DISABLE KEYS */;
INSERT INTO `ConsumerRole` (`Id`, `ConsumerId`, `RoleId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(1, 1, 1, 'apollo', 'apollo');
INSERT INTO `ConsumerRole` (`Id`, `ConsumerId`, `RoleId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(2, 1, 11, 'apollo', 'apollo');
INSERT INTO `ConsumerRole` (`Id`, `ConsumerId`, `RoleId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(3, 1, 12, 'apollo', 'apollo');
/*!40000 ALTER TABLE `ConsumerRole` ENABLE KEYS */;

/*!40000 ALTER TABLE `ConsumerToken` DISABLE KEYS */;
INSERT INTO `ConsumerToken` (`Id`, `ConsumerId`, `Token`, `Expires`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(1, 1, '3c16bf5b1f44b465179253442460e8c0ad845289', '2098-12-31 10:00:00', 'apollo', 'apollo');
/*!40000 ALTER TABLE `ConsumerToken` ENABLE KEYS */;

/*!40000 ALTER TABLE `Favorite` DISABLE KEYS */;
/*!40000 ALTER TABLE `Favorite` ENABLE KEYS */;

/*!40000 ALTER TABLE `Permission` DISABLE KEYS */;
INSERT INTO `Permission` (`Id`, `PermissionType`, `TargetId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(1, 'AssignRole', 'consumer-test-app-id-0', 'apollo', 'apollo');
INSERT INTO `Permission` (`Id`, `PermissionType`, `TargetId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(2, 'CreateNamespace', 'consumer-test-app-id-0', 'apollo', 'apollo');
INSERT INTO `Permission` (`Id`, `PermissionType`, `TargetId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(3, 'CreateCluster', 'consumer-test-app-id-0', 'apollo', 'apollo');
INSERT INTO `Permission` (`Id`, `PermissionType`, `TargetId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(4, 'ManageAppMaster', 'consumer-test-app-id-0', 'apollo', 'apollo');
INSERT INTO `Permission` (`Id`, `PermissionType`, `TargetId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(5, 'ModifyNamespace', 'consumer-test-app-id-0+application', 'apollo', 'apollo');
INSERT INTO `Permission` (`Id`, `PermissionType`, `TargetId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(6, 'ReleaseNamespace', 'consumer-test-app-id-0+application', 'apollo', 'apollo');
INSERT INTO `Permission` (`Id`, `PermissionType`, `TargetId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(7, 'ModifyNamespace', 'consumer-test-app-id-0+application+DEV', 'apollo', 'apollo');
INSERT INTO `Permission` (`Id`, `PermissionType`, `TargetId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(8, 'ReleaseNamespace', 'consumer-test-app-id-0+application+DEV', 'apollo', 'apollo');
INSERT INTO `Permission` (`Id`, `PermissionType`, `TargetId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(9, 'CreateNamespace', 'consumer-test-app-id-1', 'apollo', 'apollo');
INSERT INTO `Permission` (`Id`, `PermissionType`, `TargetId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(10, 'AssignRole', 'consumer-test-app-id-1', 'apollo', 'apollo');
INSERT INTO `Permission` (`Id`, `PermissionType`, `TargetId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(11, 'CreateCluster', 'consumer-test-app-id-1', 'apollo', 'apollo');
INSERT INTO `Permission` (`Id`, `PermissionType`, `TargetId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(12, 'ManageAppMaster', 'consumer-test-app-id-1', 'apollo', 'apollo');
INSERT INTO `Permission` (`Id`, `PermissionType`, `TargetId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(13, 'ModifyNamespace', 'consumer-test-app-id-1+application', 'apollo', 'apollo');
INSERT INTO `Permission` (`Id`, `PermissionType`, `TargetId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(14, 'ReleaseNamespace', 'consumer-test-app-id-1+application', 'apollo', 'apollo');
INSERT INTO `Permission` (`Id`, `PermissionType`, `TargetId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(15, 'ModifyNamespace', 'consumer-test-app-id-1+application+DEV', 'apollo', 'apollo');
INSERT INTO `Permission` (`Id`, `PermissionType`, `TargetId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(16, 'ReleaseNamespace', 'consumer-test-app-id-1+application+DEV', 'apollo', 'apollo');
INSERT INTO `Permission` (`Id`, `PermissionType`, `TargetId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(17, 'CreateCluster', 'consumer-test-app-id-2', 'apollo', 'apollo');
INSERT INTO `Permission` (`Id`, `PermissionType`, `TargetId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(18, 'AssignRole', 'consumer-test-app-id-2', 'apollo', 'apollo');
INSERT INTO `Permission` (`Id`, `PermissionType`, `TargetId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(19, 'CreateNamespace', 'consumer-test-app-id-2', 'apollo', 'apollo');
INSERT INTO `Permission` (`Id`, `PermissionType`, `TargetId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(20, 'ManageAppMaster', 'consumer-test-app-id-2', 'apollo', 'apollo');
INSERT INTO `Permission` (`Id`, `PermissionType`, `TargetId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(21, 'ModifyNamespace', 'consumer-test-app-id-2+application', 'apollo', 'apollo');
INSERT INTO `Permission` (`Id`, `PermissionType`, `TargetId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(22, 'ReleaseNamespace', 'consumer-test-app-id-2+application', 'apollo', 'apollo');
INSERT INTO `Permission` (`Id`, `PermissionType`, `TargetId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(23, 'ModifyNamespace', 'consumer-test-app-id-2+application+DEV', 'apollo', 'apollo');
INSERT INTO `Permission` (`Id`, `PermissionType`, `TargetId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(24, 'ReleaseNamespace', 'consumer-test-app-id-2+application+DEV', 'apollo', 'apollo');
/*!40000 ALTER TABLE `Permission` ENABLE KEYS */;

/*!40000 ALTER TABLE `Role` DISABLE KEYS */;
INSERT INTO `Role` (`Id`, `RoleName`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(1, 'Master+consumer-test-app-id-0', 'apollo', 'apollo');
INSERT INTO `Role` (`Id`, `RoleName`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(2, 'ManageAppMaster+consumer-test-app-id-0', 'apollo', 'apollo');
INSERT INTO `Role` (`Id`, `RoleName`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(3, 'ModifyNamespace+consumer-test-app-id-0+application', 'apollo', 'apollo');
INSERT INTO `Role` (`Id`, `RoleName`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(4, 'ReleaseNamespace+consumer-test-app-id-0+application', 'apollo', 'apollo');
INSERT INTO `Role` (`Id`, `RoleName`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(5, 'ModifyNamespace+consumer-test-app-id-0+application+DEV', 'apollo', 'apollo');
INSERT INTO `Role` (`Id`, `RoleName`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(6, 'ReleaseNamespace+consumer-test-app-id-0+application+DEV', 'apollo', 'apollo');
INSERT INTO `Role` (`Id`, `RoleName`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(7, 'Master+consumer-test-app-id-1', 'apollo', 'apollo');
INSERT INTO `Role` (`Id`, `RoleName`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(8, 'ManageAppMaster+consumer-test-app-id-1', 'apollo', 'apollo');
INSERT INTO `Role` (`Id`, `RoleName`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(9, 'ModifyNamespace+consumer-test-app-id-1+application', 'apollo', 'apollo');
INSERT INTO `Role` (`Id`, `RoleName`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(10, 'ReleaseNamespace+consumer-test-app-id-1+application', 'apollo', 'apollo');
INSERT INTO `Role` (`Id`, `RoleName`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(11, 'ModifyNamespace+consumer-test-app-id-1+application+DEV', 'apollo', 'apollo');
INSERT INTO `Role` (`Id`, `RoleName`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(12, 'ReleaseNamespace+consumer-test-app-id-1+application+DEV', 'apollo', 'apollo');
INSERT INTO `Role` (`Id`, `RoleName`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(13, 'Master+consumer-test-app-id-2', 'apollo', 'apollo');
INSERT INTO `Role` (`Id`, `RoleName`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(14, 'ManageAppMaster+consumer-test-app-id-2', 'apollo', 'apollo');
INSERT INTO `Role` (`Id`, `RoleName`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(15, 'ModifyNamespace+consumer-test-app-id-2+application', 'apollo', 'apollo');
INSERT INTO `Role` (`Id`, `RoleName`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(16, 'ReleaseNamespace+consumer-test-app-id-2+application', 'apollo', 'apollo');
INSERT INTO `Role` (`Id`, `RoleName`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(17, 'ModifyNamespace+consumer-test-app-id-2+application+DEV', 'apollo', 'apollo');
INSERT INTO `Role` (`Id`, `RoleName`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(18, 'ReleaseNamespace+consumer-test-app-id-2+application+DEV', 'apollo', 'apollo');
/*!40000 ALTER TABLE `Role` ENABLE KEYS */;

/*!40000 ALTER TABLE `RolePermission` DISABLE KEYS */;
INSERT INTO `RolePermission` (`Id`, `RoleId`, `PermissionId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(1, 1, 1, 'apollo', 'apollo');
INSERT INTO `RolePermission` (`Id`, `RoleId`, `PermissionId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(2, 1, 2, 'apollo', 'apollo');
INSERT INTO `RolePermission` (`Id`, `RoleId`, `PermissionId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(3, 1, 3, 'apollo', 'apollo');
INSERT INTO `RolePermission` (`Id`, `RoleId`, `PermissionId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(4, 2, 4, 'apollo', 'apollo');
INSERT INTO `RolePermission` (`Id`, `RoleId`, `PermissionId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(5, 3, 5, 'apollo', 'apollo');
INSERT INTO `RolePermission` (`Id`, `RoleId`, `PermissionId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(6, 4, 6, 'apollo', 'apollo');
INSERT INTO `RolePermission` (`Id`, `RoleId`, `PermissionId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(7, 5, 7, 'apollo', 'apollo');
INSERT INTO `RolePermission` (`Id`, `RoleId`, `PermissionId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(8, 6, 8, 'apollo', 'apollo');
INSERT INTO `RolePermission` (`Id`, `RoleId`, `PermissionId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(9, 7, 9, 'apollo', 'apollo');
INSERT INTO `RolePermission` (`Id`, `RoleId`, `PermissionId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(10, 7, 10, 'apollo', 'apollo');
INSERT INTO `RolePermission` (`Id`, `RoleId`, `PermissionId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(11, 7, 11, 'apollo', 'apollo');
INSERT INTO `RolePermission` (`Id`, `RoleId`, `PermissionId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(12, 8, 12, 'apollo', 'apollo');
INSERT INTO `RolePermission` (`Id`, `RoleId`, `PermissionId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(13, 9, 13, 'apollo', 'apollo');
INSERT INTO `RolePermission` (`Id`, `RoleId`, `PermissionId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(14, 10, 14, 'apollo', 'apollo');
INSERT INTO `RolePermission` (`Id`, `RoleId`, `PermissionId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(15, 11, 15, 'apollo', 'apollo');
INSERT INTO `RolePermission` (`Id`, `RoleId`, `PermissionId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(16, 12, 16, 'apollo', 'apollo');
INSERT INTO `RolePermission` (`Id`, `RoleId`, `PermissionId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(17, 13, 17, 'apollo', 'apollo');
INSERT INTO `RolePermission` (`Id`, `RoleId`, `PermissionId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(18, 13, 18, 'apollo', 'apollo');
INSERT INTO `RolePermission` (`Id`, `RoleId`, `PermissionId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(19, 13, 19, 'apollo', 'apollo');
INSERT INTO `RolePermission` (`Id`, `RoleId`, `PermissionId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(20, 14, 20, 'apollo', 'apollo');
INSERT INTO `RolePermission` (`Id`, `RoleId`, `PermissionId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(21, 15, 21, 'apollo', 'apollo');
INSERT INTO `RolePermission` (`Id`, `RoleId`, `PermissionId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(22, 16, 22, 'apollo', 'apollo');
INSERT INTO `RolePermission` (`Id`, `RoleId`, `PermissionId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(23, 17, 23, 'apollo', 'apollo');
INSERT INTO `RolePermission` (`Id`, `RoleId`, `PermissionId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(24, 18, 24, 'apollo', 'apollo');
/*!40000 ALTER TABLE `RolePermission` ENABLE KEYS */;

/*!40000 ALTER TABLE `UserRole` DISABLE KEYS */;
INSERT INTO `UserRole` (`Id`, `UserId`, `RoleId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(1, 'apollo', 1, 'apollo', 'apollo');
INSERT INTO `UserRole` (`Id`, `UserId`, `RoleId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(2, 'apollo', 3, 'apollo', 'apollo');
INSERT INTO `UserRole` (`Id`, `UserId`, `RoleId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(3, 'apollo', 4, 'apollo', 'apollo');
INSERT INTO `UserRole` (`Id`, `UserId`, `RoleId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(4, 'apollo', 7, 'apollo', 'apollo');
INSERT INTO `UserRole` (`Id`, `UserId`, `RoleId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(5, 'apollo', 9, 'apollo', 'apollo');
INSERT INTO `UserRole` (`Id`, `UserId`, `RoleId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(6, 'apollo', 10, 'apollo', 'apollo');
INSERT INTO `UserRole` (`Id`, `UserId`, `RoleId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(7, 'apollo', 13, 'apollo', 'apollo');
INSERT INTO `UserRole` (`Id`, `UserId`, `RoleId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(8, 'apollo', 15, 'apollo', 'apollo');
INSERT INTO `UserRole` (`Id`, `UserId`, `RoleId`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`) VALUES
(9, 'apollo', 16, 'apollo', 'apollo');
/*!40000 ALTER TABLE `UserRole` ENABLE KEYS */;

/*!40000 ALTER TABLE `Users` DISABLE KEYS */;
INSERT INTO `Users` (`Id`, `Username`, `Password`, `UserDisplayName`, `Email`, `Enabled`) VALUES
(1, 'apollo', '$2a$10$7r20uS.BQ9uBpf3Baj3uQOZvMVvB1RN3PYoKE94gtz2.WAOuiiwXS', 'apollo', 'apollo@acme.com', 1);
/*!40000 ALTER TABLE `Users` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
