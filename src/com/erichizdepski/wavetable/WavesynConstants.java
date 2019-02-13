package com.erichizdepski.wavetable;

import be.tarsos.dsp.io.TarsosDSPAudioFormat;

import javax.sound.sampled.AudioFormat;
import java.util.Arrays;
import java.util.List;

public class WavesynConstants {

    public static final AudioFormat MONO_WAV =
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    44100, 16, 1, 2, 44100, false);

    public static final TarsosDSPAudioFormat TARSOS_MONO_WAV =
            new TarsosDSPAudioFormat(TarsosDSPAudioFormat.Encoding.PCM_SIGNED,
                    44100, 16, 1, 2, 44100, false);

    //accomodates the biggest waveform buffer of all tables scans I have seen by about 50%
    public static final int MAXSIZE = 10000000;
    public static final int BUFFERSIZE = 2560;
    public static final double SAMPLERATE = 44100;
    public static final int WAVESAMPLESIZE = 512;


    public static List<String> LFO_TYPE = Arrays.asList("SAW", "SINE", "TRIANGLE", "SQUARE");

    public static final int STOPINDEX_DEFAULT = 20;
    public static final int STARTINDEX_DEFAULT = 0;
    public static final int SCANRATE_DEFAULT = 50;
    public static final int TABLEINDEX_DEFAULT = 55;

    //for waveform alignement
    public static final int FUZZINESS = 3;
    public static final int MINMATCH = 12;

    public static final float MINMATCHPERCENT = 0.4f;

    //patch storage
    public static final String PATCHFILE = "wavesynpatches.json";

    //scan rate
    public static final int MAXSCAN = 96;

    //pitch range max in cents
    //TODO change if expanding lower octave range- can go down two easily
    public static final int MAXPITCH = 3000;

    //patch dialog
    public static final String PATCHDIALOGTITLE = "Save Patch";
    public static final String PATCHNAME= "Enter a patch name or use default value";

    //midi stuff
    public static final int VELOCITY = 100;

    //for flushing left over audio from kast note played
    public static final int AUDIOFLUSH = 7500;
}
