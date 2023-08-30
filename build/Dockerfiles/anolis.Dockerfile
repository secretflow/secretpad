FROM secretflow-registry.cn-hangzhou.cr.aliyuncs.com/secretflow/secretpad-base-lite:0.2

ENV LANG=C.UTF-8
WORKDIR /app

RUN mkdir -p /var/log/secretpad && mkdir -p /app/db && mkdir -p /app/config/certs && yum install -y sqlite

COPY config /app/config
COPY scripts /app/scripts
COPY demo/data /app/data
COPY target/*.jar secretpad.jar

EXPOSE 80
EXPOSE 8080
ENTRYPOINT ["java","-jar", "-Dsun.net.http.allowRestrictedHeaders=true", "secretpad.jar"]