package com.example.sw_project.domain.mqtt

import android.content.Context
import android.util.Log
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import java.nio.charset.StandardCharsets


class MqttClient(
    private val context: Context,
    private val brokerUrl: String,
    private val topic: String,
    private val onMessage: (String, ByteArray) -> Unit
) {
    private val client: Mqtt3AsyncClient = Mqtt3Client.builder()
        .identifier("MqttClient-${System.currentTimeMillis()}")
        .serverHost(brokerUrl.split("//")[1].split(":")[0])
        .serverPort(brokerUrl.split(":")[2].toInt())
        .buildAsync()

    init {
        connect()
    }

    private fun connect() {
        client.connectWith().send().whenComplete { _, throwable ->
            if (throwable != null) {
                Log.d("Error","Failed to connect: ${throwable.message}")
                reconnect()
            } else {
                Log.d("Success","Connected successfully")
                subscribe()
            }
        }
    }

    private fun subscribe() {
        client.subscribeWith()
            .topicFilter(topic)
            .callback { publish ->
                onMessage(publish.topic.toString(), publish.payloadAsBytes)
            }
            .send()
            .whenComplete { _, throwable ->
                if (throwable != null) {
                    Log.d("Error","Failed to subscribe to topic: ${throwable.message}")

                } else {
                    Log.d("Topic","Subscribed to topic: $topic")
                }
            }
    }

    private fun reconnect() {
        client.connectWith().send().whenComplete { _, throwable ->
            if (throwable != null) {

            } else {
                subscribe()
            }
        }
    }

    fun publish(topic: String, message: String) {
        val publishMessage = Mqtt3Publish.builder()
            .topic(topic)
            .payload(message.toByteArray(StandardCharsets.UTF_8))
            .build()

        client.publish(publishMessage).whenComplete { _, throwable ->
            if (throwable != null) {
                Log.d("Error","Failed to publish message: ${throwable.message}")

            } else {
                Log.d("Topic","Message published to topic: $topic")
            }
        }
    }

    fun disconnect() {
        client.disconnect().whenComplete { _, throwable ->
            if (throwable != null) {
                Log.d("Error","Failed to disconnect: ${throwable.message}")
            } else {
                Log.d("Success","Disconnected successfully")
            }
        }
    }
}