package com.erichizdepski.wavetable;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
Such bad class names in Java MIDI. Here you request a receiver from a synth and send MIDI commands to the synth using
the receiver. Sounds a little like a transmitter, but that is another class!
 */
public class WaveSynReceiver implements Receiver {

    private final static Logger LOGGER = Logger.getLogger(WaveSynReceiver.class.getName());
    private WaveSynthesizer synth;
    private boolean alreadyOn = false;

    public WaveSynReceiver(WaveSynthesizer synth)
    {
        this.synth = synth;
    }



    @Override
    public void send(MidiMessage message, long timeStamp) {

        //send the note to the synth. set the pitch.

        //the midi message has a midi pitch in it. Convert to wavesyn cents above 0 note (D4).
        byte[] midi = message.getMessage();
        int status = message.getStatus();
        int note = (int)(midi[1] & 0xFF);

        if (status == ShortMessage.NOTE_ON)
        {
            if (alreadyOn)
            {
                //ignore
                return;
            }

            alreadyOn = true;
            //set pitch to given note
            int pitchCents = (note - 62) * 100;
            synth.setPitch(pitchCents);
            synth.turnOn();
            //LOGGER.log(Level.INFO, "NOTE ON " + pitchCents);
        }
        else if (status == ShortMessage.NOTE_OFF)
        {
            //note off
            //LOGGER.log(Level.INFO, "NOTE OFF");
            alreadyOn = false;
            synth.turnOff();
        }
    }

    @Override
    public void close() {

    }
}
