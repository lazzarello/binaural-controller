import monome
import pyliblo3
import asyncio

pyo_target = pyliblo3.Address(9002)

class OSCForwarder(monome.ArcApp):
    def __init__(self):
        super().__init__()
        self.pos = [0, 0, 0, 0] # our four encoders, left to right

    def on_arc_ready(self):
        print("Ready, clearing all rings")
        for n in range(0,4):
            self.arc.ring_all(n, 0)

    def on_arc_disconnect(self):
        print('Arc disconnected')

    def on_arc_delta(self, ring, delta):
        old_position = self.pos[ring]
        new_position = old_position + delta
        self.pos[ring] = new_position
        pyliblo3.send(pyo_target, "/arc/enc", int(ring + 1), self.pos[ring])
        print(f"Old Position: {old_position}, New Position: {new_position}")

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