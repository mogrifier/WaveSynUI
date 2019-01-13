package com.erichizdepski.util;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.erichizdepski.wavetable.WavesynConstants.PATCHFILE;


public class PatchList {

    private final static Logger LOGGER = Logger.getLogger(PatchList.class.getName());
    List<String> names;
    List<WavePatch> patches;

    public PatchList(int size)
    {
        //load it up
        patches = new ArrayList<WavePatch> (size);
        //read from a file
        JSONParser parser = new JSONParser();
        try {
            File userDir = new File(System.getProperty("user.home"));
            File patchFile = new File(userDir + File.separator + PATCHFILE);
            //check existence
            if (!patchFile.exists())
            {
                //no patch file so return default patch
                patches = new ArrayList<WavePatch>(1);
                patches.add(WavePatch.getDefaultPatch());
            }
            else {
                Object obj = parser.parse(new FileReader(patchFile));
                JSONArray patchList = (JSONArray) obj;
                //convert to a List of WavePatch objects
                for (int i = 0; i < patchList.size(); i++)
                {
                    JSONObject patch = (JSONObject) patchList.get(i);
                    patches.add(WavePatch.getPatch(patch));
                }

            }
        }
        catch (IOException | ParseException ex) {
            ex.printStackTrace();
        }

        //sort the list
        patches.sort(new PatchComparator());

    }

    public List<String> getPatchNames() {

        List<String> patchNames = new ArrayList<>(patches.size());

        //read all the names from the patch list
        for (int i = 0; i < patches.size(); i++)
        {
            patchNames.add(patches.get(i).getName());
        }

        //sort
        patchNames.sort(Comparator.comparing(String::toString));

        return patchNames;
    }

    public void savePatch(WavePatch waveSynPatch) {

        //save it to internal array
        patches.add(waveSynPatch);
        //sort the list
        patches.sort(new PatchComparator());
        //just save all each time, and blow away the json file.
        saveAll();
    }


    public WavePatch getPatch(int i)
    {
        return patches.get(i);
    }

    public void saveAll()
    {
        //write all patches to a json file
        WavePatch current;
        JSONArray list = new JSONArray();
        for (int i = 0; i < patches.size(); i++)
        {
            current = patches.get(i);
            //convert to json
            JSONObject obj = new JSONObject();

            obj.put("start", current.getStartIndex());
            obj.put("stop", current.getStopIndex());
            obj.put("scanRate", current.getScanRate());
            obj.put("tableIndex", current.getWaveTableIndex());
            obj.put("lfotype", current.getLfoType());
            obj.put("name", current.getName());

            list.add(obj);
        }

        FileWriter writer = null;
        try {
            File userDir = new File(System.getProperty("user.home"));
            File patchFile = new File(userDir + File.separator + PATCHFILE);
            //get a filewriter
            writer = new FileWriter(patchFile);
            writer.write(list.toJSONString());
            //LOGGER.log(Level.INFO, "wrote file " + patchFile.getCanonicalPath());
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public class PatchComparator implements Comparator<WavePatch> {
        @Override
        public int compare(WavePatch o1, WavePatch o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }
}
