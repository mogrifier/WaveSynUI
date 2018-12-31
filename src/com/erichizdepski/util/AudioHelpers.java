package com.erichizdepski.util;

import java.util.Arrays;

public class AudioHelpers {


    public static int getInt(byte lsb, byte msb)
    {
        return lsb + msb << 8;
    }

    public static byte[] trim(byte[] data)
    {
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
            if (getInt(data[leadIndex], data[leadIndex + 1]) < 10000)
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
            if (Math.abs(getInt(data[trailIndex -1], data[trailIndex])) < 100)
            {
                trailIndex -= 2;
            }
            else
            {
                zero = false;
            }
        }

        System.out.println("range is " + leadIndex + " to " + trailIndex + "; length is " + data.length);
        //trim the byte array from
        //copy the byte array
         return Arrays.copyOfRange(data, leadIndex, trailIndex);
    }
}
