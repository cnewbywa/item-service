# Item service

## Overview
This is a simple rest service built with [Spring Boot](https://spring.io/projects/spring-boot) that lets users add, modify, delete and retrieve generic item objects. The service uses [virtual threads](https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html) and it contains the following additional features:

 * [OAuth2](https://oauth.net/2/) authentication
 * Redis cache
 * [OpenAPI](https://www.openapis.org/) documentation with [Swagger UI](https://swagger.io/tools/swagger-ui/)
 * Event creation (to Kafka topic) when adding, modifying and deleting items
 
Running this project requires the following 3rd party resources to be accessible:

 * [Postgres](https://www.postgresql.org/) (Tested with version 15.4)
 * [Redis](https://redis.io/) (Tested with version 7.2)
 * [Keycloak](https://www.keycloak.org/) (Tested with version 22.0.4)
 * [Kafka](https://kafka.apache.org/) with ZooKeeper (Tested with Confluent Community version of Kafka 7.5)
 
Postgres and Redis [Docker Compose](https://docs.docker.com/compose/) configurations can be found in this project while Keycloak and Kafka Docker Compose configurations can be found in the [common-services](https://github.com/cnewbywa/common-services) project.

## Build
Prerequisites:

 * [event-message](https://github.com/cnewbywa/event-message) project built and installed to local Maven repository
 * Java 21+

This project supports running the application either locally or in a docker container. The docker image creation is done with [Jib](https://github.com/GoogleContainerTools/jib).

Please note that the integration tests in this project use [Testcontainers](https://testcontainers.com/) which requires a local Docker installation. To skip running integration tests use the following parameter: `-Dskip.integration.tests=true`

#### Local
```
./mvnw clean package
```

#### Docker
```
./mvnw clean package jib:dockerBuild
```

This command does require a local Docker installation.

## Usage
Prerequisites:

 * Keycloak running
 * Kafka running
 * Java 21+

Start Postgres and Redis by running the `docker/db/start.sh` script. Please note that the Docker Compose project mounts volumes to the host file system and uses [Docker secrets](https://docs.docker.com/compose/use-secrets/).

Very simplified flow:

 * Get access token
 * Perform action on item service using received access token

### Start

#### Local
Prerequisites:

 * ca.crt from the [auth service proxy ssl directory](https://github.com/cnewbywa/common-services/tree/main/docker/runtime/auth/nginx/ssl) has been added to the Java truststore, with the following command:

```
keytool -import -trustcacerts -keystore <location of the cacerts file in the currently used sdk> \
   -storepass changeit -noprompt -alias authcacert -file ca.crt
```

Start the application:

```
./mvnw spring-boot:run -Dspring-boot.run.arguments="--postgres.password=bD4xo9RcW- --item.keystore.password=itemservice" -Dspring-boot.run.profiles=local
```

#### Docker
Run the `docker/app/start.sh` script to start the application

### Use
Swagger UI is available at `https://localhost:8443/items/swagger-ui/index.html`. You need to fetch an access token to be able to call the services (see below one way to do it).

Examples with curl:

Get access token:

```
curl -i --insecure --request POST 'https://cnewbywa.auth:443/realms/item/protocol/openid-connect/token' --header 'Content-Type: application/x-www-form-urlencoded' --data-urlencode 'grant_type=password' --data-urlencode 'client_id=item-app' --data-urlencode 'username=test.user' --data-urlencode 'password=Password1'
```

Add item (using an input file with json content)

```
curl --insecure -i -X POST 'https://localhost:8443/items' -d @input.json --header 'Content-Type: application/json' --header 'Authorization: bearer <access token>'
```

Get item

```
curl --insecure -i -X GET 'https://localhost:8443/items/<item id>' --header 'Authorization: bearer <access token>'
```
