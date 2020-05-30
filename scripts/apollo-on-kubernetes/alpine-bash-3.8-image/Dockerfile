# Build with:
# docker build -t alpine-bash:3.8 .

FROM alpine:3.9

RUN \
    echo "http://mirrors.aliyun.com/alpine/v3.9/main" > /etc/apk/repositories && \
    echo "http://mirrors.aliyun.com/alpine/v3.9/community" >> /etc/apk/repositories  && \
    apk update upgrade && \
    apk add --no-cache procps curl bash && \
    ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

CMD ["bash"]
