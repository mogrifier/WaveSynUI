package com.erichizdepski.util;

import be.tarsos.dsp.io.TarsosDSPAudioFloatConverter;
import com.erichizdepski.wavetable.WavesynConstants;

public class AudioFader {

    public static byte[] fadeOut(byte[] data) {
        TarsosDSPAudioFloatConverter converter = TarsosDSPAudioFloatConverter.getConverter(WavesynConstants.TARSOS_MONO_WAV);

        float[] audioFloatBuffer = new float[data.length / 2];
        converter.toFloatArray(data, audioFloatBuffer);
        double gain = 1d;
        for (int i = 0; i < audioFloatBuffer.length; i++) {

            gain = 1d - ((double)i / (double)audioFloatBuffer.length);

            float newValue = (float) (audioFloatBuffer[i] * gain);
            if (newValue > 1.0f) {
                newValue = 1.0f;
            } else if (newValue < -1.0f) {
                newValue = -1.0f;
            }
            audioFloatBuffer[i] = newValue;
        }

        byte[] fadedAudio = new byte[data.length];
        return converter.toByteArray(audioFloatBuffer, fadedAudio);

    }

    public static byte[] fadeIn(byte[] data) {
        TarsosDSPAudioFloatConverter converter = TarsosDSPAudioFloatConverter.getConverter(WavesynConstants.TARSOS_MONO_WAV);

        float[] audioFloatBuffer = new float[data.length / 2];
        converter.toFloatArray(data, audioFloatBuffer);
        double gain = 1d;
        for (int i = 0; i < audioFloatBuffer.length; i++) {

            gain = ((double)i / (double)audioFloatBuffer.length);

            float newValue = (float) (audioFloatBuffer[i] * gain);
            if (newValue > 1.0f) {
                newValue = 1.0f;
            } else if (newValue < -1.0f) {
                newValue = -1.0f;
            }
            audioFloatBuffer[i] = newValue;
        }

        byte[] fadedAudio = new byte[data.length];
        return converter.toByteArray(audioFloatBuffer, fadedAudio);

    }
}
