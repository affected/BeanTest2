package com.affecto.beantest2;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

import nl.littlerobots.bean.*;
import nl.littlerobots.bean.message.Callback;


public class MainActivity extends ActionBarActivity {

    /**
     * TAG = "BeanSDK". Used for debug messages.
     */
    private static final String TAG = "BeanTest2";

    public Bean myBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create a listener
        BeanDiscoveryListener listener = new BeanDiscoveryListener() {
            @Override
            public void onBeanDiscovered(Bean bean) {
                BeanManager.getInstance().cancelDiscovery();

                Toast.makeText(getApplicationContext(), "Bean discovered - " + this, Toast.LENGTH_LONG).show();
                Log.w(TAG, "Bean discovered - " + bean.getDevice());
                bean.connect(getApplicationContext(), myBeanListener);

                // after the connection is instantiated, briefly flash the led:
                bean.setLed(255,0,0);

                bean.readTemperature(new Callback<Integer>() {
                    @Override
                    public void onResult(Integer integer) {
                        Toast.makeText(getApplicationContext(), "Temperature: " + integer, Toast.LENGTH_LONG).show();
                        Log.w(TAG, "Temperature: " + integer);
                        testMqtt(integer);
                    }
                });

                bean.setLed(0, 0, 0);

                // when you're done, don't forget to disconnect
                //bean.disconnect();
            }

            @Override
            public void onDiscoveryComplete() {
                int numbre = BeanManager.getInstance().getBeans().size();
                Collection<Bean> beans = BeanManager.getInstance().getBeans();

                Toast.makeText(getApplicationContext(), numbre + " Beans Found", Toast.LENGTH_LONG).show();
                Log.w(TAG, numbre + " Beans Found");

            }

            BeanListener myBeanListener = new BeanListener() {
                @Override
                public void onConnected() {
                    Toast.makeText(getApplicationContext(), "CONNECTED TO BEAN", Toast.LENGTH_LONG).show();
                    Log.w(TAG, "CONNECTED TO BEAN");

                }

                @Override
                public void onConnectionFailed() {
                    Toast.makeText(getApplicationContext(), "CONNECTED FAILED", Toast.LENGTH_LONG).show();
                    Log.w(TAG, "CONNECTED FAILED");
                }

                @Override
                public void onDisconnected() {
                    Toast.makeText(getApplicationContext(), "BEAN DISCONNECTED", Toast.LENGTH_LONG).show();
                    Log.w(TAG, "BEAN DISCONNECTED");
                }

                @Override
                public void onSerialMessageReceived(byte[] bytes) {
                    Toast.makeText(getApplicationContext(), "Byte - " + bytes, Toast.LENGTH_LONG).show();
                    Log.w(TAG, "Byte - " + bytes);
                }

                @Override
                public void onScratchValueChanged(int i, byte[] bytes) {

                }

                private void cancelBeanDiscovery() {
                    BeanManager.getInstance().cancelDiscovery();
                }
            };
        };

        // Assuming "this" is an activity or service:
        BeanManager.getInstance().startDiscovery(listener);
    }


    public void testMqtt( int temperature) {
        String topic = "test";
        String content = "{\n" +
                "  \"unitOfMeasure\": \"C\",\n" +
                "  \"partitionId\": \"A1\",\n" +
                "  \"measurementType\": \"temperature\",\n" +
                "  \"timeCreated\": \"2015-04-02T07:28:43-03:00\",\n" +
                "  \"organization\": \"NIOTH2015\",\n" +
                "  \"guid\": \"b646c5a3-b873-4910-a1ee-53168daa3341\",\n" +
                "  \"value\": "+temperature+",\n" +
                "  \"sensorId\": \"bean3\",\n" +
                "  \"modCamId\": \"A2\"\n" +
                "}";
        int qos = 2;
        String broker = "tcp://mqtt.dev.mikael.io:1883";
        String clientId = "AffectoO";
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
