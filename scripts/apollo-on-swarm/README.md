# 基于swarm集群部署, 不依赖Eureka

## 手动部署-感知部署过程

这里只是给出了具体实现路径，细微调整需要根据项目做出调整，疑问也可以联系sunwei@aibeb.com

注意:
* 未开启swarm集群请手动开启
* 可以通过修改.env中的${VERSION}来改变镜像版本


```shell
docker swarm init
```

进入目录scripts/apollo-on-swarm

```shell
cd scripts/apollo-on-swarm
```

为我们的apollo服务创建一个overlay网络: apollo

```shell
docker network create -d overlay --attachable apollo
```

创建数据库，如果你已经有了数据库可以跳过这一步
```shell

docker service create \
  --name apollo-mysql \
  --replicas 1 \
  --restart-condition on-failure \
  --env TZ=Asia/Shanghai \
  --env MYSQL_ALLOW_EMPTY_PASSWORD='yes' \
  --mount type=bind,source=$(pwd)/../docker-quick-start/sql,destination=/docker-entrypoint-initdb.d \
  --mount type=volume,source=apollo-mysql,destination=/var/lib/mysql \
  --network apollo \
  --publish 3306:3306 \
  --stop-grace-period 3m \
  mysql:5.7

```

创建apollo-configservice服务

```shell
# 注意密码更换为数据库密码
# 注意通过修改.env文件中APOLLO_CONFIG_SERVICE_HOST更换为集群所处宿主机ip地址，保证外部客户端访问
docker service create \
  --name apollo-configservice \
  --replicas 1 \
  --restart-condition on-failure \
  --env SPRING_DATASOURCE_URL=jdbc:mysql://apollo-mysql:3306/ApolloConfigDB?characterEncoding=utf8 \
  --env SPRING_DATASOURCE_USERNAME=root \
  --env SPRING_DATASOURCE_PASSWORD= \
  --env SPRING_PROFILES_ACTIVE=github,kubernetes \
  --env APOLLO_CONFIG_SERVICE_URL=http://${APOLLO_CONFIG_SERVICE_HOST}:8080 \
  --env APOLLO_ADMIN_SERVICE_URL=http://apollo-adminservice:8090 \
  --env-file ./.env
  --network apollo \
  --publish 8080:8080 \
  apolloconfig/apollo-configservice:${VERSION}
```

创建apollo-adminservice服务

```shell
docker service create \
  --name apollo-adminservice \
  --replicas 1 \
  --restart-condition on-failure \
  --env SPRING_DATASOURCE_URL=jdbc:mysql://apollo-mysql:3306/ApolloConfigDB?characterEncoding=utf8 \
  --env SPRING_DATASOURCE_USERNAME=root \
  --env SPRING_DATASOURCE_PASSWORD= \
  --env SPRING_PROFILES_ACTIVE=github,kubernetes \
  --env-file ./.env
  --network apollo \
  apolloconfig/apollo-adminservice:${VERSION}
```

创建apollo-portal服务

```shell
docker service create \
  --name apollo-portal \
  --replicas 1 \
  --restart-condition on-failure \
  --env SPRING_DATASOURCE_URL=jdbc:mysql://apollo-mysql:3306/ApolloPortalDB?characterEncoding=utf8 \
  --env SPRING_DATASOURCE_USERNAME=root \
  --env SPRING_DATASOURCE_PASSWORD= \
  --env DEV_META=http://apollo-configservice:8080 \
  --env-file ./.env
  --network apollo \
  --publish 8070:8070 \
  apolloconfig/apollo-portal:${VERSION}
```

## 通过docker-compose.yaml部署

```shell
# 注意文件中 APOLLO.CONFIG-SERVICE.URL更换为集群所处宿主机ip地址，保证外部客户端访问
docker stack deploy -c ./docker-compose.yaml apollo
```
