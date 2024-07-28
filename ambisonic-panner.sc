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

// Neat GUI for all the transform methods
// ~display = FoaXformDisplay();
~encoder = FoaEncoderMatrix.newOmni;
~decoder = FoaDecoderKernel.newUHJ;                         // UHJ (kernel)
~transformer = 'push';

(
MIDIClient.init;
MIDIIn.connectAll;
MIDIClient.sources;

// Polyphonic synthesizer
~bend = 8192;
~notes = Array.newClear(128);
MIDIdef.noteOn(\noteOnTest, {|vel,nn,chan,src|
	[vel,nn].postln;
	~notes[nn] = Synth.new(\tone, [
		\freq, nn.midicps,
		\amp, vel.linexp(1,127,0.01,0.3),
		\gate,1,
		\bend, ~bend.linlin(0,16383, -2, 2)
	]);
});

MIDIdef.noteOff(\noteOffTest, {|vel,nn|
	[vel,nn].postln;
	~notes[nn].set(\gate, 0);
	~notes[nn] = nil;
});

MIDIdef.bend(\bendTest, {|val, chan, src|
	[val,chan,src].postln;
	~bend = val;
	~notes.do{|synth| synth.set(\bend, val.linlin(0,16383, -2, 2))};
}, chan:0);
SynthDef.new(\micInput, { |out = 0,
	                       angle= 0,
	                       azimuth = 0,
	                       elevation = 0,
	                       distance = 1|
	Out.ar(out,
		FoaDecode.ar(
		    FoaTransform.ar(
			    FoaEncode.ar(
				    SoundIn.ar(out, distance),
				    ~encoder
		        ), ~transformer, angle, azimuth, elevation
			), ~decoder
		)
	);
}).add;

SynthDef.new(\tone, {|freq=440, amp=0.3, gate=0, bend=0|
	var sig, env;
	// sig = LFTri.ar(freq * bend.midiratio)!2;
	sig = LFTri.ar(freq * bend.midiratio);
	env = EnvGen.kr(Env.adsr, gate, doneAction:2);
	sig = sig * env * amp;
	Out.ar(0,
		FoaDecode.ar(
			FoaTransform.ar(
				FoaEncode.ar(
					sig,
					~encoder
				), ~transformer, -1.4, 2.3, -0.26
			),~decoder
		)
	);
}).add;

~ring_values = [0,0,0,0];
// Define callback function for encoder events
EncDeltadef(\anglePan, {|ring, delta|
	var min = -512;
	var max = 512;
	var scaled_value;
	case
	// going down
	{delta < 0} {
		var current_value = ~ring_values[ring];
		if (current_value + delta < min)
		{~ring_values[ring] = min}
		{~ring_values[ring] = current_value + delta};
	}
	// going up
	{delta > 0} {
		var current_value = ~ring_values[ring];
		if (current_value + delta > max)
		{~ring_values[ring] = max}
		{~ring_values[ring] = current_value + delta};
	};
	scaled_value = ~ring_values[ring].linlin(min, max, -pi/2, pi/2);
	"encoder number (%): angle (%)".format(ring, scaled_value).postln;
	x.set(\angle, scaled_value);
}, 0);
EncDeltadef(\aziPan, {|ring, delta|
	var min = -512;
	var max = 512;
	var scaled_value;
	case
	// going down
	{delta < 0} {
		var current_value = ~ring_values[ring];
		if (current_value + delta < min)
		{~ring_values[ring] = min}
		{~ring_values[ring] = current_value + delta};
	}
	// going up
	{delta > 0} {
		var current_value = ~ring_values[ring];
		if (current_value + delta > max)
		{~ring_values[ring] = max}
		{~ring_values[ring] = current_value + delta};
	};
	scaled_value = ~ring_values[ring].linlin(min, max, pi, -pi);
	"encoder number (%): azimuth (%)".format(ring, scaled_value).postln;
	x.set(\azimuth, scaled_value);
}, 1);
EncDeltadef(\elevationPan, {|ring, delta|
	var min = -512;
	var max = 512;
	var scaled_value;
	case
	// going down
	{delta < 0} {
		var current_value = ~ring_values[ring];
		if (current_value + delta < min)
		{~ring_values[ring] = min}
		{~ring_values[ring] = current_value + delta};
	}
	// going up
	{delta > 0} {
		var current_value = ~ring_values[ring];
		if (current_value + delta > max)
		{~ring_values[ring] = max}
		{~ring_values[ring] = current_value + delta };
	};
	scaled_value = ~ring_values[ring].linlin(min, max, pi, -pi);
	"encoder number (%): elevation (%)".format(ring, scaled_value).postln;
	x.set(\elevation, scaled_value);
}, 2);
EncDeltadef(\distancePan, {|ring, delta|
	var min = -512;
	var max = 512;
	var scaled_value;
	case
	// going down
	{delta < 0} {
		var current_value = ~ring_values[ring];
		if (current_value + delta < min)
		{~ring_values[ring] = min}
		{~ring_values[ring] = current_value + delta};
	}
	// going up
	{delta > 0} {
		var current_value = ~ring_values[ring];
		if (current_value + delta > max)
		{~ring_values[ring] = max}
		{~ring_values[ring] = current_value + delta };
	};
	scaled_value = ~ring_values[ring].lincurve(min, max, 0, 1);
	"encoder number (%): distance (%)".format(ring, scaled_value).postln;
	x.set(\distance, scaled_value);
}, 3);
)
// Make a synth after the defs
x = Synth(\micInput, [\angle, 0,
	                  \azimuth, 0,
	                  \distance, 1,
	                  \elevation, 0]
);
x.free;