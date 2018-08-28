#!/bin/sh

set -e

# config-server config
config_server_config=/apollo-config-server/config/application-github.properties
cp ${config_server_config} ${config_server_config}.cp

sed -i -E "s#DATASOURCES_URL#${DATASOURCES_URL}#g" ${config_server_config}.cp
sed -i -E "s#DATASOURCES_USERNAME#${DATASOURCES_USERNAME}#g" ${config_server_config}.cp
sed -i -E "s#DATASOURCES_PASSWORD#${DATASOURCES_PASSWORD}#g" ${config_server_config}.cp

cat ${config_server_config}.cp > ${config_server_config}
rm -rf ${config_server_config}.cp

exec "$@"
