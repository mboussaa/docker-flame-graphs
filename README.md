# What is Docker Flame Graphs?
**Docker Flame graphs (DFG)** is a CPU Flame Graph genrerator that profiles JVMs proceeses running inside Docker containers.
**Flame graphs (FGs)** are a visualization of profiled software, allowing the most frequent code-paths to be identified quickly and accurately. 
This project provides scripts and guidelines to: 
- **generate different docker-based software configurations** using CAMP
- **run stress tests** over generated configurations using Jmeter
- **profile and record stack traces** of running processes
- **generate configuration-specific Flame Graphs**
- **generate differential Flame Graphs**, useful to comapre different profiles performance

# Flame Graphs for Java
In order to generate Flame Graphs, one need a profiler that can sample stack traces.
FlameGraphs use the Linux perf toolset to profile any kernel and user space processes in a Linux machine.
Even though, perf and Flamegraphs are amazing tools to profile processes, it remains challenging to profile Java processes. More, if the Java process is in a container, it’s even more annoying. To profile JVMs, we use Perf-map-agent which generates flame graphs and allows perf top for Java programs by (1) attaching to the JVM and getting a symbol map
, and (2) running perf top of perf record with the map file.
When a JVM is inside a Docker container it’s inside its own PID namespace typically running as a user that only exists inside the container.
This means that the JVM appears to have a different PID on the host than it does in the container.
To make java perf agent work, we:
- mount perf-map-agent inside each of our containers (e.g. have it on every server and mount the volume read only).
- get the symbol map from inside the container using the container PID and copy it to the host tmp directory. Renaming it from the container PID to the host PID.
- run perf on the host as root to get top or a flame graph.
You can refer to this blog for more details http://www.batey.info/docker-jvm-flamegraphs.html
# How does it work?
## 1- Run a sample java process in a container 
### Using CAMP https://github.com/STAMP-project/camp:
Although you can run any java application in a Docker container, in this tutorial, we are going to take advantage of the CAMP project to generate different java processes in Docker containers. In fact, CAMP allows you to generate different software configurations from an input project template. To use CAMP, please follow the following instructions:

-Clone the project
```
git clone https://github.com/mboussaa/docker-flame-graphs.git
```
-Spring-petclinic is a popular spring-based web application https://github.com/spring-projects/spring-petclinic
```
cd docker-flame-graphs/sample-projects/spring-petclinic/
```
**NB:** Before generating configurations, please edit the file `camp.yml` by entring your host machine ip:
```
pattern: "IP_ADDRESS"
replacements: [ "XXX.XXX.XXX.XXX" ] #provide your host machine IP
```
-Generate configurations to support different DB (PetClinic app + MySQL and PetClinic app + Postgres)
```
docker run -t -v $PWD:/campworkingdir -v /var/run/docker.sock:/var/run/docker.sock -t fchauvel/camp:latest camp generate -d /campworkingdir
docker run -t -v $PWD:/campworkingdir -v /var/run/docker.sock:/var/run/docker.sock -t fchauvel/camp:latest camp realize -d /campworkingdir
```
-Select one of the two generated applications to run
```
cd out/config_1/ #or cd out/config_2/
docker-compose up
```
-To verify that the application is running visit: http://localhost:8080

## 2- Set up the docker profiler
-go back to the project root
```
cd docker-flame-graphs
```
-Make sure that `JAVA_HOME` is configured to point to a JDK. You also need `cmake` >= 2.8.6 (tested version cmake version 3.5.1)
-build the project
```
git clean
cmake . && make
```
-Identify the container id (`CONTAINER_ID`)
```
docker ps
```
-Attach the Docker profiler to the target running container. To do so: 
```
#In ~/docker-flame-graphs
docker cp $(pwd) [CONTAINER_ID]:/docker-flame-graphs
```
-On the host, pick up the JAVA process id (`JAVA_ID`)
```
ps aux | grep java
```
-Now everything is ready. You need to warm up you application to collect stacktraces. We are going to generate some load to our running PetClinic application using some stress tests. 
## 3- Run stress tests
-To do so, install first JMeter:
```
apt install jmeter
```
-Run tests:
```
#In ~/docker-flame-graphs/sample-projects/spring-petclinic/tests
jmeter -n -t petclinic_test_plan.jmx -l out/jtllog.csv -j out/jmeterrun.log
```
## 4- Profile and generate Flame Graphs
```
cd bin
./docker-perf-top `CONTAINER_ID` `JAVA_ID` 
./docker-perf-java-flames `CONTAINER_ID` `JAVA_ID`
```
-Flame graphs will be generated in bin/
## 5- Generate differential Flame Graphs
```
cd FlameGraph
./difffolded.pl /tmp/out-13070.collapsed /tmp/out-24343.collapsed | ./flamegraph.pl > flamegraph-differential.svg
```


