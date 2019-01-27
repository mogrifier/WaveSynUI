package com.erichizdepski.wavetable;

import javax.sound.midi.MidiDevice;

public class WaveSynInfo extends MidiDevice.Info {

    private static WaveSynInfo instance = null;

    {
        //static initializer
        instance = new WaveSynInfo("wavesyn", "The Hope Machine",
                "PPG-like wavetable synthesizer", "1.0");
    }

    /**
     * Constructs a device info object.
     *
     * @param name        the name of the device
     * @param vendor      the name of the company who provides the device
     * @param description a description of the device
     * @param version     version information for the device
     */
    private WaveSynInfo(String name, String vendor, String description, String version) {
        super(name, vendor, description, version);
    }

    public static WaveSynInfo getInstance(){
        return instance;
    }


}
