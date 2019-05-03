#
# CAMP
#
# Copyright (C) 2017 -- 2019 SINTEF Digital
# All rights reserved.
#
# This software may be modified and distributed under the terms
# of the MIT license.  See the LICENSE file for details.
#

# Step 1: Build the WAR file
FROM openjdk:8-jdk-stretch as builder

LABEL maintainer "franck.chauvel@sintef.no"

RUN apt-get update && \
	apt-get install -y --no-install-recommends  \
	maven=3.3.9-4 && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN mkdir greetings
WORKDIR /greetings

COPY . /greetings
RUN mvn clean package
RUN mv target/greetings-1.0-SNAPSHOT.war target/greetings.war


# Step 2: Deploy the WAR file into a Tomcat instance
FROM tomcat:8

COPY --from=builder /greetings/target/greetings.war /usr/local/tomcat/webapps/greetings.war

CMD ["catalina.sh", "run"]
