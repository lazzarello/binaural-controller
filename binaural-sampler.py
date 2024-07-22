from pyo import *

port = 9002
s = Server(audio='jack', sr=48000, jackname='binaural-sampler')
s.boot().start()
sine = Sine(freq=440, mul=0.3)
azi = 0
ele = 0
binaural_out = Binaural(sine, azimuth=azi, elevation=ele).out()


def printInputMessage(address, *args):
    print("Address =", address)
    print("Values =", args)
    print("---------------")

def getArcData(address, *args):
    if args[0] == 1:
        sine.freq = args[1]
        print(f"Frequency is {sine.freq}")
    if args[0] == 2:
        print("Let's scale this value to set the elevation")

data = OscDataReceive(port=port, address="*", function=getArcData)
s.gui(locals())