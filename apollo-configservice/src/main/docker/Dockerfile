# Dockerfile for apollo-configservice
# 1. Copy apollo-configservice-${VERSION}-github.zip to current directory
# 2. Build with: docker build -t apollo-configservice .
# 3. Run with: docker run -p 8080:8080 -d -v /tmp/logs:/opt/logs --name apollo-configservice apollo-configservice

FROM openjdk:8-jre-alpine
MAINTAINER ameizi <sxyx2008@163.com>

ENV VERSION 1.4.0-SNAPSHOT

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

ADD apollo-configservice-${VERSION}-github.tar.gz /apollo-configservice/apollo-configservice-${VERSION}-github.tar.gz

RUN tar -zxvf /apollo-configservice/apollo-configservice-${VERSION}-github.tar.gz -C /apollo-configservice \
    && rm -rf /apollo-configservice/apollo-configservice-${VERSION}-github.tar.gz \
    && sed -i '$d' /apollo-configservice/scripts/startup.sh \
    && echo "tail -f /dev/null" >> /apollo-configservice/scripts/startup.sh

EXPOSE 8080

CMD ["/apollo-configservice/scripts/startup.sh"]
