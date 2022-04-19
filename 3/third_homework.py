import RPi.GPIO as GPIO
import time
import json
import sender
from mfrc522 import SimpleMFRC522
 
G_LED_PIN = 17
R_LED_PIN = 27
 
passwords = ["123", "12345", "qwerty"]
 
GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)
reader = SimpleMFRC522()
GPIO.setup(G_LED_PIN, GPIO.OUT)
GPIO.setup(R_LED_PIN, GPIO.OUT)
 
def read_rfid():
    id, text = reader.read()
    return id,text
 
def password_checking_signal(is_passed):
    if is_passed:
        GPIO.output(G_LED_PIN , GPIO.HIGH)
        time.sleep(1)
        GPIO.output(G_LED_PIN, GPIO.LOW)
    else:
        GPIO.output(R_LED_PIN , GPIO.HIGH)
        time.sleep(1)
        GPIO.output(R_LED_PIN, GPIO.LOW)
 
try:
    while True:
        id, text = reader.read()
        id = str(id)
        password = str(text).strip()
        is_correct = password in passwords
        print("User ID: " + id)
        print("Password: " + password)
        print("Is the password correct: " + str(is_correct))
        password_checking_signal(is_correct)
        
        result = json.dumps({
            "user_id": id, 
            "password": password, 
            "is_correct": is_correct
        })
        sender.send_message(result)
finally:
        GPIO.cleanup()