import RPi.GPIO as GPIO
import time
import datetime
import json
import sender
from mfrc522 import SimpleMFRC522
from gpiozero import MotionSensor
from threading import Thread
from threading import Lock
 
 
G_LED_PIN = 17
R_LED_PIN = 27
PIR_PIN = 21
 
IS_BLOCKED = False
passwords = ["123", "12345", "qwerty"]
 
lock = Lock()
 
GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)
reader = SimpleMFRC522()
pir = MotionSensor(PIR_PIN)
GPIO.setup(G_LED_PIN, GPIO.OUT)
GPIO.setup(R_LED_PIN, GPIO.OUT)
 
 
def alert_lamp():
    for x in range(5):
        GPIO.output(R_LED_PIN , GPIO.HIGH)
        time.sleep(0.2)
        GPIO.output(R_LED_PIN, GPIO.LOW)
        GPIO.output(G_LED_PIN , GPIO.HIGH)
        time.sleep(0.2)
        GPIO.output(G_LED_PIN, GPIO.LOW)
 
def password_checking_signal(is_passed):
    if is_passed:
        GPIO.output(G_LED_PIN , GPIO.HIGH)
        time.sleep(1)
        GPIO.output(G_LED_PIN, GPIO.LOW)
    else:
        GPIO.output(R_LED_PIN , GPIO.HIGH)
        time.sleep(1)
        GPIO.output(R_LED_PIN, GPIO.LOW)
 
def get_alert_message():
    return json.dumps({
            "topic": "itis/alert",
            "message": "Person detected",
            "date_time": str(datetime.datetime.now())
        })
 
def get_auth_result(user_id, password, is_authorized):
    return json.dumps({
            "topic": "itis/data",
            "user_id": user_id, 
            "password": password, 
            "is_authorized": is_authorized,
            "date_time": str(datetime.datetime.now())
        })
 
def log(string):
    print(str(datetime.datetime.now()) + " " + string)
 
def read_rfid():
    id, text = reader.read()
    return id,text
 
def rfid():
    while True:
        id, text = reader.read()
        global IS_BLOCKED
        with lock:
            IS_BLOCKED = True
        id = str(id)
        password = str(text).strip()
        is_authorized = password in passwords
        log(
            "User ID: " + id + 
            "\nPassword: " + password + 
            "\nIs the password correct: " + str(is_authorized)
            )
        password_checking_signal(is_authorized)
        result = get_auth_result(id, password, is_authorized)
        sender.send_auth_message(result)
        if is_authorized == True:
            time.sleep(10)
        with lock:
            IS_BLOCKED = False
 
def motion():
    while True:
        global IS_BLOCKED
        print(IS_BLOCKED)
        if pir.motion_detected == True and IS_BLOCKED == False:
            log('Person detected')
            alert_lamp()
            message = get_alert_message()
            sender.send_alert_message(message)
            time.sleep(3)
 
def main():
    try:
        rfid_thread = Thread(target = rfid)
        motion_thread = Thread(target = motion)
        
        rfid_thread.start()
        motion_thread.start()
    
        rfid_thread.join()
        motion_thread.join()
    
        while True:
            pass
    finally:
        GPIO.cleanup()
    
    
if __name__ == '__main__':
    main()
