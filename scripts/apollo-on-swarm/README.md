# 基于swarm集群部署, 不依赖Eureka

这里我们采用挨个服务部署的方式进行，不统一yml文件

另这里只是给出了具体实现路径，细微调整需要根据项目做出调整，疑问也可以联系sunwei@aibeb.com

如果已经在swarm集群中请跳过这一步，如果单纯docker环境请用如下命令开启swarm集群

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

docker volume create apollo-mysql

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
# 注意apollo_config-service_url更换为对外地址接口
docker service create \
  --name apollo-configservice \
  --replicas 1 \
  --restart-condition on-failure \
  --env SPRING_DATASOURCE_URL=jdbc:mysql://apollo-mysql:3306/ApolloConfigDB?characterEncoding=utf8 \
  --env SPRING_DATASOURCE_USERNAME=root \
  --env SPRING_DATASOURCE_PASSWORD= \
  --env SPRING_PROFILES_ACTIVE=github,kubernetes \
  --env apollo_config-service_url=http://localhost:8080 \
  --env apollo_admin-service_url=http://apollo-adminservice:8090 \
  --mount type=bind,source=$(pwd)/application-github.properties,destination=/apollo-configservice/config/application-github.properties \
  --network apollo \
  --publish 8080:8080 \
  apolloconfig/apollo-configservice
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
  --network apollo \
  --publish 8090 \
  apolloconfig/apollo-adminservice
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
  --network apollo \
  --publish 8070:8070 \
  apolloconfig/apollo-portal
```

或者通过docker stack
```shell
docker stack deploy -c ./docker-compose.yml apollo
```