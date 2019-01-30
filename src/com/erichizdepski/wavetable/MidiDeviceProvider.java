/*
 * MidiDeviceProvider.java
 *
 */

package com.erichizdepski.wavetable;

import javax.sound.midi.MidiDevice;
import java.io.IOException;

/**
 * Used to provide Java Sound with the plug-in data needed for the WaveSyn Synthesizer.
 *
 * @author  Erich Izdepski
 */
public class MidiDeviceProvider extends javax.sound.midi.spi.MidiDeviceProvider
{
    MidiDevice.Info[] devices = new MidiDevice.Info[1];
    
    /** Creates a new instance of MidiDeviceProvider */
    public MidiDeviceProvider()
    {
        super();
        //points to single object with info about the Synthesizer
        //WaveSynInfo is a singleton
        devices[0] = WaveSynInfo.getInstance();
    }
    
    public MidiDevice getDevice(MidiDevice.Info info)
    {
        if (isDeviceSupported(info))
        {
            //this device is represented by a synthesizer
            try {
                return new WaveSynthesizer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
    
    
    public MidiDevice.Info[] getDeviceInfo()
    {
        //only one device to return in the array
        return devices;
    }
    
    
    public boolean isDeviceSupported(MidiDevice.Info info)
    {
        //only one device supported so check is trivial
        return (info.equals(WaveSynInfo.getInstance()));
    }
    
}
