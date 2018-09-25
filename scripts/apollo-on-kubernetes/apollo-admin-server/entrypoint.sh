#!/bin/sh

set -e

# admin-server config
admin_server_config=/apollo-admin-server/config/application-github.properties
cp ${admin_server_config} ${admin_server_config}.cp

sed -i -E "s#DATASOURCES_URL#${DATASOURCES_URL}#g" ${admin_server_config}.cp
sed -i -E "s#DATASOURCES_USERNAME#${DATASOURCES_USERNAME}#g" ${admin_server_config}.cp
sed -i -E "s#DATASOURCES_PASSWORD#${DATASOURCES_PASSWORD}#g" ${admin_server_config}.cp

cat ${admin_server_config}.cp > ${admin_server_config}
rm -rf ${admin_server_config}.cp

exec "$@"
