package com.dlesl.cpmini;

import io.confluent.kafka.schemaregistry.rest.SchemaRegistryConfig;
import io.confluent.kafka.schemaregistry.rest.SchemaRegistryRestApplication;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.function.BiConsumer;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            wrappedMain(args);
        } catch (Command.CommandFailedException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static void wrappedMain(String[] args) throws Exception {
        var srProps = new Properties();

        var kafkaPorts = getKafkaPorts();
        srProps.setProperty("kafkastore.bootstrap.servers", "127.0.0.1:" + getKafkaPorts()[0]);

        EmbeddedKafkaBroker broker = new EmbeddedKafkaBroker(1, false, 1, getTopicsToCreate())
                .kafkaPorts(kafkaPorts)
                .zkPort(getZkPort());

        System.getenv().forEach((k, v) -> {
            maybeSetProperty("KAFKA_", k, v, broker::brokerProperty);
            maybeSetProperty("SCHEMA_REGISTRY_", k, v, srProps::setProperty);
        });

        // we cheat a bit and "initialize" the bean like this (it works because EmbeddedKafka doesn't use DI).
        // This starts zookeeper + kafka
        broker.afterPropertiesSet();

        try {
            SchemaRegistryRestApplication app = new SchemaRegistryRestApplication(
                    new SchemaRegistryConfig(srProps));
            Server server = app.createServer();
            server.start();
            if (args.length > 0) {
                try {
                    Command.run(args);
                } finally {
                    server.stop();
                }
            }
            server.join();
        } finally {
            broker.destroy();
        }
    }

    private static void maybeSetProperty(String prefix, String envVarName, String value,
                                         BiConsumer<String, String> setter) {
        if (!envVarName.startsWith(prefix)) return;
        var rest = envVarName.substring(prefix.length());
        var property = String.join(".", rest.split("_")).toLowerCase(Locale.ROOT);
        logger.info("Setting property {} to {}", property, value);
        setter.accept(property, value);
    }

    private static String[] getListFromEnv(String envVar) {
        var topics = System.getenv(envVar);
        if (topics == null) return new String[] {};
        return topics.split(",");
    }

    private static String[] getTopicsToCreate() {
        return getListFromEnv("CREATE_TOPICS");
    }

    private static int[] getKafkaPorts() {
        var fromEnv = getListFromEnv("KAFKA_PORTS");
        if (fromEnv.length == 0) return new int[] {9092};
        return Arrays.stream(fromEnv).mapToInt(Integer::parseInt).toArray();
    }

    private static int getZkPort() {
        var fromEnv = System.getenv("ZOOKEEPER_PORT");
        if (fromEnv == null) return 2181;
        return Integer.parseInt(fromEnv);
    }
}
