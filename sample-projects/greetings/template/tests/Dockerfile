#
# CAMP
#
# Copyright (C) 2017 -- 2019 SINTEF Digital
# All rights reserved.
#
# This software may be modified and distributed under the terms
# of the MIT license.  See the LICENSE file for details.
#

FROM openjdk:8-jdk-stretch

LABEL maintainer "franck.chauvel@sintef.no"

RUN apt-get update && \
	apt-get install -y --no-install-recommends  \
	maven=3.3.9-4 \
	&& \
    apt-get clean && \
	rm -rf /var/lib/apt/lists/*

WORKDIR /tests
COPY . /tests
