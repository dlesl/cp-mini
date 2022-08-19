# cp-mini

Run Zookeeper, Kafka and Schema Registry together in a single process, as a docker image or jar.

* Fast starting (~ 6 seconds until all three services are ready)
* Almost small (75 mb jar)
* Configurable via env vars

## Usage

As a docker image (amd64 or arm64)
``` sh
docker run --rm -p 2181:2181 -p 29092:29092 -p 8081:8081 \
    -e CREATE_TOPICS=test_topic \
    gchr.io/dlesl/cp-mini
```

Or in a docker-compose stack (here we make the kafka broker accessible from the host at `localhost:29092`)
``` yaml
services:
  cp-mini:
    image: ghcr.io/dlesl/cp-mini
    ports:
      - 8081:8081
      - 29092:29092
    environment:
      CREATE_TOPICS: test_topic
      ROOT_LOG_LEVEL: info
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: CLIENT:PLAINTEXT,EXTERNAL:PLAINTEXT
      KAFKA_LISTENERS: CLIENT://:9092,EXTERNAL://:29092
      KAFKA_ADVERTISED_LISTENERS: CLIENT://cp-mini:9092,EXTERNAL://127.0.0.1:29092
      KAFKA_INTER_BROKER_LISTENER_NAME: CLIENT

```

As a jar (grab it from the releases page)
``` sh
java -jar cp-mini.jar
# arguments will be interpreted as a command to execute once the stack is up, useful for running tests
java -jar cp-mini.jar bash ./my-test-suite.sh
```
