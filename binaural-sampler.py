from pyo import *

port = 9002
s = Server(audio='jack', sr=48000, jackname='binaural-sampler')
s.boot().start()
sine = Sine(freq=440, mul=0.3).out()

def printInputMessage(address, *args):
    print("Address =", address)
    print("Values =", args)
    print("---------------")

def getArcData(address, *args):
    sine.freq = args[1]
    print(f"Frequency is {sine.freq}")

data = OscDataReceive(port=port, address="*", function=getArcData)
s.gui(locals())