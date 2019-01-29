package com.erichizdepski.util;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;

public class MidiDeviceCheck {

    MidiDevice.Info info[] = null;
    MidiDevice deviceInput = null;
    MidiDevice deviceOutput = null;

    public MidiDeviceCheck()
    {
        info =  MidiSystem.getMidiDeviceInfo();
    }

    public static void main(String arg[])
    {
        //check midi
        MidiDeviceCheck midi = new MidiDeviceCheck();
        midi.enumerate();
        System.exit(0);
    }
    public void enumerate()
    {

            //just query the midi info objects and print what the contain
            for (int i = 0; i < info.length; i++) {
                System.out.println("name: " + info[i].getName() + " description: " + info[i].getDescription() +
                        " vendor: " + info[i].getVendor() +
                        " version: " + info[i].getVersion());
            }

            try {
                //HACK 5 = receiver. 13 = transmitter
                deviceInput = MidiSystem.getMidiDevice(info[6]);
                System.out.println("USING name: " + info[4].getName() + " description: " + info[4].getDescription() +
                        " vendor: " + info[4].getVendor() +
                        " version: " + info[4].getVersion());
                Receiver rcv = deviceInput.getReceiver();
                System.out.println("got a receiver!");

                /*
                ShortMessage myMsg = new ShortMessage();
                // Start playing the note Middle C (60),
                // moderately loud (velocity = 93).
                myMsg.setMessage(ShortMessage.NOTE_ON, 0, 60, 93);
                long timeStamp = -1;
                rcv.send(myMsg, timeStamp);

                myMsg.setMessage(ShortMessage.NOTE_ON, 1, 66, 93);

                rcv.send(myMsg, timeStamp);

                myMsg.setMessage(ShortMessage.NOTE_ON, 2, 64, 93);

                rcv.send(myMsg, timeStamp);
                */

                deviceOutput = MidiSystem.getMidiDevice(info[14]);
                System.out.println("max transmitters " + deviceOutput.getMaxTransmitters());


                //trn.setReceiver(rcv);
                Sequencer seq;
                Transmitter seqTrans;
                seq     = MidiSystem.getSequencer();

                if (seq == null) {
                    // Error -- sequencer device is not supported.
                    // Inform user and return...
                    System.out.println("sequencer is null!");
                    System.exit(1);

                } else {
                    // Acquire resources and make operational.
                    seq.open();
                }

                //try to send to moog
                deviceInput.open();


                seqTrans = seq.getTransmitter();
                //send data out port 2 to s90


                //try the to data to the moog through it's receiver interface

                seq.setTempoInBPM(100.0f);
                seq.setMicrosecondPosition(0l);

                File myMidiFile = new File("E://java_work//WaveSynUI//resources//testmidi.mid");
                // Construct a Sequence object, and
                // load it into my sequencer.
                Sequence mySeq = MidiSystem.getSequence(myMidiFile);

                seq.setSequence(mySeq);


                seq.setLoopCount(1);
                //seq.start();

                System.out.println("sequence length is " + mySeq.getMicrosecondLength()/1000000);

                if (seq.isRunning())
                {
                    System.out.println("sequencer is running!");
                }



                Synthesizer synth = MidiSystem.getSynthesizer();
                synth.open();
                synth.loadAllInstruments(synth.getDefaultSoundbank());
                MidiChannel[] channels = synth.getChannels();
                channels[0].programChange(1, 1);

                Receiver defRcv = synth.getReceiver();
                seqTrans.setReceiver(defRcv);

                //seqTrans.setReceiver(rcv);
                //seq.setSequence(mySeq);
                seq.setLoopCount(1);
                seq.start();

                boolean running = true;
                while(running)
                {
                    if (!seq.isRunning())
                    {
                        System.out.println("no longer running");
                        seq.stop();
                        running = false;
                    }
                }

                //close resources
                //seq.close();
                //defRcv.close();

                defRcv = synth.getReceiver();
                channels[0].programChange(1, 6);

                //now try to receive midi
                deviceOutput.open();
                Transmitter trn = deviceOutput.getTransmitter();
                System.out.println("got a transmitter!" + trn.toString());

                trn.setReceiver(defRcv);




            }
            catch (MidiUnavailableException | InvalidMidiDataException | IOException e)
            {
                e.printStackTrace();
            }

    }




}


