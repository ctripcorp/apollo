#!/bin/sh

# apollo config db info
apollo_config_db_url=jdbc:mysql://fill-in-the-correct-server:3306/ApolloConfigDB?characterEncoding=utf8
apollo_config_db_username=FillInCorrectUser
apollo_config_db_password=FillInCorrectPassword

# apollo portal db info
apollo_portal_db_url=jdbc:mysql://fill-in-the-correct-server:3306/ApolloPortalDB?characterEncoding=utf8
apollo_portal_db_username=FillInCorrectUser
apollo_portal_db_password=FillInCorrectPassword

# apollo services and portal port
config_server_port=8080
admin_server_port=8090
portal_server_port=8070

# meta server url, different environments should have different meta server addresses
dev_meta=http://fill-in-dev-meta-server:$config_server_port
fat_meta=http://fill-in-fat-meta-server:$config_server_port
uat_meta=http://fill-in-uat-meta-server:$config_server_port
pro_meta=http://fill-in-pro-meta-server:$config_server_port

META_SERVERS_OPTS="-Ddev_meta=$dev_meta -Dfat_meta=$fat_meta -Duat_meta=$uat_meta -Dpro_meta=$pro_meta"

# =============== Please do not modify the following content =============== #
# go to script directory
cd "${0%/*}"

cd ..

# package config-service and admin-service
echo "==== starting to build config-service and admin-service ===="

mvn clean package -DskipTests -pl apollo-configservice,apollo-adminservice -am -Dconfig_server_port=$config_server_port -Dadmin_server_port=$admin_server_port -Dapollo_profile=github -Dspring_datasource_url=$apollo_config_db_url -Dspring_datasource_username=$apollo_config_db_username -Dspring_datasource_password=$apollo_config_db_password

echo "==== building config-service and admin-service finished ===="

echo "==== starting to build portal ===="

mvn clean package -DskipTests -pl apollo-portal -am -Dserver_port=$portal_server_port -Dapollo_profile=github,auth -Dspring_datasource_url=$apollo_portal_db_url -Dspring_datasource_username=$apollo_portal_db_username -Dspring_datasource_password=$apollo_portal_db_password $META_SERVERS_OPTS

echo "==== building portal finished ===="
