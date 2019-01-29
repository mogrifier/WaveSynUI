package com.erichizdepski.util;

import javax.sound.midi.*;

public class MidiTest{

    MidiDevice usbInput;

    public static void main(String[] args) {
        try{
            /* Create a new Sythesizer and open it. Most of
             * the methods you will want to use to expand on this
             * example can be found in the Java documentation here:
             * https://docs.oracle.com/javase/7/docs/api/javax/sound/midi/Synthesizer.html
             */
            MidiDevice.Info info[] = MidiSystem.getMidiDeviceInfo();

            Synthesizer midiSynth = MidiSystem.getSynthesizer();
            midiSynth.open();

            //get and load default instrument and channel lists
            Instrument[] instr = midiSynth.getDefaultSoundbank().getInstruments();
            MidiChannel[] mChannels = midiSynth.getChannels();

            midiSynth.loadInstrument(instr[0]);//load an instrument


            mChannels[0].noteOn(60, 100);//On channel 0, play note number 60 with velocity 100
            try { Thread.sleep(2000); // wait time in milliseconds to control duration
            } catch( InterruptedException e ) { }
            mChannels[0].noteOff(60);//turn of the note

            //now play from midi keyboard
            //
            //
            MidiTest test = new MidiTest();
            test.init(info);

        } catch (MidiUnavailableException e) {}
    }

    public void init(MidiDevice.Info[] info)
    {
        try {
            usbInput = MidiSystem.getMidiDevice(info[13]);
            System.out.println("name: " + info[13].getName() + " description: " + info[13].getDescription() +
                    " vendor: " + info[13].getVendor() +
                    " version: " + info[13].getVersion());

            MidiInput myMidiIn = new MidiInput(usbInput);

            Thread thread = new Thread(myMidiIn);
            thread.start();

        }
        catch (MidiUnavailableException e)
        {
            e.printStackTrace();
        }
    }


    public class MidiInput implements Runnable{

        MidiDevice usbIn;

        public MidiInput(MidiDevice device)
        {
            this.usbIn = device;
            try {
                usbIn.open();
                System.out.println("max transmitters " + usbIn.getMaxTransmitters());

                Synthesizer midiSynth = MidiSystem.getSynthesizer();
                midiSynth.open();

                //get and load default instrument and channel lists
                Instrument[] instr = midiSynth.getDefaultSoundbank().getInstruments();
                MidiChannel[] mChannels = midiSynth.getChannels();

                midiSynth.loadInstrument(instr[0]);//load an instrument

                usbIn.getTransmitter().setReceiver(midiSynth.getReceiver());
            }
            catch (MidiUnavailableException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {

            //play music
            while(true)
            {

            }
        }
    }

}
