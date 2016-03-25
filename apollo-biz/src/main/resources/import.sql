INSERT INTO Cluster (AppId, IsDeleted, Name) VALUES (100, 0, 'default');
INSERT INTO Cluster (ID, AppId, IsDeleted, Name) VALUES (100, 6666, 0, 'default-cluster-name');
INSERT INTO Cluster (ID, AppId, IsDeleted, Name) VALUES (101, 6666, 0, 'cluster1');

INSERT INTO Version (AppId, IsDeleted, Name, ReleaseId) VALUES (101, 0, '1.0', 1);
INSERT INTO Version (AppId, IsDeleted, Name, ReleaseId) VALUES (102, 0, '1.0', 2);
INSERT INTO Version (ID, AppId, IsDeleted, Name, ReleaseId) VALUES (100, 6666, 0, '1.0', 11111);
INSERT INTO Version (ID, AppId, IsDeleted, Name, ReleaseId) VALUES (101, 6666, 0, '2.0', 11112);

INSERT INTO RELEASESNAPSHOT (ClusterName, IsDeleted, ReleaseId, Configurations) VALUES ('default', 0, 1, '{"apollo.foo":"bar", "apollo.bar":"foo"}');
INSERT INTO RELEASESNAPSHOT (ClusterName, IsDeleted, ReleaseId, Configurations) VALUES ('default', 0, 2, '{"demo.foo":"demo1", "demo.bar":"demo2"}');
INSERT INTO RELEASESNAPSHOT (ClusterName, IsDeleted, ReleaseId, Configurations) VALUES ('default-cluster-name', 0, 11111, '{"6666.foo":"demo1", "6666.bar":"demo2","3333.foo":"1008","4444.bar":"99901"}');
INSERT INTO RELEASESNAPSHOT (ClusterName, IsDeleted, ReleaseId, Configurations) VALUES ('cluster1', 0, 11111, '{"6666.foo":"demo1"}');
INSERT INTO RELEASESNAPSHOT (ClusterName, IsDeleted, ReleaseId, Configurations) VALUES ('cluster2', 0, 11111, '{"6666.bar":"bar2222"}');
INSERT INTO RELEASESNAPSHOT (ClusterName, IsDeleted, ReleaseId, Configurations) VALUES ('default-cluster-name', 0, 11112, '{"6666.foo":"verson2.0", "6666.bar":"verson2.0","3333.foo":"1008","4444.bar":"99901"}');

INSERT INTO ConfigItem(ClusterId, ClusterName, AppId, Key, Value, comment, dataChangeCreatedBy, dataChangeCreatedTime, dataChangeLastModifiedBy, dataChangeLastModifiedTime, IsDeleted) VALUES (100, 'default-cluster-name', 6666, '6666.k1', '6666.v1', 'comment1', 'lepdou', '2016-03-23 12:00:00', '王五', NOW(), 0);

INSERT INTO ConfigItem(ClusterId, ClusterName, AppId, Key, Value, comment, dataChangeCreatedBy, dataChangeCreatedTime, dataChangeLastModifiedBy, dataChangeLastModifiedTime, IsDeleted) VALUES (100, 'default-cluster-name', 6666, '6666.k2', '6666.v2', 'xxxx', 'lepdou', '2016-03-23 12:00:00', '王五1', NOW(),0);

INSERT INTO ConfigItem(ClusterId, ClusterName, AppId, Key, Value, comment, dataChangeCreatedBy, dataChangeCreatedTime, dataChangeLastModifiedBy, dataChangeLastModifiedTime, IsDeleted) VALUES (100, 'default-cluster-name', 6666, '6666.k3', '6666.v3', 'yyyy', 'lepdou', '2016-03-23 12:00:00', '王五2', NOW(),0);

INSERT INTO ConfigItem(ClusterId, ClusterName, AppId, Key, Value, comment, dataChangeCreatedBy, dataChangeCreatedTime, dataChangeLastModifiedBy, dataChangeLastModifiedTime, IsDeleted) VALUES (100, 'default-cluster-name', 5555, '5555.k1', '5555.v11', 'zzzz', 'lepdou', '2016-03-23 12:00:00', '王五3', NOW(),0);

INSERT INTO ConfigItem(ClusterId, ClusterName, AppId, Key, Value, comment, dataChangeCreatedBy, dataChangeCreatedTime, dataChangeLastModifiedBy, dataChangeLastModifiedTime, IsDeleted) VALUES (101, 'cluster1', 6666, '6666.k1', '6666.v122', 'qqqqq', 'lepdou', '2016-03-23 12:00:00', '王五4', NOW(),0);
INSERT INTO RELEASESNAPSHOT (ClusterName, IsDeleted, ReleaseId, Configurations) VALUES ('default', 0, 3, '{"apollo.foo":"another bar", "apollo.bar_new":"foo"}');
