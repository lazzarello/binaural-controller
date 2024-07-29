# Binaural Controller

This is a collection of SuperCollider and Python code to create a realtime Ambisonic control system for arbitrary monophonic audio inputs. Audio I/O is built for Linux with Pipewire-jack. Using a [Focusrite Scarlett 18i8 2nd Gen Pro](https://downloads.focusrite.com/focusrite/scarlett-2nd-gen/scarlett-18i8-2nd-gen) USB audio interface, I get ~10ms latency for microphone input + Ambisonic control.

## Getting Started

The file ambisonic-panner.sc is a prototype of the full stack of things to create a demo with various hardware inputs. It consists of the following

1. Hard coded [Pipewire-jack](https://gitlab.freedesktop.org/pipewire/pipewire/-/wikis/Config-JACK#general) nodes from the Scarlett to bus 0 and 1 in SC. Pipewire quantum settings are documented in the comments.
2. Initialize a [SerialOSCClient](https://github.com/antonhornquist/SerialOSCClient-sc) for a Monome Arc
3. Encoder/Transformer/Decoder from the [Ambisonic Tool Kit](https://www.ambisonictoolkit.net/documentation/supercollider/) (ATK)
4. A MIDI input device that can make note on/off events
5. Binaural headphones (most over-the-ear will work). [I have this AKG device.](https://www.akg.com/headphones/professional-headphones/K553MkII.html)

## Hardware Control

This depends on a [Monome Arc](https://monome.org/docs/arc/) for angle/azimuth/elevation/distance controls for the microphone input.

1. Enc1 is angle, which is how directional the soundfield is percieved. Hard right is in front, hard left is behind.
2. Enc2 is azimuth, which is where the sound is relative to your nose (roughly).
3. Enc3 is elevation, which is how high the sound energy is relative to your nose(?).
4. Enc4 is distance from you, which is simply the volume of the sound source.

The MIDI keyboard can play a simple polyphonic syntheziser. Move your voice around to compare its location to the synth! Fun!

## TODO

* Arc LED indication
* Use the ControlSpec from SerialOSCClient for param scaling
* Create an ambisonic bus in SC for the MIDI synth so each note doesn't create a whole new soundfield pipeline
* Enable hardware controls for all channels

## Planned Features

* Monome Grid positioning of channels by mapping a 2D plane to a 3D soundfield
* Gen AI speech synthesis with [XTTS V2 model](https://github.com/lazzarello/TTS)
* Gen AI music input from [Facebook Musicgen](https://huggingface.co/facebook/musicgen-small) open source model
* Higher order Ambisonics with Near-Field controller HOA

## Python Code?

The Python code was a first attempt to get to the same place as the SuperCollider code, but using the [Pyo DSP framework](https://github.com/belangeo/pyo). Python also has PyTorch for gen AI model inference. I envision two controllers, one doing text-to-speech with gen AI and sending audio over a Pipewire source (or analog out from a second host with a bigger GPU) into a Pipewire sink on the same host. The other doing the Ambisonic DSP and hardware controls.
