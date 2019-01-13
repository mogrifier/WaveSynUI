package com.erichizdepski.util;

import com.erichizdepski.wavetable.WaveSynthesizer;
import javafx.beans.binding.IntegerBinding;
import org.json.simple.JSONObject;

import static com.erichizdepski.wavetable.WavesynConstants.*;

public class WavePatch {

    int start;
    int stop;
    int rate;
    WaveSynthesizer.LfoType lfo;
    int wavetableIndex;
    String name;

    public WavePatch(int startIndex, int stopIndex, int scanRate, int tableIndex, WaveSynthesizer.LfoType type, String patchName)
    {
        this.start = startIndex;
        this.stop = stopIndex;
        this.rate = scanRate;
        this.wavetableIndex = tableIndex;
        this.lfo = type;
        this.name = patchName;
    }


    public static WavePatch getDefaultPatch()
    {
      return new WavePatch(STARTINDEX_DEFAULT, STOPINDEX_DEFAULT, SCANRATE_DEFAULT, TABLEINDEX_DEFAULT,
              WaveSynthesizer.LfoType.TRIANGLE, "default");
    }


    public int getStartIndex() {
        return start;
    }

    public int getStopIndex() {
        return stop;
    }

    public int getScanRate() {
        return rate;
    }

    public int getWaveTableIndex() {
        return wavetableIndex;
    }

    public String getLfoType() {
        return lfo.toString();
    }

    public String getName() {
        return name;
    }


    public static WavePatch getPatch(JSONObject json)
    {
        WavePatch patch = null;
        //create a patch from the JSON object

        patch = new WavePatch(((Long)json.get("start")).intValue(), ((Long)json.get("stop")).intValue(),
                ((Long)json.get("scanRate")).intValue(),
                ((Long)json.get("tableIndex")).intValue(),
                WaveSynthesizer.LfoType.valueOf((String)json.get("lfotype")),
                (String)json.get("name"));

        return patch;
    }
}
