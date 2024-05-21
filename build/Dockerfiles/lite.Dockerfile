ARG BASE_IMAGE=openanolis/anolisos:23
FROM ${BASE_IMAGE}
ARG JDK_DIR=/usr/local/openjdk-17
ARG TARGETPLATFORM

RUN yum install -y tar gzip
RUN echo "Building for $TARGETPLATFORM" && \
    if [ "$TARGETPLATFORM" = "linux/amd64" ]; then \
          mkdir -p ${JDK_DIR} && \
          curl -o openjdk.tar.gz https://secretflow-public.oss-cn-hangzhou.aliyuncs.com/OpenJDK17U-jdk_x64_linux_hotspot_17.0.11_9.tar.gz && \
          tar -xvf openjdk.tar.gz -C ${JDK_DIR} --strip-components=1 && \
          rm -rf openjdk.tar.gz; \
    elif [ "$TARGETPLATFORM" = "linux/arm64" ]; then \
          mkdir -p ${JDK_DIR} && \
          curl -o openjdk.tar.gz https://secretflow-public.oss-cn-hangzhou.aliyuncs.com/OpenJDK17U-jdk_aarch64_linux_hotspot_17.0.11_9.tar.gz && \
          tar -xvf openjdk.tar.gz -C ${JDK_DIR} --strip-components=1 && \
          rm -rf openjdk.tar.gz; \
    else \
        echo "Unsupported platform: $TARGETPLATFORM"; \
        exit 1; \
    fi

ENV JAVA_HOME=${JDK_DIR}
ENV PATH=${JAVA_HOME}/bin:${PATH}
## set timezone and charset
ENV TZ=Asia/Shanghai
ENV LANG=C.UTF-8
WORKDIR /home/admin/dev
CMD ["java","-version"]