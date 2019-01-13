# WaveSynUI

This is a software synthesizer modeled after the famous PPG. It is a true wavetable synth- it scans a table full of short audio samples, adding them to an audio buiffer as it goes, then plays the buffer. 

Features
- 125 wavetables of 64 512 byte samples each from https://waveeditonline.com/
- 16 bit, 44.1khz audio
- monophonic
- 30 notes (but planning to add 4 more octaves)

There are no effects or envelopes, since this is meant to be played through other synthesizers that can accept and manipulate an external audio source. For example, a Moog Sub 37 can mix in external audio, and apply its great filter, amplitude/filter envelopes, arpeggiator, etc. to the external sound.

This uses Java and JaxaFX for the UI.
I am not using Maven so you will need to install two libraries into your project:

- Tarsos DSP
- json simple 1.1

