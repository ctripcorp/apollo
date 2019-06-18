# Dockerfile for apollo-portal
# 1. Copy apollo-portal-${VERSION}-github.zip to current directory
# 2. Build with: docker build -t apollo-portal .
# 3. Run with: docker run -p 8070:8070 -d -v /tmp/logs:/opt/logs --name apollo-portal apollo-portal

FROM openjdk:8-jre-alpine
MAINTAINER ameizi <sxyx2008@163.com>

ENV VERSION 1.5.0-SNAPSHOT
ENV SERVER_PORT 8070
# DataSource Info 
ENV DS_URL ""
ENV DS_USERNAME ""
ENV DS_PASSWORD ""
# Environmental variable declaration (meta server url, different environments should have different meta server addresses)
ENV DEV_META ""
ENV FAT_META ""
ENV UAT_META ""
ENV LPT_META ""
ENV PRO_META ""

RUN echo "http://mirrors.aliyun.com/alpine/v3.8/main" > /etc/apk/repositories \
    && echo "http://mirrors.aliyun.com/alpine/v3.8/community" >> /etc/apk/repositories \
    && apk update upgrade \
    && apk add --no-cache procps unzip curl bash tzdata \
    && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

ADD apollo-portal-${VERSION}-github.zip /apollo-portal/apollo-portal-${VERSION}-github.zip

RUN unzip /apollo-portal/apollo-portal-${VERSION}-github.zip -d /apollo-portal \
    && rm -rf /apollo-portal/apollo-portal-${VERSION}-github.zip \
    && sed -i '$d' /apollo-portal/scripts/startup.sh \
    && chmod +x /apollo-portal/scripts/startup.sh \
    && echo "tail -f /dev/null" >> /apollo-portal/scripts/startup.sh

EXPOSE $SERVER_PORT

CMD ["/apollo-portal/scripts/startup.sh"]
