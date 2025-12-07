FROM openjdk:8-jre-slim

RUN apt-get update && \
    apt-get install -y tzdata && \
    ln -fs /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    dpkg-reconfigure -f noninteractive tzdata

RUN mkdir -p /app
WORKDIR /app

COPY target/camera-server-1.0.0.jar app.jar

EXPOSE 80

ENTRYPOINT ["java", "-jar", "app.jar"]