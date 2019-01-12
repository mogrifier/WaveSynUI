package com.erichizdepski.util;

import com.erichizdepski.wavetable.WavesynConstants;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Cycle {

    private final static Logger LOGGER = Logger.getLogger(Cycle.class.getName());
    //the samnple count
    int count = 0;
    //whether pos or neg cycle
    Type type;


    public enum Type
    {
        POSITIVE, NEGATIVE;

        @Override
        public String toString() {

            switch(this) {
                case POSITIVE: return "pos";
                case NEGATIVE: return "neg";
            }

            //will never happen but needed to compile
            return "";
        }
    }

    public Cycle(int count, Type type)
    {
        this.count = count;
        this.type = type;
    }

    public int getCount() {
        return count;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString()
    {
        return this.count + "" + this.type.toString();
    }


    public static boolean matchCycle(Cycle one, Cycle two)
    {
        if (one.type == two.type)
        {
            //now check amplitude

            //LOGGER.log(Level.INFO, "********************************************* comparing " + one.amplitude + " and " + two.amplitude);

            if (Math.abs(one.count - two.count) <= WavesynConstants.FUZZINESS)
            {
                //LOGGER.log(Level.INFO, " comparing " + one.amplitude + " and " + two.amplitude);
                //LOGGER.log(Level.INFO, "count and type match");
                //got a match
                return true;
            }
        }

        return false;
    }

}
