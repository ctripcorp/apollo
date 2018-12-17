#!/bin/sh

# apollo config db info
apollo_config_db_url=jdbc:mysql://mysql-4000512230212608-write.default/apollo_config_pro?useSSL=false
apollo_config_db_username=root
apollo_config_db_password=Facethink2016

# apollo portal db info
apollo_portal_db_url=jdbc:mysql://mysql-4000512230212608-write.default/apollo_portal?useSSL=false
apollo_portal_db_username=root
apollo_portal_db_password=Facethink2016

#meta server url, different environments should have different meta server addresses
dev_meta=http://godeye-apollo-configservice:8768
fat_meta=http://godeye-apollo-configservice-fat:8768
uat_meta=http://godeye-apollo-configservice-uat:8768
pro_meta=http://godeye-apollo-configservice-pro:8768

META_SERVERS_OPTS="-Ddev_meta=$dev_meta -Dfat_meta=$fat_meta -Duat_meta=$uat_meta -Dpro_meta=$pro_meta"

# =============== Please do not modify the following content =============== #
# go to script directory
cd "${0%/*}"

cd ..

# package config-service and admin-service
echo "==== starting to build config-service and admin-service ===="

mvn clean package -DskipTests -pl apollo-configservice,apollo-adminservice -am -Dapollo_profile=github -Dspring_datasource_url=$apollo_config_db_url -Dspring_datasource_username=$apollo_config_db_username -Dspring_datasource_password=$apollo_config_db_password

echo "==== building config-service and admin-service finished ===="

echo "==== starting to build portal ===="

mvn clean package -DskipTests -pl apollo-portal -am -Dapollo_profile=github,auth -Dspring_datasource_url=$apollo_portal_db_url -Dspring_datasource_username=$apollo_portal_db_username -Dspring_datasource_password=$apollo_portal_db_password $META_SERVERS_OPTS

echo "==== building portal finished ===="

echo "==== starting to build client ===="

mvn clean install -DskipTests -pl apollo-client -am $META_SERVERS_OPTS

echo "==== building client finished ===="

