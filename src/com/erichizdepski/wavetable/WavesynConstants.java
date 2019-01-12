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
    public static final int BUFFERSIZE = 5120;
    public static final double SAMPLERATE = 44100;
    public static final int WAVESAMPLESIZE = 512;


    public static List<String> LFO_TYPE = Arrays.asList("SAW", "SINE", "TRIANGLE");

    public static final int STOPINDEX_DEFAULT = 20;
    public static final int STARTINDEX_DEFAULT = 0;
    public static final int SCANRATE_DEFAULT = 20;

    //for waveform alignement
    public static final int FUZZINESS = 3;
    public static final int MINMATCH = 12;

    public static final float MINMAATCHPERCENT = 0.4f;
}
