from RPi import GPIO
from time import sleep

G_LED_PIN = 17
R_LED_PIN = 22
B_LED_PIN = 27
clk = 2
dt = 3

GPIO.setmode(GPIO.BCM)
GPIO.setup(G_LED_PIN, GPIO.OUT)
GPIO.setup(R_LED_PIN, GPIO.OUT)
GPIO.setup(B_LED_PIN, GPIO.OUT)
GPIO.setup(clk, GPIO.IN, pull_up_down=GPIO.PUD_DOWN)
GPIO.setup(dt, GPIO.IN, pull_up_down=GPIO.PUD_DOWN)

counter = 0
clkLastState = GPIO.input(clk)

def lamp(led_pin):
        GPIO.output(led_pin , GPIO.HIGH)

try:
        lastState = R_LED_PIN
        while True:
                clkState = GPIO.input(clk)
                dtState = GPIO.input(dt)
                if clkState != clkLastState:
                        GPIO.output(lastState, GPIO.LOW)
                        counter += 1
                        print(counter)
                        if (counter % 3 == 0):
                            lamp(R_LED_PIN)
                            lastState = R_LED_PIN
                        elif (counter % 2 == 0):
                            lamp(G_LED_PIN)
                            lastState = G_LED_PIN
                        else:
                            lamp(B_LED_PIN)
                            lastState = B_LED_PIN
                clkLastState = clkState
                sleep(0.01)

finally:
        GPIO.cleanup()