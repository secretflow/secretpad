ARG BASE_IMAGE=secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/secretpad-base-lite:0.3
FROM ${BASE_IMAGE}

ARG TARGETPLATFORM

ENV TZ=Asia/Shanghai
ENV LANG=C.UTF-8
WORKDIR /app

RUN mkdir -p /var/log/secretpad && mkdir -p /app/db && mkdir -p /app/config/certs && yum install -y sqlite

COPY config /app/config
COPY scripts /app/scripts
COPY demo/data /app/data
COPY target/secretpad.jar secretpad.jar
ENV JAVA_OPTS="-server -Xms2048m -Xmx2300m -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m" SPRING_PROFILES_ACTIVE="default"
EXPOSE 80
EXPOSE 8080
EXPOSE 9001
ENTRYPOINT java ${JAVA_OPTS} -Dsun.net.http.allowRestrictedHeaders=true  -jar -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} /app/secretpad.jar