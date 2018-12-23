package com.erichizdepski.wavetable;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.io.PipedInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;


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
    boolean discard = false;
    int buffersize = 0;


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

        try
        {
            info = new DataLine.Info(SourceDataLine.class, WavesynConstants.MONO_WAV);
            line = (SourceDataLine)AudioSystem.getLine(info);
            line.open(WavesynConstants.MONO_WAV, buffersize);
            line.start();
            byte[] buffer = new byte[buffersize];

            //write data for a while, then quit
            int length = 0;

            while(alive)
            {
                //this discards any data- but there is still some junk being played.
                if (discard)
                {
                    //line.stop();
                    line.flush();//??
                }
                else
                {
                    //line.start();
                    length = input.read(buffer);
                    if (length > 0)
                    {
                        //write to the audio line (most likely BUFFER_SIZE bytes)- this should start playback
                        line.write(buffer, 0, length);
                    }
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

    public void setDiscard(boolean status)
    {
        discard = status;
    }
}