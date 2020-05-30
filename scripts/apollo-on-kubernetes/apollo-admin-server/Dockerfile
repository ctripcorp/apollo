# Dockerfile for apollo-admin-server

# Build with:
# docker build -t apollo-admin-server:v1.0.0 .

FROM openjdk:8-jre-alpine3.9

RUN \
    echo "http://mirrors.aliyun.com/alpine/v3.9/main" > /etc/apk/repositories && \
    echo "http://mirrors.aliyun.com/alpine/v3.9/community" >> /etc/apk/repositories && \
    apk update upgrade && \
    apk add --no-cache procps curl bash tzdata && \
    ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    mkdir -p /apollo-admin-server

ADD . /apollo-admin-server/

ENV APOLLO_ADMIN_SERVICE_NAME="service-apollo-admin-server.sre"

EXPOSE 8090

CMD ["/apollo-admin-server/scripts/startup-kubernetes.sh"]
