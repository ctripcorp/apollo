#!/bin/sh

set -e

# portal-server config
portal_server_config=/apollo-portal-server/config/application-github.properties
cp ${portal_server_config} ${portal_server_config}.cp

sed -i -E "s#DATASOURCES_URL#${DATASOURCES_URL}#g" ${portal_server_config}.cp
sed -i -E "s#DATASOURCES_USERNAME#${DATASOURCES_USERNAME}#g" ${portal_server_config}.cp
sed -i -E "s#DATASOURCES_PASSWORD#${DATASOURCES_PASSWORD}#g" ${portal_server_config}.cp

cat ${portal_server_config}.cp > ${portal_server_config}
rm -rf ${portal_server_config}.cp

# meta-server config
meta_server_config=/apollo-portal-server/config/apollo-env.properties
cp ${meta_server_config} ${meta_server_config}.cp

sed -i -E "s#DEV_META_SERVICE_NAME#${DEV_META_SERVICE_NAME}#g" ${meta_server_config}.cp
sed -i -E "s#TEST_ALPHA_META_SERVICE_NAME#${TEST_ALPHA_META_SERVICE_NAME}#g" ${meta_server_config}.cp
sed -i -E "s#TEST_BETA_META_SERVICE_NAME#${TEST_BETA_META_SERVICE_NAME}#g" ${meta_server_config}.cp
sed -i -E "s#PROD_META_SERVICE_NAME#${PROD_META_SERVICE_NAME}#g" ${meta_server_config}.cp

cat ${meta_server_config}.cp > ${meta_server_config}
rm -rf ${meta_server_config}.cp

exec "$@"
