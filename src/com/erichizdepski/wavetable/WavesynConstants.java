package com.erichizdepski.wavetable;

import javax.sound.sampled.AudioFormat;

public class WavesynConstants {

    public static final AudioFormat MONO_WAV =
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    44100, 16, 1, 2, 44100, false);


    public static final int BUFFERSIZE = 5120;

    public static final int WAVESAMPLESIZE = 512;
}
