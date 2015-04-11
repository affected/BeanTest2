package com.affecto.beantest2;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.widget.Toast;

import java.util.Collection;

import nl.littlerobots.bean.*;
import nl.littlerobots.bean.message.Callback;


public class MainActivity extends ActionBarActivity {

    /**
     * TAG = "BeanSDK". Used for debug messages.
     */
    private static final String TAG = "BeanTest2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create a listener
        BeanDiscoveryListener listener = new BeanDiscoveryListener() {
            @Override
            public void onBeanDiscovered(Bean bean) {
                BeanManager.getInstance().cancelDiscovery();
                bean.disconnect();
                Toast.makeText(getApplicationContext(), "Bean discovered - " + this, Toast.LENGTH_LONG).show();
                Log.w(TAG, "Bean discovered - " + bean.getDevice());
                bean.connect(getApplicationContext(), myBeanListener);

                // after the connection is instantiated, briefly flash the led:
                bean.setLed(255, 0, 0);
                bean.setLed(0, 255, 0);
                bean.setLed(0, 0, 255);
                bean.setLed(0, 0, 0);

                bean.readTemperature(new Callback<Integer>() {
                    @Override
                    public void onResult(Integer integer) {
                        Toast.makeText(getApplicationContext(), "Temperature: " + integer, Toast.LENGTH_LONG).show();
                        Log.w(TAG, "Temperature: " + integer);
                    }
                });

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
