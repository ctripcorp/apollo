# Dockerfile for apollo-config-server

# Build with:
# docker build -t apollo-config-server:v1.0.0 .

FROM openjdk:8-jre-alpine3.8

RUN \
    echo "http://mirrors.aliyun.com/alpine/v3.8/main" > /etc/apk/repositories && \
    echo "http://mirrors.aliyun.com/alpine/v3.8/community" >> /etc/apk/repositories && \
    apk update upgrade && \
    apk add --no-cache procps curl bash tzdata && \
    ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    mkdir -p /apollo-config-server

ADD . /apollo-config-server/

ENV APOLLO_CONFIG_SERVICE_NAME="service-apollo-config-server.sre"

EXPOSE 8080

CMD ["/apollo-config-server/scripts/startup-kubernetes.sh"]
