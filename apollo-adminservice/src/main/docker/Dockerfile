# Dockerfile for apollo-adminservice
# Build with:
# docker build -t apollo-adminservice .
# Run with:
# docker run -p 8090:8090 -d -v /tmp/logs:/opt/logs --name apollo-adminservice apollo-adminservice

FROM openjdk:8-jre-alpine
MAINTAINER ameizi <sxyx2008@163.com>

ENV VERSION 0.10.2

RUN echo "http://mirrors.aliyun.com/alpine/v3.6/main" > /etc/apk/repositories \
    && echo "http://mirrors.aliyun.com/alpine/v3.6/community" >> /etc/apk/repositories \
    && apk update upgrade \
    && apk add --no-cache procps unzip curl bash tzdata \
    && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

ADD apollo-adminservice-${VERSION}-github.zip /apollo-adminservice/apollo-adminservice-${VERSION}-github.zip

RUN unzip /apollo-adminservice/apollo-adminservice-${VERSION}-github.zip -d /apollo-adminservice \
    && rm -rf /apollo-adminservice/apollo-adminservice-${VERSION}-github.zip \
    && sed -i '$d' /apollo-adminservice/scripts/startup.sh \
    && echo "tail -f /dev/null" >> /apollo-adminservice/scripts/startup.sh

EXPOSE 8090

CMD ["/apollo-adminservice/scripts/startup.sh"]
