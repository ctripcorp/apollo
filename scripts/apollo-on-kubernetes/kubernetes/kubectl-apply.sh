# create namespace
kubectl create namespace sre

# dev-env
kubectl apply -f service-mysql-for-apollo-dev-env.yaml --record && \
kubectl apply -f service-apollo-config-server-dev.yaml --record && \
kubectl apply -f service-apollo-admin-server-dev.yaml --record

# fat-env(test-alpha-env)
kubectl apply -f service-mysql-for-apollo-test-alpha-env.yaml --record && \
kubectl apply -f service-apollo-config-server-test-alpha.yaml --record && \
kubectl apply -f service-apollo-admin-server-test-alpha.yaml --record

# uat-env(test-beta-env)
kubectl apply -f service-mysql-for-apollo-test-beta-env.yaml --record && \
kubectl apply -f service-apollo-config-server-test-beta.yaml --record && \
kubectl apply -f service-apollo-admin-server-test-beta.yaml --record

# prod-env
kubectl apply -f service-mysql-for-apollo-prod-env.yaml --record && \
kubectl apply -f service-apollo-config-server-prod.yaml --record && \
kubectl apply -f service-apollo-admin-server-prod.yaml --record

# portal
kubectl apply -f service-apollo-portal-server.yaml --record
