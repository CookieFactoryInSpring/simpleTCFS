# The Cookie Factory... in Spring (aka the simple TCFS)

  * Author: Philippe Collet
  * Author: Nassim Bounouas
  * Reviewer: Mireille Blay-Fornarino
  * Reviewer: Anne-Marie Déry
  * Reviewer: Nikita Rousseau
  * some code and doc borrowed from the original Cookie Factory by Sebastien Mosser, last fork being [https://github.com/collet/4A_ISA_TheCookieFactory](https://github.com/collet/4A_ISA_TheCookieFactory)

This case study is used to illustrate the different technologies involved in the _Introduction to Software Architecture_  course given at Polytech Nice - Sophia Antipolis at the graduate level. This demonstration code requires the following software to run properly:

  * Build & Spring environment configuration: Maven >=3.8.5 (provided maven wrapper set to 3.9.6)
  * Spring/Java implementation language: Java >=17 or above (Java language level is set to Java 17), SpringBoot 3.2.0
  * NestJS 10.2.1 (node 20+, npm 10+)
  * Docker Engine (with compose) >= 24.x

## Product vision

_The Cookie Factory_ (TCF) is a major bakery brand in the USA. The _Cookie on Demand_ (CoD) system is an innovative service offered by TCF to its customer. They can order cookies online thanks to an application, and select when they'll pick-up their order in a given shop. The CoD system ensures to TCF's happy customers that they'll always retrieve their pre-paid warm cookies on time.

## Chapters

  1. [Architecture and deployment](chapters/Architecture.md)
  2. [Business components](chapters/BusinessComponents.md)
  3. [Controllers](chapters/Controllers.md)
  4. [Testing](chapters/Testing.md)
  5. [Persistence](chapters/Persistence.md)
  6. [AOP, logging, and monitoring](chapters/AOPLogging.md)

## How to use this repository

The following "build and run" documentation is divided in two versions from "everything in a container" to a bare run. The second part helps in understanding each compilation and running steps.

If you want, the bank API has been deployed on the Apoorva Hosting Service (uptime not garanteed, no SLA provided, no support provided).   
However ! We can garantee that the servers are France, maybe in Europe, maybe somewhere else.  
Here is the url : https://bank.polytech.apoorva64.com/.  
Here is the uptime : https://uptime.ozeliurs.com/status/polytech.  
Here is the admin : best-admin-in-the-world@ozeliurs.com  

### Everything containerized

While the details and separate build of each subsystem can be found in the following section, we run here the Spring backend with postgres, the CLI, and the external system into docker. It requires to build the three images while the `docker compose` will take care of retrieving an official postgres image and compose everything.

To build all three images, you can directly run the `build-all.sh` script. It actually goes in each of the three directory and run the corresponding `build.sh` script, which itself compiles, if needed, and creates the image. For example the build of the cli docker image corresponds to the command `docker build --build-arg JAR_FILE=target/cli-0.0.1-SNAPSHOT.jar -t pcollet/tcf-spring-cli .`

As for the postgres database, we reuse its standard image and configure several environment variables that will be used to configure the backend (so that the JPA configuration will connect to the DB, as explained in the [chapter on persistence](chapters/Persistence.md)).

The whole system can now be deployed locally from the root folder using the command:

    docker compose up -d

after some time, each container is started, in the right order from their dependencies, waiting for their health check command to be OK. You can then use :

    docker attach cli

enables to use the containerized. In this spring shell cli, 

At startup the cli must provide the following prompt :

    shell:>

Running the command `help` will guide you in the CLI usage.

You can run a demo with `script demo.txt`. The docker-compose file indeed contains a volume declaration to mount the `demo.txt` file which can be directly used from the cli as below to iterate through a complete scenario and check that verify is running fine :

```
shell:>script demo.txt
start of demo
SCENARIO: simple registration and valid order
[DARK_TEMPTATION, SOO_CHOCOLATE, CHOCOLALALA]
Customer{id='1', name='kadoc', creditCard='5251896983'}
{kadoc=Customer{id='1', name='kadoc', creditCard='5251896983'}}
cart content ->
[]
12xCHOCOLALALA
3xSOO_CHOCOLATE
[3xSOO_CHOCOLATE, 12xCHOCOLALALA]
2xSOO_CHOCOLATE
[12xCHOCOLALALA, 2xSOO_CHOCOLATE]
Order 1 (amount 18.1) is validated
[...]
```

Some actions will display intended errors :

```
[CliOrder[id=1, customerId=1, price=18.1, payReceiptId=RECEIPT:3eb2dac0-c6ef-4535-9e48-4f76654837c2, status=IN_PROGRESS]]
ERROR 403: remove too much cookies in a cart
2xSOO_CHOCOLATE
403 : "{"error":"Attempting to update the cookie quantity to a negative value","details":"from Customer kadoc with cookie SOO_CHOCOLATE leading to quantity -1"}"
Details of the error have been omitted. You can use the stacktrace command to print the full stacktrace.
```

Here are some useful commands with docker compose:

    docker compose down # to stop the containers gracefully

    docker compose ps # to list the running containers with their health status

    docker compose logs --follow # to follow the logs of all containers (as -d has been used to run them in background). You can run this command in a separate terminal to check what happens in the different containers' logs.


As for persistence, you can use the `psql` command within the postgres image to connect to the DB with a SQL cli:

    docker exec -it db psql -U postgresuser -W -d tcf-db

And then commands like:

* `\dt+` to list all tables
* `SELECT * FROM customer;` to check that the two customers have been created by the demo script.

Note that you cannot run the two docker images separately and expect them to communicate with each other, each one being isolated in its own container. That's one of the main purpose of `docker compose` to enable composition of container, with by default a shared network. A complete schema of the docker compose deployment is available in the [Architecture and deployment chapter](chapters/Architecture.md).


### Basic build and run 

Outside docker, the first step is to build the backend and the cli. This can be done manually, from both folders (it will generate the corresponding jar into the target folder), using the command:

    mvn clean package
 
Note that the previous command will only run unit tests. To run both unit and integration tests, you can use:

    mvn clean verify
    
See the page on [Testing](chapters/Testing.md#running-different-types-of-test-with-maven) for more details.

With a postgres DB running inside docker but accessible outside (in your host), first run:

    ./run-postgres-out-of-docker-compose.sh

This will run a postgres server listening on the 5432 port of your host machine.

Do not forget to run the external bank system as well!

To run the server (from the corresponding folder):

    POSTGRES_HOST=127.0.0.1:5432 mvn spring-boot:run

or

    POSTGRES_HOST=127.0.0.1:5432 java -jar target/simpleTCFS-0.0.1-SNAPSHOT.jar

To run the cli (from the corresponding folder):

    mvn spring-boot:run

or

    java -jar target/cli-0.0.1-SNAPSHOT.jar

**NOTE: To prevent unnecessary complexity, do not use "docker run" on the cli and the backend. USE "docker compose".**


## Tips & Troubleshoot

### Windows specific terminals

Few terminals are not considered as a TTY which will lead to an error at the `docker attach cli` command run. According the terminal used it might be necessary to prepend the command with another one. For example for `MinGW64` : 

```
$ docker attach cli
the input device is not a TTY.  If you are using mintty, try prefixing the command with 'winpty'

$ winpty docker attach cli
shell:>
```

## Docker Desktop Backend on Windows

The backend used for Docker Desktop on Windows can be either WSL or Hyper-V. WSL might require an update/upgrade. The procedure can be found on the following page and requires a restart : [https://aka.ms/wsl2kernel](https://aka.ms/wsl2kernel)

### How to detach from the CLI without stopping it ?

As mentionned on the [Docker CLI documentation](https://docs.docker.com/engine/reference/commandline/attach/#description) it is possible to detach from a running container and let it run using `CTRL+P` then `CTRL+Q`.

### build_all.sh output according the environment

The `build_all.sh` script output can be slightly different on windows than on Linux or Mac OS X depending on the environment it is executed without causing any damages. See the following (mingw64 terminal installed by git installer) :

```
** Building all
Compiling the TCF Spring BACKEND within a multi-stage docker build
#1 [internal] load build definition from Dockerfile
#1 sha256:b4da1a09722684c536ca109a15d57a497155b023ef064b46bd0202cd907f2695
#1 transferring dockerfile: 841B done
#1 DONE 0.0s

#2 [internal] load .dockerignore
#2 sha256:e317355cc535cec5252170bc3f4f01faad40d922e26e7fdceb6a256560d58447
#2 transferring context: 2B done
#2 DONE 0.0s

#4 [internal] load metadata for docker.io/library/eclipse-temurin:17-jdk-jammy
#4 sha256:a51bde80cf0615ff53399b6efd508bc3dc742fee4204aebe09d433f9034dcb55
#4 DONE 10.0s

[...]
