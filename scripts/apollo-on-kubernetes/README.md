# 使用方法

## 一、构建镜像

### 1.1 获取 apollo 压缩包
从 https://github.com/ctripcorp/apollo/releases 下载预先打好的 java 包 <br/>
例如你下载的是: <br/>
apollo-portal-1.0.0-github.zip <br/>
apollo-adminservice-1.0.0-github.zip <br/>
apollo-configservice-1.0.0-github.zip <br/>

### 1.2 解压压缩包, 获取程序 jar 包
- 解压 apollo-portal-1.0.0-github.zip <br/>
获取 apollo-portal-1.0.0.jar, 重命名为 apollo-portal.jar, 放到 scripts/apollo-on-kubernetes/apollo-portal-server
- 解压 apollo-adminservice-1.0.0-github.zip <br/>
获取 apollo-adminservice-1.0.0.jar, 重命名为 apollo-adminservice.jar, 放到 scripts/apollo-on-kubernetes/apollo-admin-server
- 解压 apollo-configservice-1.0.0-github.zip <br/>
获取 apollo-configservice-1.0.0.jar, 重命名为 apollo-configservice.jar, 放到 scripts/apollo-on-kubernetes/apollo-config-server

### 1.3 build image
需要分别为alpine-bash-3.8-image，apollo-config-server，apollo-admin-server和apollo-portal-server构建镜像。

以 build apollo-config-server image 为例, 其他类似

```bash
scripts/apollo-on-kubernetes/apollo-config-server$ tree -L 2
.
├── apollo-configservice.conf
├── apollo-configservice.jar
├── config
│   ├── application-github.properties
│   └── app.properties
├── Dockerfile
├── entrypoint.sh
└── scripts
    └── startup-kubernetes.sh
```

build image

```bash
# 在 scripts/apollo-on-kubernetes/apollo-config-server 路径下
docker build -t apollo-config-server:v1.0.0 .
```

push image <br/>
将 image push 到你的 docker registry, 例如 vmware harbor

## 二、Deploy apollo on kubernetes

### 2.1 部署 MySQL 服务
你可以选用 MySQL-Galera-WSrep 来提高你的 MySQL 服务的可用性 <br/>
MySQL 部署步骤略

### 2.1 导入 MySQL DB 文件
由于上面部署了分布式的 MySQL, 所有 config-server、admin-server、portal-server 使用同一个 MySQL 服务的不同数据库

示例假设你的 apollo 开启了 4 个环境, 即 dev、test-alpha、test-beta、prod, 在你的 MySQL 中导入 scripts/apollo-on-kubernetes/db 下的文件即可

如果有需要, 你可以更改 eureka.service.url 的地址, 格式为 http://config-server-pod-name-index.meta-server-service-name:8080/eureka/ , 例如 http://statefulset-apollo-config-server-dev-0.service-apollo-meta-server-dev:8080/eureka/

### 2.2 Deploy apollo on kubernetes

示例假设你有 4 台 kubernetes node 来部署 apollo, apollo 开启了 4 个环境, 即 dev、test-alpha、test-beta、prod

按照 scripts/apollo-on-kubernetes/kubernetes/kubectl-apply.sh 文件的内容部署 apollo 即可，注意需要按照实际情况修改对应配置文件中的数据库连接信息、eureka.service.url、replicas、nodeSelector、镜像信息等。

```bash
scripts/apollo-on-kubernetes/kubernetes$ cat kubectl-apply.sh
# create namespace
kubectl create namespace sre

# dev-env
kubectl apply -f apollo-env-dev/service-mysql-for-apollo-dev-env.yaml --record && \
kubectl apply -f apollo-env-dev/service-apollo-config-server-dev.yaml --record && \
kubectl apply -f apollo-env-dev/service-apollo-admin-server-dev.yaml --record

# fat-env(test-alpha-env)
kubectl apply -f apollo-env-test-alpha/service-mysql-for-apollo-test-alpha-env.yaml --record && \
kubectl apply -f apollo-env-test-alpha/service-apollo-config-server-test-alpha.yaml --record && \
kubectl apply -f apollo-env-test-alpha/service-apollo-admin-server-test-alpha.yaml --record

# uat-env(test-beta-env)
kubectl apply -f apollo-env-test-beta/service-mysql-for-apollo-test-beta-env.yaml --record && \
kubectl apply -f apollo-env-test-beta/service-apollo-config-server-test-beta.yaml --record && \
kubectl apply -f apollo-env-test-beta/service-apollo-admin-server-test-beta.yaml --record

# prod-env
kubectl apply -f apollo-env-prod/service-mysql-for-apollo-prod-env.yaml --record && \
kubectl apply -f apollo-env-prod/service-apollo-config-server-prod.yaml --record && \
kubectl apply -f apollo-env-prod/service-apollo-admin-server-prod.yaml --record

# portal
kubectl apply -f service-apollo-portal-server.yaml --record
```

