# Apollo Helm Chart

[Apollo](https://github.com/ctripcorp/apollo) is a reliable configuration management system.

## 1. Introduction

The apollo-service and apollo-portal charts create deployments for apollo-configservice, apollo-adminservice and apollo-portal, which utilize the kubernetes native service discovery.

## 2. Prerequisites

- Kubernetes 1.10+
- Helm 3

## 3. Add Apollo Helm Chart Repository

```bash
$ helm repo add apollo http://ctripcorp.github.io/apollo/charts
$ helm search repo apollo
```

## 4. Deployments of apollo-configservice and apollo-adminservice

### 4.1 Installation

apollo-configservice and apollo-adminservice should be installed per environment, so it is suggested to indicate environment in the release name, e.g. `apollo-service-dev`

```bash
$ helm install apollo-service-dev \
    --set configdb.host=1.2.3.4 \
    --set configdb.userName=apollo \
    --set configdb.password=apollo \
    --set configdb.service.enabled=true \
    --set configService.replicaCount=1 \
    --set adminService.replicaCount=1 \
    apollo/apollo-service
```

Or customize it with values.yaml

```bash
$ helm install apollo-service-dev -f values.yaml apollo/apollo-service 
```

### 4.2 Uninstallation

To uninstall/delete the `apollo-service-dev` deployment:

```bash
$ helm uninstall apollo-service-dev
```

### 4.3 Configuration

The following table lists the configurable parameters of the apollo-service chart and their default values.

| Parameter            | Description                                 | Default             |
|----------------------|---------------------------------------------|---------------------|
| `configdb.host` | The host for apollo config db | `nil` |
| `configdb.port` | The port for apollo config db | `3306` |
| `configdb.dbName` | The database name for apollo config db | `ApolloConfigDB` |
| `configdb.userName` | The user name for apollo config db | `nil` |
| `configdb.password` | The password for apollo config db | `nil` |
| `configdb.connectionStringProperties` | The connection string properties for apollo config db | `characterEncoding=utf8` |
| `configdb.service.enabled` | Whether to create a Kubernetes Service for `configdb.host` or not. Set it to `true` if `configdb.host` is an endpoint outside of the kubernetes cluster | `false` |
| `configdb.service.fullNameOverride` | Override the service name for apollo config db | `nil` |
| `configdb.service.port` | The port for the service of apollo config db | `3306` |
| `configdb.service.type` | The service type of apollo config db: `ClusterIP` or `ExternalName` | `ClusterIP` |
| `configService.fullNameOverride` | Override the deployment name for apollo-configservice | `nil` |
| `configService.replicaCount` | Replica count of apollo-configservice | `2` |
| `configService.containerPort` | Container port of apollo-configservice | `8080` |
| `configService.image.repository` | Image repository of apollo-configservice | `apolloconfig/apollo-configservice` |
| `configService.image.pullPolicy`                | Image pull policy of apollo-configservice | `IfNotPresent` |
| `configService.imagePullSecrets`                | Image pull secrets of apollo-configservice | `[]` |
| `configService.service.fullNameOverride` | Override the service name for apollo-configservice | `nil` |
| `configService.service.port` | The port for the service of apollo-configservice | `8080` |
| `configService.service.targetPort` | The target port for the service of apollo-configservice | `8080` |
| `configService.service.type` | The service type of apollo-configservice                     | `ClusterIP` |
| `configService.liveness.initialDelaySeconds` | The initial delay seconds of liveness probe | `100` |
| `configService.liveness.periodSeconds` | The period seconds of liveness probe | `10` |
| `configService.readiness.initialDelaySeconds` | The initial delay seconds of readiness probe | `30` |
| `configService.readiness.periodSeconds` | The period seconds of readiness probe | `5` |
| `configService.config.configServiceUrlOverride` | Override `apollo.config-service.url`: config service url to be accessed by apollo-client | `nil` |
| `configService.config.adminServiceUrlOverride` | Override `apollo.admin-service.url`: admin service url to be accessed by apollo-portal | `nil` |
| `configService.env` | Environment variables passed to the container, e.g. <br />`JAVA_OPTS: -Xss256k` | `{}` |
| `configService.strategy` | The deployment strategy of apollo-configservice | `{}` |
| `configService.resources` | The resources definition of apollo-configservice | `{}` |
| `configService.nodeSelector` | The node selector definition of apollo-configservice | `{}` |
| `configService.tolerations` | The tolerations definition of apollo-configservice | `[]` |
| `configService.affinity` | The affinity definition of apollo-configservice | `{}` |
| `adminService.fullNameOverride` | Override the deployment name for apollo-adminservice | `nil` |
| `adminService.replicaCount` | Replica count of apollo-adminservice | `2` |
| `adminService.containerPort` | Container port of apollo-adminservice | `8090` |
| `adminService.image.repository` | Image repository of apollo-adminservice | `apolloconfig/apollo-adminservice` |
| `adminService.image.pullPolicy`                | Image pull policy of apollo-adminservice | `IfNotPresent` |
| `adminService.imagePullSecrets`                | Image pull secrets of apollo-adminservice | `[]` |
| `adminService.service.fullNameOverride` | Override the service name for apollo-adminservice | `nil` |
| `adminService.service.port` | The port for the service of apollo-adminservice | `8090` |
| `adminService.service.targetPort` | The target port for the service of apollo-adminservice | `8090` |
| `adminService.service.type` | The service type of apollo-adminservice                     | `ClusterIP` |
| `adminService.liveness.initialDelaySeconds` | The initial delay seconds of liveness probe | `100` |
| `adminService.liveness.periodSeconds` | The period seconds of liveness probe | `10` |
| `adminService.readiness.initialDelaySeconds` | The initial delay seconds of readiness probe | `30` |
| `adminService.readiness.periodSeconds` | The period seconds of readiness probe | `5` |
| `adminService.env` | Environment variables passed to the container, e.g. <br />`JAVA_OPTS: -Xss256k` | `{}` |
| `adminService.strategy` | The deployment strategy of apollo-adminservice | `{}` |
| `adminService.resources` | The resources definition of apollo-adminservice | `{}` |
| `adminService.nodeSelector` | The node selector definition of apollo-adminservice | `{}` |
| `adminService.tolerations` | The tolerations definition of apollo-adminservice | `[]` |
| `adminService.affinity` | The affinity definition of apollo-adminservice | `{}` |

## 5. Deployments of apollo-portal

### 5.1 Installation

To install the apollo-portal chart with the release name `apollo-portal`:

```bash
$ helm install apollo-portal \
    --set portaldb.host=1.2.3.4 \
    --set portaldb.userName=apollo \
    --set portaldb.password=apollo \
    --set portaldb.service.enabled=true \
    --set config.envs="dev\,pro" \
    --set config.metaServers.dev=http://apollo-service-dev-apollo-configservice:8080 \
    --set config.metaServers.pro=http://apollo-service-pro-apollo-configservice:8080 \
    --set replicaCount=1 \
    apollo/apollo-portal
```

Or customize it with values.yaml

```bash
$ helm install apollo-portal -f values.yaml apollo/apollo-portal 
```

### 5.2 Uninstallation

To uninstall/delete the `apollo-portal` deployment:

```bash
$ helm uninstall apollo-portal
```

### 5.3 Configuration

The following table lists the configurable parameters of the apollo-portal chart and their default values.

| Parameter            | Description                                 | Default               |
|----------------------|---------------------------------------------|-----------------------|
| `fullNameOverride` | Override the deployment name for apollo-portal | `nil` |
| `replicaCount` | Replica count of apollo-portal | `2` |
| `containerPort` | Container port of apollo-portal | `8070` |
| `image.repository` | Image repository of apollo-portal | `apolloconfig/apollo-portal` |
| `image.pullPolicy`                | Image pull policy of apollo-portal | `IfNotPresent` |
| `imagePullSecrets`                | Image pull secrets of apollo-portal | `[]` |
| `service.fullNameOverride` | Override the service name for apollo-portal | `nil` |
| `service.port` | The port for the service of apollo-portal | `8070` |
| `service.targetPort` | The target port for the service of apollo-portal | `8070` |
| `service.type` | The service type of apollo-portal                     | `ClusterIP` |
| `service.sessionAffinity` | The session affinity for the service of apollo-portal | `ClientIP` |
| `ingress.enabled` | Whether to enable the ingress or not | `false` |
| `ingress.annotations` | The annotations of the ingress | `{}` |
| `ingress.hosts.host` | The host of the ingress | `nil` |
| `ingress.hosts.paths` | The paths of the ingress | `[]` |
| `ingress.tls` | The tls definition of the ingress | `[]` |
| `liveness.initialDelaySeconds` | The initial delay seconds of liveness probe | `100` |
| `liveness.periodSeconds` | The period seconds of liveness probe | `10` |
| `readiness.initialDelaySeconds` | The initial delay seconds of readiness probe | `30` |
| `readiness.periodSeconds` | The period seconds of readiness probe | `5` |
| `env` | Environment variables passed to the container, e.g. <br />`JAVA_OPTS: -Xss256k` | `{}` |
| `strategy` | The deployment strategy of apollo-portal | `{}` |
| `resources` | The resources definition of apollo-portal | `{}` |
| `nodeSelector` | The node selector definition of apollo-portal | `{}` |
| `tolerations` | The tolerations definition of apollo-portal | `[]` |
| `affinity` | The affinity definition of apollo-portal | `{}` |
| `config.envs` | specify the env names, e.g. dev,pro | `nil` |
| `config.metaServers` | specify the meta servers, e.g.<br />`dev: http://apollo-configservice-dev:8080`<br />`pro: http://apollo-configservice-pro:8080` | `{}` |
| `portaldb.host` | The host for apollo portal db | `nil`                              |
| `portaldb.port` | The port for apollo portal db | `3306` |
| `portaldb.dbName` | The database name for apollo portal db | `ApolloPortalDB`                                     |
| `portaldb.userName` | The user name for apollo portal db | `nil` |
| `portaldb.password` | The password for apollo portal db | `nil` |
| `portaldb.connectionStringProperties` | The connection string properties for apollo portal db | `characterEncoding=utf8` |
| `portaldb.service.enabled` | Whether to create a Kubernetes Service for `portaldb.host` or not. Set it to `true` if `portaldb.host` is an endpoint outside of the kubernetes cluster | `false` |
| `portaldb.service.fullNameOverride` | Override the service name for apollo portal db | `nil` |
| `portaldb.service.port` | The port for the service of apollo portal db | `3306` |
| `portaldb.service.type` | The service type of apollo portal db: `ClusterIP` or `ExternalName` | `ClusterIP` |
