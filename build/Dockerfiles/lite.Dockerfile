FROM openanolis/anolisos:8.4-x86_64

## install openjdk 17
ARG JDK_VERSION=17.0.6+10
ARG JDK_DIR=/usr/local/openjdk-17
RUN mkdir -p ${JDK_DIR} && \
    curl -o openjdk.tar.gz https://builds.openlogic.com/downloadJDK/openlogic-openjdk/${JDK_VERSION}/openlogic-openjdk-${JDK_VERSION}-linux-x64.tar.gz && \
    tar -xvf openjdk.tar.gz -C ${JDK_DIR} --strip-components=1 && \
    rm -rf openjdk.tar.gz
ENV JAVA_HOME=${JDK_DIR}
ENV PATH=${JAVA_HOME}/bin:${PATH}

## set timezone and charset
ENV TZ=Asia/Shanghai
ENV LANG=C.UTF-8

WORKDIR /home/admin/dev

CMD ["sh"]