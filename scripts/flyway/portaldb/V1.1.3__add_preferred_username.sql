Use ApolloPortalDB;

ALTER TABLE `Users`
    MODIFY COLUMN `Username` varchar(64) NOT NULL DEFAULT 'default' COMMENT '用户登录账户',
    ADD COLUMN `UserDisplayName` varchar(512) NOT NULL DEFAULT 'default' COMMENT '用户名称' AFTER `Password`;
UPDATE `Users` SET `UserDisplayName`=`Username` WHERE `UserDisplayName` = 'default';
