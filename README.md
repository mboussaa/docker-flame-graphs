# docker-flame-graphs
Flame graphs for JVMs running inside Docker containers

# how to run

-git clone https://github.com/mboussaa/docker-flame-graphs

-Cd docker-flame-graphs

-Make sure JAVA_HOME is configured to point to a JDK. You need cmake >= 2.8.6 (tested version cmake version 3.5.1)

-git clean

-cmake . && make

-run java in docker docker run -d -v $(pwd):/docker-flame-graphs java-app (a sample java app)

-pgrep -fl java and pick up a java_id

-docker ps and pick up the container_id

-cd bin

-./docker-perf-top container_id pid (edited vi docker-perf-top)

-./docker-perf-java-flames container_id pid


