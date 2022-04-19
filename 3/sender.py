import time
import paho.mqtt.client as mqtt
import paho.mqtt.publish as publish
 
Broker = "broker.hivemq.com"
broker_port = 1883
 
pub_topic = "itis/data"
 
import paho.mqtt.client as mqtt
import time
 
client = mqtt.Client()
 
def on_connect():
    print(f"Connected with result code {rc}")
 
 
def on_publish(payload):
    client.publish(pub_topic, payload, 0, False)
    print("Send " + str(payload) + " to " + pub_topic)
 
 
def send_message(message):
    client.on_connect = on_connect
    client.connect(Broker, broker_port, 60)
    on_publish(message)