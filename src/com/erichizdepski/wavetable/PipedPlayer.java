package com.erichizdepski.wavetable;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.PipedInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.BooleanControl.Type;

import static com.erichizdepski.wavetable.WavesynConstants.AUDIOFLUSH;


/**
 * This class plays 16-bit, mono sample data using Java Sound.
 *
 * @author Erich Izdepski
 */
public class PipedPlayer extends Thread
{
    private final static Logger LOGGER = Logger.getLogger(PipedPlayer.class.getName());
    PipedInputStream input = null;
    boolean alive = false;
    //control data flushing behavior
    boolean mute = false;
    int buffersize = 0;
    boolean restart = false;


    public PipedPlayer(PipedInputStream input, int buffersize)
    {
        this.input = input;
        this.buffersize = buffersize;
    }


    public void setAlive(boolean status)
    {
        this.alive = status;
    }

    public void run()
    {
        alive = true;

        //now create a playback piece
        DataLine.Info info = null;
        SourceDataLine line = null;
        BooleanControl mute = null;

        try
        {
            info = new DataLine.Info(SourceDataLine.class, WavesynConstants.MONO_WAV);
            line = (SourceDataLine)AudioSystem.getLine(info);
            line.open(WavesynConstants.MONO_WAV, buffersize);
            mute = (BooleanControl)line.getControl(Type.MUTE);
            line.start();
            byte[] buffer = new byte[buffersize];

            //write data for a while, then quit
            int length = 0;

            while(alive)
            {
                //there is data from old note still in buffer, so it must be muted
                if (this.mute)
                {
                    /* TODO muting and skipping some bytes solves the problem of hearing the prior note. Still not smooth
                    since mute is on/off and abrupt. Would be best to try the gain control and a really fast fade out
                    and fade in. This should really smooth it out.
                     */
                    //mute the line
                    mute.setValue(true);
                    restart = true;
                }
                else
                {
                    if (restart) {
                        //skip some bytes to get rid of the data from the last note played
                        input.skip(AUDIOFLUSH);
                        restart = false;
                        mute.setValue(false);
                    }

                    length = input.read(buffer);
                    if (length > 0)
                    {
                        //write to the audio line (most likely BUFFER_SIZE bytes)- to start playback
                        line.write(buffer, 0, length);
                    }
                }

                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            LOGGER.log(Level.INFO,"player thread no longer alive");

        }
        catch (LineUnavailableException |IOException e)
        {
            LOGGER.log(Level.ALL, "IOException", e);
        }
        finally
        {
            line.stop();
            try
            {
                input.close();
            }
            catch (IOException e)
            {
                LOGGER.log(Level.ALL, "IOException in Finally", e);
            }
        }
    }

    public void setMute(boolean status)
    {
        mute = status;
    }
}