FROM maven:3.8.5-openjdk-8
EXPOSE 8081

RUN mkdir -p /authlete/app

ADD . /authlete/app

WORKDIR /authlete/app

RUN mvn -s /usr/share/maven/ref/settings-docker.xml clean install

CMD ["mvn", "-s", "/usr/share/maven/ref/settings-docker.xml", "clean", "jetty:run"]
