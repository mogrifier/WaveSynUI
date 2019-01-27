package com.erichizdepski.wavetable;

import javax.sound.midi.MidiChannel;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.erichizdepski.wavetable.WavesynConstants.VELOCITY;

public class KeyPlayer extends Thread implements KeyListener {

    private final static Logger LOGGER = Logger.getLogger(KeyPlayer.class.getName());
    private boolean alive = true;
    private MidiChannel channel;
    private int note = 0;

    private boolean on = false;

    public KeyPlayer(MidiChannel channel)
    {
        channel = channel;
    }

    public void run()
    {
        //listen for keyboard events. convert a key to a note and send command to play it.
        while(alive)
        {
            //just listen for keys

            if (on)
            {
                channel.noteOn(note, VELOCITY);
            }
            else
            {
                channel.noteOff(note);
            }

            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /** Handle the key typed event from the text field. */
    @Override
    public void keyTyped(KeyEvent e) {
        displayInfo(e, "KEY TYPED: ");

        //get note for key
        note = getNote(e);
        on = true;
    }

    /** Handle the key pressed event from the text field. */
    @Override
    public void keyPressed(KeyEvent e) {
        displayInfo(e, "KEY PRESSED: ");
    }

    private int getNote(KeyEvent e) {

        return e.getKeyCode();
    }

    /** Handle the key released event from the text field. */
    @Override
    public void keyReleased(KeyEvent e) {
        displayInfo(e, "KEY RELEASED: ");

        //turn off note.
        on = false;
    }


    private void displayInfo(KeyEvent e, String status)
    {
        int id = e.getID();
        String keyString;
        if (id == KeyEvent.KEY_TYPED) {
            char c = e.getKeyChar();
            keyString = "key character = '" + c + "'";
        } else {
            int keyCode = e.getKeyCode();
            keyString = "key code = " + keyCode
                    + " ("
                    + KeyEvent.getKeyText(keyCode)
                    + ")";
        }

        LOGGER.log(Level.INFO, keyString + " " + status);
    }
}
