INSERT INTO App (AppId, Name, OwnerName, OwnerEmail) VALUES ('someAppId','someAppName','someOwnerName','someOwnerName@ctrip.com');

INSERT INTO Cluster (AppId, Name) VALUES ('someAppId', 'default');

INSERT INTO AppNamespace (AppId, Name) VALUES ('someAppId', 'application');

INSERT INTO Namespace (Id, AppId, ClusterName, NamespaceName) VALUES (100, 'someAppId', 'default', 'application');

INSERT INTO Item (NamespaceId, `Key`, Value, Comment) VALUES (100, 'k1', 'v1', 'comment1');
INSERT INTO Item (NamespaceId, `Key`, Value, Comment) VALUES (100, 'k2', 'v2', 'comment1');
INSERT INTO Item (NamespaceId, `Key`, Value, Comment) VALUES (100, 'k3', 'v3', 'comment1');