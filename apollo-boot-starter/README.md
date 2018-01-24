## Apollo Spring Boot Starter ##

### 一、说明 ###
由于@EnableApolloConfig使用ImportBeanDefinitionRegistrar初始化PropertySource,时机晚于spring boot的其他PropertySource,使得@ConditionalOnProperty无法生效。这里使用SpringApplicationRunListener初始化Apollo的PropertySource，优先级如下所示：

```text
StubPropertySource {name='servletConfigInitParams'}
StubPropertySource {name='servletContextInitParams'}
MapPropertySource {name='systemProperties'}
SystemEnvironmentPropertySource {name='systemEnvironment'}
CompositePropertySource [name='ApolloPropertySources', propertySources=[ConfigPropertySource {name='application'}]]
RandomValuePropertySource {name='random'}
PropertiesPropertySource {name='applicationConfig: [classpath:/application-local.properties]'}
PropertiesPropertySource {name='applicationConfig: [classpath:/application.properties]'}
MapPropertySource {name='refresh'}
```

### 二、Maven Dependency ###
```xml
<dependency>
    <groupId>com.ctrip.framework.apollo</groupId>
    <artifactId>apollo-spring-boot-starter</artifactId>
    <version>0.9.1</version>
</dependency>

<!-- 由于apollo-client通常是自行打包发布的，这里的版本不确定，所以需要自行引入 -->
<dependency>
    <groupId>com.ctrip.framework.apollo</groupId>
    <artifactId>apollo-client</artifactId>
    <version>0.9.1-SNAPSHOT</version>
</dependency>
```

### 三、配置 ###
```properties
#默认为true
apollo.enabled=true 
#默认为application
apollo.namespaces=application,FX.apollo
```