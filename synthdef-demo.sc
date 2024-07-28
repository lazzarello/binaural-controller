{ SoundIn.ar(0, [0.1,0.1])}.play;
SynthDef.new(\micInput, { |out = 0, pan = 0|
	Out.ar(out,
		Balance2.ar(SoundIn.ar(out, 1),SoundIn.ar(out, 1), pan));
}).add
x = Synth(\micInput, [\pan, 0]);
MIDIClient.init;
MIDIIn.connectAll;
// still can't put a .kr signal anywhere...what am I missing?
x.set(\pan, 0);
x.free;









(
var notes, on, off;

notes = Array.newClear(128);    // array has one slot per possible MIDI note

on = MIDIFunc.noteOn({ |veloc, num, chan, src|
    notes[num] = Synth(\default, [\freq, num.midicps,
        \amp, veloc * 0.00315]);
});

off = MIDIFunc.noteOff({ |veloc, num, chan, src|
    notes[num].release;
});

q = { on.free; off.free; };
)