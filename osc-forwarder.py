import monome
import pyliblo3
import asyncio

pyo_target = pyliblo3.Address(9002)

class OSCForwarder(monome.ArcApp):
    def __init__(self):
        super().__init__()

    def update(self, delta):
        self.accumulated_value += delta
        return self.scale_value(self.accumulated_value)

    def scale_value(self, value):
        if self.in_min == self.in_max:
            raise ValueError("Input range cannot be zero")
        scaled_value = (value - self.in_min) * (self.out_max - self.out_min) / (self.in_max - self.in_min) + self.out_min
        return scaled_value

    def on_arc_ready(self):
        print("Ready, clearing all rings")
        for n in range(0,4):
            self.arc.ring_all(n, 0)

    def on_arc_disconnect(self):
        print('Arc disconnected')

    def on_arc_delta(self, ring, delta):
        if ring == 0:
            pyliblo3.send(pyo_target, "/pyo/sine/freq", delta)
        if ring == 1:
            pyliblo3.send(pyo_target, "/pyo/azi/degree", delta)
        if ring == 2:
            pyliblo3.send(pyo_target, "/pyo/ele/degree", delta)

async def main():
    loop = asyncio.get_running_loop()
    app = OSCForwarder()

    # Monome device discovery
    def serialosc_device_added(id, type, port):
        if 'arc' not in type:
            print(f'ignoring {id} ({type}) as device does not appear to be an arc')
            return
        print(f'connecting to {id} ({type})')
        asyncio.create_task(app.arc.connect('127.0.0.1', port))        

    serialosc = monome.SerialOsc()
    serialosc.device_added_event.add_handler(serialosc_device_added)

    # connect to the serialosc device port
    await serialosc.connect()
    # create a co-routine?
    await loop.create_future()

if __name__ == '__main__':
    asyncio.run(main())