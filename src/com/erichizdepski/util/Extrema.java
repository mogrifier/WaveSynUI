package com.erichizdepski.util;

import com.erichizdepski.wavetable.WavesynConstants;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Extrema {

    private final static Logger LOGGER = Logger.getLogger(Extrema.class.getName());
    //track characteristics of waveform
    //type. used to identify if a local max or min of the curve
    public enum DotType
    {
        MAX, MIN, NEITHER;

        @Override
        public String toString() {
            switch(this) {
                case MAX: return "max";
                case MIN: return "min";
            }

            return "neither";
        }
    }

    //the sample count
    int index;
    //the amplitude
    int amplitude;
    //whether a max or min
    DotType type;


    public Extrema (int index, int amplitude, DotType type)
    {
        this.index = index;
        this.amplitude = amplitude;
        this.type = type;
    }


    /**
     * This is correct mathematically but seems to find too many extrema because of very small and frequent sample
     * variations. May cause problem when trying to align samples.
     * @param prior
     * @param current
     * @param next
     * @return
     */
    public static DotType compare (int prior, int current, int next)
    {
        //mostly will be neither so test that first
        if (current > prior && current < next)
        {
            return DotType.NEITHER;
        }

        if (current < prior && current > next)
        {
            return DotType.NEITHER;
        }

        if (current > prior && current > next)
        {
            return DotType.MAX;
        }

        if (current < prior && current < next)
        {
            return DotType.MIN;
        }

        //any other possibilities??
        return DotType.NEITHER;
    }


    public String toString()
    {
        return "index " + index + " amplitude " + amplitude + " type " + type.toString();
    }



    public static boolean matchExtrema(Extrema one, Extrema two)
    {
        if (one.type == two.type)
        {
            //now check amplitude

            //LOGGER.log(Level.INFO, "********************************************* comparing " + one.amplitude + " and " + two.amplitude);

            if (Math.abs(one.amplitude - two.amplitude) < WavesynConstants.FUZZINESS)
            {
                //LOGGER.log(Level.INFO, " comparing " + one.amplitude + " and " + two.amplitude);
                //LOGGER.log(Level.INFO, "amplitude and type match");
                //got a match
                return true;
            }
        }

        return false;
    }


    public static int[] getExtremaRelativePosition (List<Extrema> extrema)
    {
        int[] delta = new int [extrema.size()];

        for (int i = 0; i < extrema.size() - 1; i++)
        {
            //compute the delta between each index pair. This gives a relative position of extrema
            delta[i] = extrema.get(i + 1).index - extrema.get(i).index;
        }

        return delta;
    }


}
