from pyo import *

port = 9002
s = Server(audio='jack', sr=48000, jackname='binaural-sampler')
s.boot().start()
sine = Sine(freq=440, mul=0.3)
# add a sample player here, like in the older file
azi = 0
ele = 0
binaural_out = Binaural(sine, azimuth=azi, elevation=ele).out()

def scale_value(value, in_min, in_max, out_min, out_max):
    if in_min == in_max:
        raise ValueError("Input range cannot be zero")
    # Scale the value, this came from GPT-4 and it doesn't work :(
    scaled_value = (value - in_min) * (out_max - out_min) / (in_max - in_min) + out_min
    return scaled_value

def printInputMessage(address, *args):
    print("Address =", address)
    print("Values =", args)
    print("---------------")

def getArcData(address, *args):
    if args[0] == 1:
        sine.freq = args[1]
        print(f"Frequency is {sine.freq}")
    elif args[0] == 2:
        current_value = scale_value(args[1], -1024, 1024, 0, 90)
        binaural_out.elevation = current_value
        print(f"Let's scale this value to set the elevation to {current_value}")
    elif args[0] == 3:
        current_value = scale_value(args[1], -1024, 1024, -180, 180)
        binaural_out.azimuth = current_value
        print(f"Let's scale this value to set the azimuth to {current_value}")

data = OscDataReceive(port=port, address="*", function=getArcData)
s.gui(locals())