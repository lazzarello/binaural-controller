from pyo import *
import ArcEncoder

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

def update(self, delta):
    self.accumulated_value += delta
    return self.scale_value(self.accumulated_value)

def getArcData(address, *args):
    # move all accumulator logic into osc-forwarder
    if address == "/pyo/sine/freq":
        sine.freq = args[0] + sine.freq
        print(f"Frequency is {sine.freq}")
    if address == "/pyo/ele/degree":
        current_value = scale_value(args[0], 0, 90, 0, 90)
        binaural_out.elevation = current_value
        print(f"Let's scale this value to set the elevation to {current_value}")
    if address == "/pyo/azi/degree":
        current_value = scale_value(args[0], -180, 180, -180, 180)
        binaural_out.azimuth = current_value
        print(f"Let's scale this value to set the azimuth to {current_value}")
    if address == "/pyo/voice/gain":

data = OscDataReceive(port=port, address="*", function=getArcData)
s.gui(locals())