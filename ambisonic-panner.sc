(
s = Server.default;
// The name of the node in the Pipewire graph
s.options.device = "ambisonic-panner";
// Connect the first two outputs to the first two channels of the scarlett device
// "SC_JACK_DEFAULT_OUTPUTS".setenv("iyo vad pro uap media test Analog Stereo:playback_FL,iyo vad pro uap media test Analog Stereo:playback_FR");
// "SC_JACK_DEFAULT_INPUTS".setenv("iyo vad pro uap media test:capture_FL,iyo vad pro uap media test:capture_FR");
"SC_JACK_DEFAULT_OUTPUTS".setenv("Scarlett 18i8 2nd Gen Pro:playback_AUX0,Scarlett 18i8 2nd Gen Pro:playback_AUX1");
"SC_JACK_DEFAULT_INPUTS".setenv("Scarlett 18i8 2nd Gen Pro:capture_AUX0,Scarlett 18i8 2nd Gen Pro:capture_AUX0");

// this is ignored by Pipewire, set latency as an env var before booting the scide application like so
// PIPEWIRE_LATENCY=512/48000 scide
/* on the Thinkpad with the Scarlett, the SC server won't run with a quantum lower than 512. the error output is "Jul 25 15:22:28 wendy pipewire[5383]: mod.profiler: 0x7ebe50000010: queue full 512 < 672"
*/
s.options.blockSize = 512;
s.boot;
SerialOSCClient.init;
)

~encoder = FoaEncoderMatrix.newOmni;
~decoder = FoaDecoderKernel.newUHJ;                         // UHJ (kernel)
~transformer = 'zoomX';
~transformer = 'push';
~renderDecode = { arg in, decoder;
    var kind;
    kind = decoder.kind;
    FoaDecode.ar(in, decoder);
}

SynthDef.new(\micInput, { |out = 0,
	                       azimuth = 0,
	                       distance = 1,
	                       elevation = 0|
	Out.ar(out,
		FoaDecode.ar(
		    FoaTransform.ar(
			    FoaEncode.ar(
				    SoundIn.ar(out, distance),
				    ~encoder
		        ), ~transformer, elevation, azimuth
			), ~decoder
		)
	);
}).add;
x = Synth(\micInput, [\azimuth, 0,
	                  \distance, 1,
	                  \elevation, 0])
x.set(\azimuth, pi);
x.free;
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
	scaled_value = ring_values[ring].lincurve(min, max, 0, 1);
	"encoder number (%): distance (%)".format(ring, scaled_value).postln;
	x.set(\distance, scaled_value);
}, 2);
b = EncDeltadef(\elevationPan, {|ring, delta|
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
	scaled_value = ring_values[ring].linlin(min, max, pi/2, 0);
	"encoder number (%): elevation (%)".format(ring, scaled_value).postln;
	x.set(\elevation, scaled_value);
}, 1);
)
// Some other options for stereo decoders and transformers
// stereophonic / binaural
~decoder = FoaDecoderMatrix.newStereo((131/2).degrad, 0.5) // Cardioids at 131 deg
~decoder = FoaDecoderKernel.newSpherical                   // synthetic binaural (kernel)
~decoder = FoaDecoderKernel.newCIPIC                       // KEMAR binaural (kernel)
~transformer = 'pushX'
~transformer = 'directO'
x.free;
s.quit;