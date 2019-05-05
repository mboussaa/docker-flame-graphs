
# What is Docker Flame Graphs?

**Docker Flame graphs (DFG)** is a CPU Flame Graph generator that profiles JVMs processes running inside Docker containers.

**Flame graphs (FGs)** are a visualization of profiled software, allowing the most frequent code-paths to be identified quickly and accurately.

This project provides scripts and guidelines to:
- Generate different docker-based software configurations using CAMP
- Run stress tests over generated configurations using Jmeter
- Profile and record stack traces of running processes
- Generate configuration-specific Flame Graphs
- Generate differential Flame Graphs, useful to compare different profiles performance

# Flame Graphs for Java

In order to generate FGs, one needs a profiler that can sample stack traces.
FGs use the Linux perf toolset ([http://www.brendangregg.com/perf.html](http://www.brendangregg.com/perf.html)) to profile any kernel and user space processes in a Linux machine.

Even though, perf and FGs are amazing tools to profile processes, it remains challenging to profile Java processes. More, if the Java process is in a container, it’s even more annoying. Hence, to profile JVMs, we use Perf-map-agent ([https://github.com/jvm-profiling-tools/perf-map-agent](https://github.com/jvm-profiling-tools/perf-map-agent)) which generates flame graphs and allows perf top for Java programs by (1) attaching to the JVM and getting a symbol map, and (2) running perf top of perf record with the map file.

When a JVM is inside a Docker container it’s inside its own PID namespace typically running as a user that only exists inside the container.
This means that the JVM appears to have a different PID on the host than it does in the container.
To make java perf agent work, we:

- Mount perf-map-agent inside each of our containers (e.g. have it on every server and mount the volume read only).
- Get the symbol map from inside the container using the container PID and copy it to the host tmp directory. Renaming it from the container PID to the host PID.
- Run perf on the host as root to get top or a FG.

You can refer to this blog for more details http://www.batey.info/docker-jvm-flamegraphs.html

# How does it work?

## 1- Run a sample java process in a container

### Using CAMP https://github.com/STAMP-project/camp:

Although you can run any java application in a Docker container, in this tutorial, we are going to take advantage of the CAMP project to generate different java processes in Docker containers. In fact, CAMP allows you to generate different docker-based configurations for a given software from an input project template. To use CAMP, please follow the following instructions:

Clone the project.
```
git clone https://github.com/mboussaa/docker-flame-graphs.git
```
We provide a sample docker-based project called `Spring-petclinic` which is a popular spring-based web application https://github.com/spring-projects/spring-petclinic that supports different types of DB (HSQLDB, Postgres, MySQL) and Jetty as web sever.
```
cd docker-flame-graphs/sample-projects/spring-petclinic/
```
**NB:** Before generating configurations, please edit the `camp.yml` file by giving your host machine ip (use `ifconfig` and provide the `en0` ip).
```
pattern: "IP_ADDRESS"
replacements: [ "XXX.XXX.XXX.XXX" ] #provide your host machine IP
```
Generate configurations to support different DB (PetClinic app + MySQL and PetClinic app + Postgres).
```
docker run -t -v $PWD:/campworkingdir -v /var/run/docker.sock:/var/run/docker.sock -t fchauvel/camp:latest camp generate -d /campworkingdir
docker run -t -v $PWD:/campworkingdir -v /var/run/docker.sock:/var/run/docker.sock -t fchauvel/camp:latest camp realize -d /campworkingdir
```
Select one of the two generated applications to run, available in `out/`.
```
cd out/config_1/ #or cd out/config_2/
docker-compose up
```
Two containers will be created, one for the web app, and one for the database.
To verify that the application is well started, please browse: http://localhost:8080.

## 2- Set up the Docker FGs profiler

Go back to the root of the project.
```
cd ~/docker-flame-graphs
```

Make sure that `JAVA_HOME` is configured to point to a JDK. You also need `cmake` >= 2.8.6 (tested version `cmake version 3.5.1`).

Build the project.

```
cmake . && make
```

Identify the container ID (`CONTAINER_ID`) using:

```
docker ps
```

Attach the Docker FG profiler to the running container.
```
#In ~/docker-flame-graphs
docker cp $(pwd) [CONTAINER_ID]:/docker-flame-graphs
```
On the host, pick up the Java process ID (`JAVA_ID`) using:
```
ps aux | grep java
```
Now everything is ready. You need to warm up you application to collect stack traces. We are going to generate some load to our running PetClinic application using some stress tests.

## 3- Run stress tests

To do so, install first Jmeter on your host machine.
```
apt install jmeter
```
Run tests.
```
#In ~/docker-flame-graphs/sample-projects/spring-petclinic/tests
jmeter -n -t petclinic_test_plan.jmx -l out/jtllog.csv -j out/jmeterrun.log
```
**NB:** Right after starting Jmeter tests, you should quickly start profiling and generating FG (while tests are running) by following Step 4 described below.
## 4- Profile and generate Flame Graphs

`docker-perf-top` and `docker-perf-java-flames` are the most important scripts of this project. They take as input the `CONTAINER_ID` and `JAVA_ID` and generate an SVG file representing the FG of the target process.
```
cd bin
./docker-perf-top CONTAINER_ID JAVA_ID
./docker-perf-java-flames CONTAINER_ID JAVA_ID
```
Flame graphs will be generated in `bin/`. You can open it with your web browser.
Now to continue with Step 5, you will need to repeat steps 1 to 4 with the second configuration generated from CAMP.

## 5- Generate differential Flame Graphs

The `difffolded.pl` script is used to show the difference between the two previously generated profiles. The input parameters to the script are the two stack traces and the output is also an SVG.
```
cd FlameGraph
./difffolded.pl /tmp/out-JAVA_ID.collapsed /tmp/out-JAVA_ID.collapsed | ./flamegraph.pl > flamegraph-differential.svg
```
Differential Flame graphs will be generated in `bin/`. You can open it with your web browser.
# Sample Results
Config 1 Flame Graph: PetClinic + MySQL
![Alt text](./bin/flamegraph-13070.svg)
<img src="./bin/flamegraph-13070.svg">

Config 2 Flame Graph: PetClinic + Postgres
![Alt text](./bin/flamegraph-24343.svg)
<img src="./bin/flamegraph-24343.svg">

Differential Flame Graph (FG1 - FG2)
![Alt text](./bin/flamegraph-differential.svg)
<img src="./bin/flamegraph-differential.svg">