~~你需要注意的是, 应当尽量让同一个 server 的不同 pod 在不同 node 上, 这个通过 kubernetes nodeSelector 实现~~
去掉nodeSelector 改为POD反亲和性[podAntiAffinity](https://kubernetes.io/docs/concepts/configuration/assign-pod-node/)

### 2.3 验证所有 pod 处于 Running 并且 READY 状态

```bash
kubectl get pod -n sre -o wide

# 示例结果
NAME                                                        READY     STATUS    RESTARTS   AGE       IP            NODE
deployment-apollo-admin-server-dev-b7bbd657-4d5jx           1/1       Running   0          2d        10.247.4.79   k8s-apollo-node-2
deployment-apollo-admin-server-dev-b7bbd657-lwz5x           1/1       Running   0          2d        10.247.8.7    k8s-apollo-node-3
deployment-apollo-admin-server-dev-b7bbd657-xs4wt           1/1       Running   0          2d        10.247.1.23   k8s-apollo-node-1
deployment-apollo-admin-server-prod-699bbd894f-j977p        1/1       Running   0          2d        10.247.4.83   k8s-apollo-node-2
deployment-apollo-admin-server-prod-699bbd894f-n9m54        1/1       Running   0          2d        10.247.8.11   k8s-apollo-node-3
deployment-apollo-admin-server-prod-699bbd894f-vs56w        1/1       Running   0          2d        10.247.1.27   k8s-apollo-node-1
deployment-apollo-admin-server-test-beta-7c855cd4f5-9br65   1/1       Running   0          2d        10.247.1.25   k8s-apollo-node-1
deployment-apollo-admin-server-test-beta-7c855cd4f5-cck5g   1/1       Running   0          2d        10.247.8.9    k8s-apollo-node-3
deployment-apollo-admin-server-test-beta-7c855cd4f5-x6gt4   1/1       Running   0          2d        10.247.4.81   k8s-apollo-node-2
deployment-apollo-portal-server-6d4bbc879c-bv7cn            1/1       Running   0          2d        10.247.8.12   k8s-apollo-node-3
deployment-apollo-portal-server-6d4bbc879c-c4zrb            1/1       Running   0          2d        10.247.1.28   k8s-apollo-node-1
deployment-apollo-portal-server-6d4bbc879c-qm4mn            1/1       Running   0          2d        10.247.4.84   k8s-apollo-node-2
statefulset-apollo-config-server-dev-0                      1/1       Running   0          2d        10.247.8.6    k8s-apollo-node-3
statefulset-apollo-config-server-dev-1                      1/1       Running   0          2d        10.247.4.78   k8s-apollo-node-2
statefulset-apollo-config-server-dev-2                      1/1       Running   0          2d        10.247.1.22   k8s-apollo-node-1
statefulset-apollo-config-server-prod-0                     1/1       Running   0          2d        10.247.8.10   k8s-apollo-node-3
statefulset-apollo-config-server-prod-1                     1/1       Running   0          2d        10.247.4.82   k8s-apollo-node-2
statefulset-apollo-config-server-prod-2                     1/1       Running   0          2d        10.247.1.26   k8s-apollo-node-1
statefulset-apollo-config-server-test-beta-0                1/1       Running   0          2d        10.247.8.8    k8s-apollo-node-3
statefulset-apollo-config-server-test-beta-1                1/1       Running   0          2d        10.247.4.80   k8s-apollo-node-2
statefulset-apollo-config-server-test-beta-2                1/1       Running   0          2d        10.247.1.24   k8s-apollo-node-1
```

### 2.4 访问 apollo service

- server 端(即 portal) <br/>
&nbsp;&nbsp;&nbsp;&nbsp;kubernetes-master-ip:30001

- client 端, 在 client 端无需再实现负载均衡 <br/>
Dev<br/>
&nbsp;&nbsp;&nbsp;&nbsp;kubernetes-master-ip:30002 <br/>
Test-Alpha <br/>
&nbsp;&nbsp;&nbsp;&nbsp;kubernetes-master-ip:30003 <br/>
Test-Beta <br/>
&nbsp;&nbsp;&nbsp;&nbsp;kubernetes-master-ip:30004 <br/>
Prod <br/>
&nbsp;&nbsp;&nbsp;&nbsp;kubernetes-master-ip:30005 <br/>

# FAQ

## 关于修改的 Dockerfile
添加 ENV 和 entrypoint.sh, 启动 server 需要的配置, 在 kubernetes yaml 文件中可以通过 configmap 传入、也可以通过 ENV 传入
关于 Dockerfile、entrypoint.sh 具体内容, 你可以查看文件里的内容

## 关于 kubernetes yaml 文件
具体内容请查看 `scripts/apollo-on-kubernetes/kubernetes/service-apollo-portal-server.yaml` 注释 <br/>
其他类似。

## 关于 eureka.service.url
使用 meta-server(即 config-server) 的 pod name, config-server 务必使用 statefulset。
格式为：`http://<config server pod名>.<meta server 服务名>:<meta server端口号>/eureka/`。

以 apollo-env-dev 为例:
```bash
('eureka.service.url', 'default', 'http://statefulset-apollo-config-server-dev-0.service-apollo-meta-server-dev:8080/eureka/,http://statefulset-apollo-config-server-dev-1.service-apollo-meta-server-dev:8080/eureka/,http://statefulset-apollo-config-server-dev-2.service-apollo-meta-server-dev:8080/eureka/', 'Eureka服务Url，多个service以英文逗号分隔')
```
你可以精简 config-server pod 的 name, 示例的长名字是为了更好的阅读与理解。

### 方式一：通过Spring Boot文件 application-github.properties配置（推荐）
推荐此方式配置 `eureka.service.url`，因为可以通过ConfigMap的方式传入容器，无需再修改数据库的字段。

Admin Server的配置：
```yaml
---
# configmap for apollo-admin-server-dev
kind: ConfigMap
apiVersion: v1
metadata:
  namespace: sre
  name: configmap-apollo-admin-server-dev
data:
  application-github.properties: |
    spring.datasource.url = jdbc:mysql://service-mysql-for-apollo-dev-env-mariadb.sre:3306/DevApolloConfigDB?characterEncoding=utf8
    spring.datasource.username = root
    spring.datasource.password = test
    eureka.service.url = http://statefulset-apollo-config-server-dev-0.service-apollo-meta-server-dev:8080/eureka/,http://statefulset-apollo-config-server-dev-1.service-apollo-meta-server-dev:8080/eureka/,http://statefulset-apollo-config-server-dev-2.service-apollo-meta-server-dev:8080/eureka/

```

Config Server的配置：
```yaml
---
# configmap for apollo-config-server-dev
kind: ConfigMap
apiVersion: v1
metadata:
  namespace: sre
  name: configmap-apollo-config-server-dev
data:
  application-github.properties: |
    spring.datasource.url = jdbc:mysql://service-mysql-for-apollo-dev-env-mariadb.sre:3306/DevApolloConfigDB?characterEncoding=utf8
    spring.datasource.username = root
    spring.datasource.password = m6bCdQXa00
    eureka.service.url = http://statefulset-apollo-config-server-dev-0.service-apollo-meta-server-dev:8080/eureka/,http://statefulset-apollo-config-server-dev-1.service-apollo-meta-server-dev:8080/eureka/,http://statefulset-apollo-config-server-dev-2.service-apollo-meta-server-dev:8080/eureka/

```

### 方式二：修改数据表 ApolloConfigDB.ServerConfig
修改数据库表 ApolloConfigDB.ServerConfig的 eureka.service.url。
