SerialOSCClient.init;
SerialOSCClient.devices;
SerialOSCClient.free;
g = SerialOSCGrid.default;
g.ledSet(0,1,0);
g.rotation;
g.ledXSpec;
g.ledAll(0);
e = SerialOSCEnc.default;
e.ringSet(0,32,15);
e.ringAll(0,0);
e.id;
e.type;
e.port;
e.numRings;
e.nSpec;

// Neat! put the sound parts in the Func. Prolly have to do some MIDI tutorials for CC values as reference for Arc encoders
a = GridKeyFunc.press({ g = { Balance2.ar(SoundIn.ar(0), SoundIn.ar(0), MouseX.kr(-1,1))}.play;
	SerialOSCGrid.all.first.ledSet(15,7,1);
}, 15,7);
b = GridKeyFunc.release({ g.release;
	SerialOSCGrid.all.first.ledSet(15,7,0);
}, 15,7);

h=Array.fill(16);
i=GridKeyFunc.press({ |x, y, state, timestamp, device|
	h[y] = (degree: y).play });
j=GridKeyFunc.release({ |x, y, state, timestamp, device| h[y].release });

y=SerialOSCClient.enc("My encoder app") { |client| // DSL style
    client.encDeltaAction = { |client, n, delta|
        "encoder number (%): delta (%)".format(n, delta).postln
    };
}

// from the SerialOSCClient help file example
(
var set_arc_led;
var scramble_8_grid_leds;
var b_spec = ControlSpec.new;

// Ensure server is running
s.serverRunning.not.if {
    Error("Boot server stored in interpreter variable s.").throw
};

// Initialize client in order to use devices
SerialOSCClient.init;

// SynthDef to server
SynthDef(\test, { |freq, gate=1| Out.ar(0, ( SinOsc.ar(Lag.kr(freq)) * EnvGen.ar(Env.cutoff, gate) * 0.1) ! 2) }).add;

// TODO: why doesn't this work with the encoder?
SynthDef(\micInput, { |pan| Out.ar(0,
	Balance2.ar(SoundIn.ar(0), SoundIn.ar(0), Lag.kr(pan)))
 }).add;

// Function to visualize a float value 0 - 1.0 on first encoder ring of default arc (if attached)
set_arc_led = { |value|
    SerialOSCEnc.default !? { |enc|
        enc.clearRings;
        enc.ringSet(0, SerialOSCEnc.ledXSpec.map(value), 15);
    };
};

// Function to scramble state for 8 random buttons in a 8x8 led matrix on the default grid (if attached)
scramble_8_grid_leds = {
    SerialOSCGrid.default !? { |grid|
        8 do: { grid.ledSet(8.rand, 8.rand, [true, false].choose) };
    };
};

// Initial arc encoder setting
b = 0.5;
set_arc_led.(b);

// First Arc encoder control pan of mic input and scrambles 8 leds
EncDeltadef(\adjustPan, { |n, delta|
    b = b_spec.constrain(b + (delta/1000));
    a !? { a.set(\pan, \pan.asSpec.map(b)) };
	"encoder 1: value (%)".format(b).postln;
    set_arc_led.(b);
    scramble_8_grid_leds.();
}, 0);

// Hitting any grid button opens the mic input and scrambles 8 leds
GridKeydef.press(
    \openMic,
    {
        a ?? {
            a = Synth(\micInput, [\pan, \pan.asSpec.map(b)]);
            scramble_8_grid_leds.();
        };
    }
);

// Releasing any grid button closes the mic input
GridKeydef.release(\closeMic, { a.release; a = nil });
)