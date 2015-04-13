package com.affecto.beantest2;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import nl.littlerobots.bean.*;
import nl.littlerobots.bean.message.Callback;


public class MainActivity extends ActionBarActivity {

    /**
     * TAG = "BeanTest2". Used for debug messages.
     */
    private static final String TAG = "BeanTest2";
    public Bean myBean;
    public Random rnd = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create a listener
        BeanDiscoveryListener listener = new BeanDiscoveryListener() {
            @Override
            public void onBeanDiscovered(Bean bean) {
                Toast.makeText(getApplicationContext(), "onBeanDiscovered", Toast.LENGTH_LONG).show();
                bean.connect(getApplicationContext(), myBeanListener);
                if (bean.getDevice().getAddress().equals("D0:39:72:C8:D4:72")) {
                    myBean = bean;
                    bean.setLed(rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255));
                    bean.setLed(0, 0, 0);
                    myBean.readTemperature(new Callback<Integer>() {
                        @Override
                        public void onResult(Integer i) {
                            testMqtt(i, 100);
                            Toast.makeText(getApplicationContext(), "Temperature: " + i, Toast.LENGTH_LONG).show();
                            Log.w(TAG, "Temperature: " + i);
                        }
                    });
                } else {
                    bean.disconnect();
                }
                if (myBean != null  ) {
                    Log.w(TAG, "cancelDiscovery");
                    BeanManager.getInstance().cancelDiscovery();
                    onDiscoveryComplete();
                }
            }

            @Override
            public void onDiscoveryComplete() {
                int num = BeanManager.getInstance().getBeans().size();
                Log.w(TAG, num + " Beans Found");
            }

            BeanListener myBeanListener = new BeanListener() {
                @Override
                public void onConnected() {
                    //Toast.makeText(getApplicationContext(), "CONNECTED TO BEAN", Toast.LENGTH_LONG).show();
                    Log.w(TAG, "CONNECTED TO BEAN");
                }

                @Override
                public void onConnectionFailed() {
                    //Toast.makeText(getApplicationContext(), "CONNECTED FAILED", Toast.LENGTH_LONG).show();
                    Log.w(TAG, "CONNECTED FAILED");
                }

                @Override
                public void onDisconnected() {
                    //Toast.makeText(getApplicationContext(), "BEAN DISCONNECTED", Toast.LENGTH_LONG).show();
                    Log.w(TAG, "BEAN DISCONNECTED");
                }

                @Override
                public void onSerialMessageReceived(byte[] bytes) {
                    /*Toast.makeText(getApplicationContext(), "Byte - " + bytes, Toast.LENGTH_LONG).show();
                    Log.w(TAG, "Byte - " + bytes);*/
                }

                @Override
                public void onScratchValueChanged(int i, byte[] bytes) {

                }

                private void cancelBeanDiscovery() {
                    BeanManager.getInstance().cancelDiscovery();
                }
            };
        };
        BeanManager.getInstance().startDiscovery(listener);
    }

    public void testMqtt(int temperature, int sensorId) {
        String topic = "tempevents";
        String content = "{\n" +
                "  \"unitOfMeasure\": \"C\",\n" +
                "  \"partitionId\": \"A1\",\n" +
                "  \"measurementType\": \"temperature\",\n" +
                "  \"timeCreated\": \"2015-04-02T07:28:43-03:00\",\n" +
                "  \"organization\": \"NIOTH2015\",\n" +
                "  \"guid\": \"b646c5a3-b873-4910-a1ee-53168daa3341\",\n" +
                "  \"value\": " + temperature + ",\n" +
                "  \"sensorId\": \"" + sensorId + "\",\n" +
                "  \"modCamId\": \"A2\"\n" +
                "}";
        int qos = 2;
        String broker = "tcp://heatnix.cloudapp.net";
        String clientId = "Device";
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
