FROM ubuntu

MAINTAINER Mohamed Boussaa <mohamed.boussaa@activeeon.com>

# Install dependencies
RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y git && apt install -y \
    apt-transport-https \
    apt-utils \
    wget \
    unzip \
    openjdk-8-jdk-headless \
    openjdk-8-jre-headless
RUN echo "deb http://archive.ubuntu.com/ubuntu trusty main universe" > /etc/apt/sources.list
RUN apt-get -y update
RUN apt-get install -y curl
RUN apt-get install unzip

#VOLUME /tmp
#ADD petclinic.jar petclinic.jar
RUN git clone https://github.com/spring-petclinic/spring-framework-petclinic /spring-framework-petclinic
WORKDIR /spring-framework-petclinic
RUN git reset --hard c6db7b5d2295edf07f334d8548c857a209900d98
RUN sed -i 's:localhost:IP_ADDRESS:' pom.xml
EXPOSE 8080
CMD ["/bin/sh", "./mvnw", "jetty:run-war", "-P", "DB_PROFILE", "-DskipTests"]

