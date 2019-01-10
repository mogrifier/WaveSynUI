package com.erichizdepski.util;

import be.tarsos.dsp.io.TarsosDSPAudioFloatConverter;
import com.erichizdepski.wavetable.WavesynConstants;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AudioHelpers {

    private final static Logger LOGGER = Logger.getLogger(AudioHelpers.class.getName());

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    public static int getInt(byte msb, byte lsb)
    {
        int amplitude = (int)lsb +  msb * 256;
        //System.out.println("lsb " + (int)lsb + " msb " + (int)msb + "int value " + amplitude);

        return Math.abs(amplitude);
    }


    public static short getShort(byte msb, byte lsb)
    {
        //System.out.println("lsb " + (int)lsb + " msb " + (int)msb + "int value " + amplitude);

        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(lsb);
        bb.put(msb);

        return bb.getShort(0);
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

        //skipping first buffer do to garbage (on mac) when pitchshift <= 700 cents.
        //no need for gap removal since gap part is cut out

        //found if only shifting 100 cents that gap reappears in location > buffersize. grr. Added 1000 to compensate.


        return Arrays.copyOfRange(data, WavesynConstants.BUFFERSIZE + 1000, trailIndex);
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

        //need to match end of fadedregion with beginning of original buffer- must be smoooth. Just go through and compare
        //not sure if slope needs to match - maybe just a value match?? easier than checking slope but sloppy and
        //may not be good for complex waveforms that could have same value at multiple times, though in different part of
        // wave cycle. Hmm. Could match distance between zero
        //crossing points to get an idea of if the waveforms aligned to correct phase.

        buffer.put(original, overlap, (original.length) - (2 * overlap));

        //starting at original[overlap] loop through and find first match with last 2 values of fadedRegion



        return buffer.array();
    }



    public static byte[] smoothBySlope(byte[] data)
    {
        //get last value. start form beginning and find matching value. Then skip over those starting bytes
        saveFile(data, "presmoothed.wav");


        //try brute force pattern matching 512 byte pieces

        //skipping last ten 10. create 40 byte patternb for match attempt
        int tailStart = data.length - 50;
        int tailEnd = data.length - 10;

        int[] tail = getAudioAmplitude(Arrays.copyOfRange(data, tailStart, tailEnd));
        saveFile(Arrays.copyOfRange(data, tailStart, tailEnd), "tail.wav");
        float tailSlope = getSlope(tail);
        //use last value
        int transitionValue = tail[tail.length - 1];

        LOGGER.log(Level.INFO, "transition = " + transitionValue);
        LOGGER.log(Level.INFO, "tail slope = " + tailSlope);

        boolean found = false;

        int skip = 10;
        int index = 0;
        int compareAt = 0;

        float[] slope = new float[WavesynConstants.WAVESAMPLESIZE];
        float delta = 0;
        //just make big to get process going for finding minimum
        float min = 100000;

        int[] amplitudeBuffer = null;

        amplitudeBuffer = getAudioAmplitude(Arrays.copyOfRange(data, 0, WavesynConstants.WAVESAMPLESIZE));

        //index has to be an even value for this to work.
        for (int i = 0; i < WavesynConstants.WAVESAMPLESIZE; i+=2)
        {
            slope[i] = getSlope(getAudioAmplitude(Arrays.copyOfRange(data, i, i + tail.length)));
            //best means same sign and closest

            if (tailSlope < 0 && slope[i] < 0)
            {
                //same sign and close value means same phase

                LOGGER.log(Level.INFO, "delta " + (amplitudeBuffer[compareAt] - transitionValue));


                if (Math.abs(amplitudeBuffer[compareAt] - transitionValue) < 500)
                {
                    //really good match
                    //compare
                    delta = Math.abs(slope[i] - tailSlope);
                }
            }
            else if (tailSlope > 0 && slope[i] > 0)
            {

                LOGGER.log(Level.INFO, "delta " + (amplitudeBuffer[compareAt] - transitionValue));
                if (Math.abs(amplitudeBuffer[compareAt] - transitionValue) < 500) {
                    //compare
                    delta = Math.abs(slope[i] - tailSlope);
                }
            }


            if (delta < min)
            {
                min = delta;
                //index where best matching slope was found
                index = i;
            }

            compareAt++;
        }

        //this can match slope but still have a discontinuity since it does not match the phase.


        LOGGER.log(Level.INFO, "best slope delta =" + min + " index of " + index);

        saveFile(Arrays.copyOfRange(data, index, (data.length - index) - skip), "bestslope.wav");

        return Arrays.copyOfRange(data, index, (data.length - index) - skip);

        /*


        */

    }


    /**
     * By converting the byte array of samples to an integer array of amplitude values we can the shape of the sound
     * and can use that for matching/smoothing loop end points.
     * @param data
     * @return
     */
    public static byte[] smoothByMatch(byte[] data)
    {
        //some garbage at end- so ignore last 10 bytes or so
        int skip = 10;

        int[] tailAmplitude = getAudioAmplitude(Arrays.copyOfRange(data, (data.length - 256) - (skip), data.length - (skip)));

        saveFile(Arrays.copyOfRange(data, (data.length - 256) - (skip), data.length - (skip)), "tail.wav");

        //use last value
        int transitionValue = tailAmplitude[tailAmplitude.length - 1];

        boolean found = false;
        int index = 0;

        //get audio amplitude array for matching against
        int[] buffer = getAudioAmplitude(Arrays.copyOfRange(data, 0, WavesynConstants.WAVESAMPLESIZE));


        //can derive waveform- max, min, zero crossing from this buffer and use to compare

        while (!found)
        {
            //LOGGER.log(Level.INFO, "transition value=" + transitionValue + "  versus " + getInt(data[index], data[index + 1]));


            //TODO match the phase- not just the value. could be 180 out or whatever if complex wave

            if (Math.abs(buffer[index] - transitionValue) < 300)
            {

                found = true;
                LOGGER.log(Level.INFO, "found index=" + index);
                LOGGER.log(Level.INFO, "transition value=" + transitionValue + "  versus " + buffer[index]);
                //break out so index is right value.
                break;
            }

            index ++;
            //no need to check more than 512 bytes
            if (index == WavesynConstants.WAVESAMPLESIZE/2)
            {
                break;
            }
        }

        //remove bad spot and return properly aligned buffer
        if (found)
        {
            //index is number of bytes to skip
            saveFile(Arrays.copyOfRange(data, index * 2, (data.length - index * 2) - skip), "smoothed.wav");
            return Arrays.copyOfRange(data, index * 2, (data.length - index * 2) - skip);
        }

        //if not found, just return original unchanged
        LOGGER.log(Level.INFO, "did not find match point for smoothing");
        return data;
    }


    public static byte[] smoothByAligning(byte[] data)
    {
        //just want to match the end and the front of the audio samples, no more than 512 bytes
        byte[] head = Arrays.copyOfRange(data, 0, 4 * WavesynConstants.WAVESAMPLESIZE);
        byte[] tail = Arrays.copyOfRange(data, data.length - (4 * WavesynConstants.WAVESAMPLESIZE), data.length);


        System.out.println(getCycleInfo(head));
        System.out.println(getCycleInfo(tail));

        List<Extrema> headExtrema = getExtema(head);
        List<Extrema> tailExtrema = getExtema(tail);

        int[] headRelative = Extrema.getExtremaRelativePosition(headExtrema);
        int[] tailRelative = Extrema.getExtremaRelativePosition(tailExtrema);

        System.out.println(Arrays.toString(headRelative));
        System.out.println(Arrays.toString(tailRelative));


        System.out.println(Arrays.toString(AudioHelpers.getAudioAmplitude(head)));
        System.out.println(Arrays.toString(getAudioAmplitude((tail))));


        LOGGER.log(Level.INFO, "******* head *********");
        for (int i = 0; i < headExtrema.size(); i++)
        {
            //LOGGER.log(Level.INFO, headExtrema.get(i).toString());
        }

        LOGGER.log(Level.INFO, "******* tail *********");
        for (int i = 0; i < tailExtrema.size(); i++)
        {
            //LOGGER.log(Level.INFO, tailExtrema.get(i).toString());
        }
        //need to be fuzzy when comparing. I think fine to just look at amplitude of extrema and type call it +/- 10?? for fuzz
        //now use the extrema arrays to align the waveforms into a match. Can use the relative difference of index values
        // to create a map for matching

        int matchCount = 0;
        int headMatchStart = 0;
        int tailMatchStart = 0;
        boolean firstMatch = true;
        boolean found = false;

        //use tail as reference. compare head to it to find where in head it matches to tail
        for (int i = 0; i < tailExtrema.size(); i++)
        {
            //compare each head extrema to tail. watch out for array out of bounds exception
            matchCount = 0;
            headMatchStart = 0;
            tailMatchStart = i;
            firstMatch = true;
            found = false;

            for (int j = 0; j < headExtrema.size(); j++)
            {
                if (i + j >= tailExtrema.size())
                {
                    continue;
                }
                //compare. need consecutive matches
                boolean match = Extrema.matchExtrema(tailExtrema.get(i + j), headExtrema.get(j));

                if (match)
                {
                    LOGGER.log(Level.INFO, "MATCH tail at " + (i) + " with head at " + j);

                    matchCount ++;
                    if (firstMatch)
                    {
                        headMatchStart = j;
                        firstMatch = false;
                    }

                    //if many points in a row match, call it found
                    if (matchCount > 8 || matchCount > 0.4 * headExtrema.size())
                    {
                        //enough?
                        found = true;
                        break;
                    }

                }
                else
                {
                    //LOGGER.log(Level.INFO, "streak broke");
                    //reset
                    matchCount = 0;
                    firstMatch = true;
                }

            }

            if (found)
            {
                break;
            }
        }

        if (found)
        {
            /*
            This means the tail aligns to head from headMatchStart. But to align the tail and head smoothly means we need
            to cut off part of the tail and have its neew ending match a starting point in the head. The cut point is
            based on counting a number of extrema, and counting the same number forward in the head.
             */

            int ouroborous = (headExtrema.size() - headMatchStart) / 2;

            //compute new start and end points after cutting
            int start =  headExtrema.get(ouroborous).index * 2;

            //byte delta from tail match start
            int delta = start - headExtrema.get(headMatchStart).index * 2;

            int end =  (data.length - WavesynConstants.WAVESAMPLESIZE) + tailExtrema.get(tailMatchStart).index * 2 + delta;

            //should just make new array from second half (from alignment start) of head plus main part plus first half of head

            //create new head = alignment start to end of head
            //new start = half way through new head
            //create new tail = first half of new head. put at tail match start


            //use data on the match to compute new buffer. it is a sample start so multiply by 2.
            LOGGER.log(Level.INFO, "found extrema pattern match; new audio range is " + start + "  to " + end);
            saveFile(Arrays.copyOfRange(data, start, data.length - start), "extremasmpoothed.wav");

            return Arrays.copyOfRange(data, start, end);
        }

        return data;

    }



    public static byte[] smoothByCycle(byte[] data)
    {
        //just want to match the end and the front of the audio samples, no more than 512 bytes
        byte[] head = Arrays.copyOfRange(data, 0, 2 * WavesynConstants.WAVESAMPLESIZE);
        byte[] tail = Arrays.copyOfRange(data, data.length - (2 * WavesynConstants.WAVESAMPLESIZE), data.length);

        List<Cycle> headCycle = getCycleInfo(head);
        List<Cycle> tailCycle = getCycleInfo(tail);

        System.out.println(headCycle);
        System.out.println(tailCycle);

        //List<Extrema> headExtrema = getExtema(head);
        //List<Extrema> tailExtrema = getExtema(tail);

        //int[] headRelative = Extrema.getExtremaRelativePosition(headExtrema);
        //int[] tailRelative = Extrema.getExtremaRelativePosition(tailExtrema);

        //System.out.println(Arrays.toString(headRelative));
        //System.out.println(Arrays.toString(tailRelative));


        //no reason to print but for debug
        System.out.println(Arrays.toString(getAudioAmplitude(head)));
        System.out.println(Arrays.toString(getAudioAmplitude((tail))));


        /*
        LOGGER.log(Level.INFO, "******* head *********");
        for (int i = 0; i < headExtrema.size(); i++)
        {
            //LOGGER.log(Level.INFO, headExtrema.get(i).toString());
        }

        LOGGER.log(Level.INFO, "******* tail *********");
        for (int i = 0; i < tailExtrema.size(); i++)
        {
            //LOGGER.log(Level.INFO, tailExtrema.get(i).toString());
        }
        */
        //need to be fuzzy when comparing. I think fine to just look at amplitude of extrema and type call it +/- 10?? for fuzz
        //now use the extrema arrays to align the waveforms into a match. Can use the relative difference of index values
        // to create a map for matching

        int matchCount = 0;
        int headMatchStart = 0;
        int tailMatchStart = 0;
        boolean firstMatch = true;
        boolean found = false;

        //use tail as reference. compare head to it to find where in head it matches to tail
        for (int i = 0; i < tailCycle.size(); i++)
        {
            //compare each head extrema to tail. watch out for array out of bounds exception
            matchCount = 0;
            headMatchStart = 0;
            tailMatchStart = i;
            firstMatch = true;
            found = false;

            for (int j = 0; j < headCycle.size(); j++)
            {
                if (i + j >= tailCycle.size())
                {
                    continue;
                }
                //compare. need consecutive matches
                boolean match = Cycle.matchCycle(tailCycle.get(i + j), headCycle.get(j));

                if (match)
                {
                    LOGGER.log(Level.INFO, "MATCH tail at " + (i) + " with head at " + j);

                    matchCount ++;
                    if (firstMatch)
                    {
                        headMatchStart = j;
                        firstMatch = false;
                    }

                    //if many points in a row match, call it found
                    if (matchCount > 8 || matchCount > 0.4 * headCycle.size())
                    {
                        //enough?
                        found = true;
                        break;
                    }

                }
                else
                {
                    //LOGGER.log(Level.INFO, "streak broke");
                    //reset
                    matchCount = 0;
                    firstMatch = true;
                }

            }

            if (found)
            {
                break;
            }
        }

        if (found)
        {
            /*
            This means the tail aligns to head from headMatchStart. But to align the tail and head smoothly means we need
            to cut off part of the tail and have its neew ending match a starting point in the head. The cut point is
            based on counting a number of extrema, and counting the same number forward in the head.
             */

            int ouroborous = (headCycle.size() - headMatchStart) / 2;

            //compute new start and end points after cutting

            //sum the cycle count values (up to current) to get the needed index


            //need three values start to ouro to end
            int startIndex = 0;
            int midIndex = 0;

            for (int i = 0; i < headMatchStart; i++)
            {
                startIndex += headCycle.get(i).count;
            }

            for (int i = ouroborous; i < headCycle.size(); i++)
            {
                midIndex += headCycle.get(i).count;
            }

            //need to save out segments I think are matching

            //start to mid index (in bytes) of the head becomes the tail. mid to end of head is new head.

            //should just make new array from second half (from alignment start) of head plus main part plus first half of head

            //create new head = alignment start to end of head
            //new start = half way through new head
            //create new tail = first half of new head. put at tail match start

            ByteBuffer buffer = ByteBuffer.allocate(data.length - (tail.length + (startIndex * 2)));

            //put semantics are not "from- to" but "from, length"
            buffer.put(head, midIndex * 2, head.length - (midIndex * 2));
            buffer.put(data, head.length,  data.length - (head.length + tail.length));
            buffer.put(head, startIndex * 2, (midIndex - startIndex) * 2);

            //use data on the match to compute new buffer. it is a sample start so multiply by 2.
            LOGGER.log(Level.INFO, "found cycle pattern match");
            saveFile(buffer.array(), "cyclesmoothed.wav");

            return buffer.array();
        }

        return data;

    }


    public static byte[] smoothByExtrema(byte[] data)
    {
        //just want to match the end and the front of the audio samples, no more than 512 bytes
        byte[] head = Arrays.copyOfRange(data, 0, WavesynConstants.WAVESAMPLESIZE);
        byte[] tail = Arrays.copyOfRange(data, data.length - WavesynConstants.WAVESAMPLESIZE, data.length);

        List<Extrema> headExtrema = getExtema(head);
        List<Extrema> tailExtrema = getExtema(tail);

        //compare lengths
        LOGGER.log(Level.INFO, "head vs tail length " + headExtrema.size() + "  " + tailExtrema.size());

        //need to be fuzzy when comparing. I think fine to just look at amplitude of extrema and type call it +/- 10?? for fuzz
        //now use the extrema arrays to align the waveforms into a match. Can use the relative difference of index values
        // to create a map for matching

        int matchCount = 0;
        int matchStart = 0;
        boolean firstMatch = true;
        boolean found = false;

        //use tail as reference. compare head to it to find where in head it matches to tail
        for (int i = 0; i < tailExtrema.size(); i++)
        {
            //compare each head extrema to tail. watch out for array out of bounds exception
            matchCount = 0;
            matchStart = 0;
            firstMatch = true;
            found = false;

            int lookAhead = i;

            for (int j = 0; j < headExtrema.size(); j++)
            {
                //compare
                boolean match = Extrema.matchExtrema(tailExtrema.get(lookAhead++), headExtrema.get(j));
                //if you find a match, keep looking,
                if (lookAhead >= tailExtrema.size())
                {
                    break;
                }
                if (match)
                {
                    matchCount ++;
                    if (firstMatch)
                    {
                        matchStart = j;
                        firstMatch = false;
                    }
                }

                //if many points in a row match, call it found
                if (matchCount > 5 ) //|| matchCount > 0.4 * headExtrema.size())
                {
                    //enough?
                    found = true;
                    break;
                }
            }

            if (found)
            {
                break;
            }
        }

        if (found)
        {

            //use data on the match to compute new buffer. it is a smaple start so multiply by 2.
            int start = headExtrema.get(matchStart).index * 2;
            LOGGER.log(Level.INFO, "found extrema pattern match; starting audio at " + start);
            saveFile(Arrays.copyOfRange(data, start, data.length - start), "extremasmpoothed.wav");

            return Arrays.copyOfRange(data, start, data.length - start);
        }

        return data;
    }


    public static int[] getAudioAmplitude(byte[] data)
    {
        int samples = data.length / 2;
        int[] amplitude= new int[samples];

        for (int i = 0; i < samples; i++) {
            int lsb = data[2 * i];
            int msb = data[2 * i + 1];
            amplitude[i] = msb << 8 | (255 & lsb);
        }

        return amplitude;
    }




    public static float getSlope(int[] data)
    {
        //very simplistic. assume straight line, roughly, due to really short array rise/run = slope

        float rise = data[data.length - 1] - data[0];
        float run = data.length/2;

        return rise/run;
    }



    public static List<Extrema> getExtema(byte[] data)
    {
        //use amplitude values
        int[] amplitude = getAudioAmplitude(data);

        List<Extrema> extrema = new ArrayList<Extrema>(amplitude.length/5);
        //go through byte array, skipping first and last elements
        for (int i = 1; i < amplitude.length - 2; i++)
        {
            Extrema.DotType type = Extrema.compare(amplitude[i - 1], amplitude[i], amplitude[i + 1]);

            if (type == Extrema.DotType.MIN)
            {
                //found a minimum
                extrema.add(new Extrema(i, amplitude[i], Extrema.DotType.MIN));
            }
            else if (type == Extrema.DotType.MAX)
            {
                //found a maxima
                extrema.add(new Extrema(i, amplitude[i], Extrema.DotType.MAX));
            }
        }

        return extrema;
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

    public static byte[] removeZeroGap(byte[] data) {

        //there is a gap (string of zeroes) near end of first first. find and remove.
        //I looked at some algorithms- nothing special here. Brute force it.

        boolean found = false;
        int index = 4000;
        int zeroRun = 0;
        int zeroStart = index;
        ByteBuffer buffer = null;

        //search from index to the end of the byte array
        while (index < data.length)
        {
            //if z zero is found, see if the start of a string of zeroes
            if (data[index++] == 0)
            {
                //reset parameters for next search
                zeroRun = 0;
                //track from index of the first zero
                zeroStart = index - 1;
                //found a zero, now look ahead 10 bytes
                while (data[index++] == 0)
                {
                    //count how many zeroes found
                    zeroRun++;
                }

                if (zeroRun > 20)
                {
                    //found it- done searching
                    found = true;
                    break;
                }
            }
        }

        if (found)
        {
            //found string of at least 20. This should be what is to be removed.
            buffer = ByteBuffer.allocate(data.length - zeroRun);
            buffer.put(data, 0, zeroStart);
            //skip the zeroRun
            buffer.put(data, zeroStart + zeroRun, (data.length - (zeroStart + zeroRun)));
            return buffer.array();
        }

        //if not found, just return original unchanged
        LOGGER.log(Level.INFO, "did not find zero run");
        return data;
    }


    /**
     * Count the number of consecutive samples above or below zero and track the streak count.
     * @param data
     * @return
     */
    public static int[] getCycleCount(byte[] data)
    {
        int[] amplitude = AudioHelpers.getAudioAmplitude(data);

        //count positive and negative values in a row and store
        //this is a way to simplify a curve

        //use autoboxing to convert between int and Integer
        List<Integer> cycle = new ArrayList<>();

        int above = 0;
        int below = 0;
        boolean positive = false;

        //set initial value
        if (amplitude[0] >= 0)
        {
            positive = true;
        }

        for (int i = 0; i < amplitude.length; i++)
        {
            if (amplitude[i] >= 0)
            {
                if (!positive)
                {
                    //just changed from negative to positive so save negative and reset
                    cycle.add(below);
                    below = 0;
                }

                above++;
                positive = true;
            }
            else
            {
                if (positive)
                {
                    //just changed from negative to positive so save negative and reset
                    cycle.add(above);
                    above = 0;
                }
                below++;
                positive = false;
            }
        }


        //fancy
        return cycle.stream().mapToInt(i->i).toArray();
    }



    public static List<Cycle> getCycleInfo(byte[] data)
    {
        int[] amplitude = AudioHelpers.getAudioAmplitude(data);

        //count positive and negative values in a row and store
        //this is a way to simplify a curve

        //use autoboxing to convert between int and Integer
        List<Cycle> cycle = new ArrayList<>();

        int above = 0;
        int below = 0;
        boolean positive = false;

        //set initial value
        if (amplitude[0] >= 0)
        {
            positive = true;
        }

        for (int i = 0; i < amplitude.length; i++)
        {
            if (amplitude[i] >= 0)
            {
                if (!positive)
                {
                    //just changed from negative to positive so save negative and reset
                    cycle.add(new Cycle(below, Cycle.Type.NEGATIVE));
                    below = 0;
                }

                above++;
                positive = true;
            }
            else
            {
                if (positive)
                {
                    //just changed from negative to positive so save negative and reset
                    cycle.add(new Cycle(above, Cycle.Type.POSITIVE));
                    above = 0;
                }
                below++;
                positive = false;
            }
        }


        return cycle;
    }
}
