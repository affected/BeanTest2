package com.affecto.beantest2;

//package io.mikael.demo;

        import org.eclipse.paho.client.mqttv3.MqttClient;
        import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
        import org.eclipse.paho.client.mqttv3.MqttException;
        import org.eclipse.paho.client.mqttv3.MqttMessage;
        import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

        import java.nio.charset.StandardCharsets;

public class MqttPublishSample {

    public static void main(String[] args) {

                String topic = "test";
                String content = "{\n" +
                        "  \"unitOfMeasure\": \"C\",\n" +
                        "  \"partitionId\": \"A1\",\n" +
                        "  \"measurementType\": \"temperature\",\n" +
                        "  \"timeCreated\": \"2015-04-02T07:28:43-03:00\",\n" +
                        "  \"organization\": \"NIOTH2015\",\n" +
                        "  \"guid\": \"b646c5a3-b873-4910-a1ee-53168daa3341\",\n" +
                        "  \"value\": 3,\n" +
                        "  \"sensorId\": \"bean3\",\n" +
                        "  \"modCamId\": \"A2\"\n" +
                        "}";
                int qos = 2;
                String broker = "tcp://mqtt.dev.mikael.io:1883";
                String clientId = "JavaSample";
                MemoryPersistence persistence = new MemoryPersistence();

                try {
                    final MqttClient client = new MqttClient(broker, clientId, persistence);
                    final MqttConnectOptions opts = new MqttConnectOptions();
                    opts.setCleanSession(true);
                    System.out.printf("Connecting to broker: %s%n", broker);

                    client.connect(opts);
                    System.out.println("Connected");

                    System.out.printf("Publishing message: %s%n", content);
                    final MqttMessage message = new MqttMessage(content.getBytes(StandardCharsets.UTF_8));
                    message.setQos(qos);
                    client.publish(topic, message);
                    System.out.println("Message published");

                    client.disconnect();
                    System.out.println("Disconnected");

                    System.exit(0);
                } catch (final MqttException me) {
                    System.out.println("reason " + me.getReasonCode());
                    System.out.println("msg " + me.getMessage());
                    System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }
    }
}