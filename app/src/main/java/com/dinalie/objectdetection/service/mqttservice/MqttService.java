package com.dinalie.objectdetection.service.mqttservice;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public abstract class MqttService {


    private static final String TAG = "class-MQTT";
    private Context context;
    private static MqttClient client;
    private static MqttService mqttService;


    public MqttService(Context context, String clientId, String serverUrl, String subscribeTopic) {
        this.context = context;

        if (client == null) {
            createClient(clientId, serverUrl, subscribeTopic);
        }
    }

    private void createClient(String clientId, String serverUrl, String subscribeTopic) {

        try {
            client = new MqttClient(serverUrl, clientId, new MemoryPersistence());
            client.setCallback(callback);
            client.connect();
            client.subscribe(subscribeTopic);

        } catch (MqttException e) {
            e.printStackTrace();
        }

    }


    private MqttCallback callback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            Log.d(TAG, "connectionLost");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            getReceivedMessage(topic, message);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            Log.d(TAG, "deliveryComplete");
        }
    };


    public void publishData(String topic, byte[] arr) {
        try {
            MqttMessage message = new MqttMessage(arr);
            client.publish(topic, message);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected abstract void getReceivedMessage(String topic, MqttMessage message);

}
