(
s = Server.default;
// The name of the node in the Pipewire graph
s.options.device = "arc-demo";
// Connect the first two outputs to the first two channels of the scarlett device
"SC_JACK_DEFAULT_OUTPUTS".setenv("Scarlett 18i8 2nd Gen Pro:playback_AUX0,Scarlett 18i8 2nd Gen Pro:playback_AUX1");
"SC_JACK_DEFAULT_INPUTS".setenv("Scarlett 18i8 2nd Gen Pro:capture_AUX0,Scarlett 18i8 2nd Gen Pro:capture_AUX0");
// this is ignored by Pipewire, set latency as an env var before booting the scide application like so
// PIPEWIRE_LATENCY=512/48000 scide
/* on the Thinkpad with the Scarlett, the SC server won't run with a quantum lower than 512. the error output is "Jul 25 15:22:28 wendy pipewire[5383]: mod.profiler: 0x7ebe50000010: queue full 512 < 672"
*/
s.options.blockSize = 512;
s.boot;

// SerialOSCEnc.testLeds;
);
// SerialOSC.trace;
// a press or release of any button on any attached grid posts event state information to Post Window
// GridKeydef(\test, { |x, y, state| (if (state == 1, "key down", "key up") + "at (%,%)".format(x, y)).postln });
// GridKeydef(\test).free; // or CmdPeriod frees responder
// setting this in in the function def resets it every time
(
SerialOSCClient.init;
// Define microphone in synth
SynthDef.new(\micInput, { |out = 0, azimuth = 0, distance = 1 |
	Out.ar(out,
		Balance2.ar(
			SoundIn.ar(out, distance),
			SoundIn.ar(out, distance),
			azimuth
		)
	);
}).add;
// Create synth
x = Synth(\micInput, [\azimuth, 0, \distance, 1]);
)

x.free;
// Initialize defaults for rings)
(
var ring_values = [0,0,0,0];
// Define callback function for encoder events
a = EncDeltadef(\aziPan, {|ring, delta|
	var min = -512;
	var max = 512;
	var scaled_value;
	// holy shit control structures in SC lang SUCK!
	case
	// going down
	{delta < 0} {
		var current_value = ring_values[ring];
		if (current_value + delta < min) {
			ring_values[ring] = min;
		} {
			ring_values[ring] = ring_values[ring] + delta;
		};
	}
	// going up
	{delta > 0} {

		var current_value = ring_values[ring];
		if (current_value + delta > max) {
			ring_values[ring] = max;
		} {
			ring_values[ring] = ring_values[ring] + delta;
		};
	};
	scaled_value = ring_values[ring].linlin(min, max, -pi, pi);
	"encoder number (%): azimuth (%)".format(ring, scaled_value).postln;
	x.set(\azimuth, scaled_value);
}, 0);
b = EncDeltadef(\distancePan, {|ring, delta|
	var min = -512;
	var max = 512;
	var scaled_value;
	// holy shit control structures in SC lang SUCK!
	case
	// going down
	{delta < 0} {
		var current_value = ring_values[ring];
		if (current_value + delta < min) {
			ring_values[ring] = min;
		} {
			ring_values[ring] = ring_values[ring] + delta;
		};
	}
	// going up
	{delta > 0} {
		var current_value = ring_values[ring];
		if (current_value + delta > max)
		{ring_values[ring] = max}
		{ring_values[ring] = ring_values[ring] + delta };
	};
	scaled_value = ring_values[ring].linlin(min, max, 0, 1);
	"encoder number (%): distance (%)".format(ring, scaled_value).postln;
	x.set(\distance, scaled_value);
}, 1);
)
// send microphone input 1 (set with default input above) to output, through SC graph. Will have latency set to the default Pipewire quantum of 1024.
x.free;
a.free;
b.free;
s.quit;