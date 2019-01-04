package com.erichizdepski.util;

import be.tarsos.dsp.io.TarsosDSPAudioFloatConverter;
import com.erichizdepski.wavetable.WavesynConstants;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class AudioHelpers {


    public static int getInt(byte msb, byte lsb)
    {
        int amplitude = (int)lsb + (((int)msb) << 8);
        //System.out.println("lsb " + (int)lsb + " msb " + (int)msb + "int value " + amplitude);

        return Math.abs(amplitude);
    }

    public static byte[] trim(byte[] data)
    {

        //saveFile(data, "before.wav");

        //remove leading and trailing zeros. Each pair of bytes is a floating point value. If both are zero, remove.
        boolean zero = true;
        int leadIndex = 0;
        int trailIndex = data.length - 1;

        if (trailIndex % 2 > 0)
        {
            //must make it even
            trailIndex--;
        }


        //remove leading bytes
        while (zero)
        {
            //first is lsb, next is msb
            if (getInt(data[leadIndex], data[leadIndex + 1])  == 0)
            {
                leadIndex += 2;
            }
            else
            {
                zero = false;
            }
        }

        //reset and trim the ending zeros
        zero = true;
        while (zero)
        {
            if ((getInt(data[trailIndex -1], data[trailIndex])) == 0)
            {
                trailIndex -= 2;
            }
            else
            {
                zero = false;
            }
        }

         return Arrays.copyOfRange(data, leadIndex, trailIndex);
    }



    public static void saveFile(byte[] data, String name)
    {
        //write data to a file
        try {
            FileOutputStream fos = new FileOutputStream(name);
            fos.write(data);
            fos.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    /**
     *
     * @param original length should be even or all messed up
     * @param overlap use an even number.
     * @return
     */
    public static byte[] crossfadeSample(byte[] original, int overlap)
    {
        TarsosDSPAudioFloatConverter converter = TarsosDSPAudioFloatConverter.getConverter(WavesynConstants.TARSOS_MONO_WAV);
        //call fade in and fade out. sum the arrays. avoid clipping
        byte[] crossfade = new byte[original.length - overlap];

        //get tail end of the original data and fade it out. length is overlap
        byte[] fadeOut =  fadeOut(Arrays.copyOfRange(original, original.length - overlap, original.length));
        if (original.length %2 != 0)
        {
            //odd length. adjust.
            fadeOut =  fadeOut(Arrays.copyOfRange(original, (original.length -1) - overlap, original.length - 1));
        }
        //length is overlap
        byte[] fadeIn = fadeIn(Arrays.copyOfRange(original, overlap , 2 * overlap));

        AudioHelpers.saveFile(original, "original.wav");
        AudioHelpers.saveFile(fadeIn, "fadein.wav");
        AudioHelpers.saveFile(fadeOut, "fadeout.wav");

        //create new byte array from the original plus fade in  and fade out
        float[] floatFadeOut = new float[overlap / 2];
        converter.toFloatArray(fadeOut, floatFadeOut);
        float[] floatFadeIn = new float[overlap / 2];
        converter.toFloatArray(fadeIn, floatFadeIn);
        float[] floatFadeSum = new float[overlap / 2];
        float sum = 0f;
        for (int i = 0; i < floatFadeIn.length; i++)
        {
            //perform with floats
            sum = floatFadeIn[i] + floatFadeOut[i];
            //avoid clipping
            if (sum > 1.0f) {
                sum = 1.0f;
                System.out.println("clipping");
            } else if (sum < -1.0f) {
                sum = -1.0f;
                System.out.println("clipping");
            }
            floatFadeSum[i] = sum;
        }

        byte[] fadedRegion = new byte[overlap];
        converter.toByteArray(floatFadeSum, fadedRegion);

        //now combine the fadedRegion byte array with the original array minus the leading and trailing overlaps
        ByteBuffer buffer = ByteBuffer.allocate(original.length - overlap);
        buffer.put(fadedRegion);
        AudioHelpers.saveFile(fadedRegion, "faded.wav");
        buffer.put(original, overlap, (original.length) - (2 * overlap));

        return buffer.array();
    }

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
                System.out.println("fadeout clipping");
            } else if (newValue < -1.0f) {
                newValue = -1.0f;
                System.out.println("fadeout clipping");
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
                System.out.println("fadein clipping");
            } else if (newValue < -1.0f) {
                newValue = -1.0f;
                System.out.println("fadein clipping");
            }
            audioFloatBuffer[i] = newValue;
        }

        byte[] fadedAudio = new byte[data.length];
        return converter.toByteArray(audioFloatBuffer, fadedAudio);

    }
}
