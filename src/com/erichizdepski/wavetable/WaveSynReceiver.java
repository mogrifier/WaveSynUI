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
    private int oldNote = 0;

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


        //TODO skip out of range notes for now
        if (note < 17 || note > 71)
        {
            //think about note on off signal
            LOGGER.log(Level.INFO, "midi note out of range");
            return;
        }

        //this is polling and sends a note on signal all the time when using typing keyboard. The key press event
        //get fired over and over.

        if (status == ShortMessage.NOTE_ON)
        {
            //these logic should cause new note priority and allow for smoother/faster playing on a keyboard
            if (alreadyOn && note == oldNote)
            {
                //ignore
                LOGGER.log(Level.INFO, "alreadyOn");
                return;
            }

            alreadyOn = true;
            //set pitch to given note
            oldNote = note;
            //moog range is 48 to 84 (c to c). wavesyn is currently 30 notes vice 37. d to g.
            int pitchCents = (note - 41) * 100;

            //FIXME
            synth.setPitch(pitchCents);
            synth.turnOn();
            LOGGER.log(Level.INFO, "midi note =  " + note + " NOTE ON " + pitchCents);
        }
        else if (status == ShortMessage.NOTE_OFF && oldNote == note)
        {
            //note off
            LOGGER.log(Level.INFO, "NOTE OFF");
            alreadyOn = false;
            synth.turnOff();
        }
    }

    @Override
    public void close() {

    }
}
