package com.erichizdepski.wavetable;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WaveSynthesizer extends Thread {

    private final static Logger LOGGER = Logger.getLogger(WaveSynthesizer.class.getName());
    PipedOutputStream outflow = null;
    PipedInputStream waveStream =null;
    //for loading wave tables
    List<String> files = null;
    List<ByteBuffer> tables = null;
    boolean alive = false;
    int wavetableIndex = 0;
    int startIndex = 10;
    int stopIndex = 30;
    //ensure at start up it loads wave data
    boolean changedParameter = true;


    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public List<String> getWavetableNames() {
        return files;
    }

    public int getWavetableIndex() {
        return wavetableIndex;
    }

    public void setWavetableIndex(int wavetableIndex) {
        this.wavetableIndex = wavetableIndex;

        LOGGER.log(Level.INFO, "new wavetable index: " + this.wavetableIndex);
        changedParameter = true;
    }


    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
        LOGGER.log(Level.INFO, "new start index: " + this.startIndex);
        changedParameter = true;
    }

    public int getStopIndex() {
        return stopIndex;
    }

    public void setStopIndex(int stopIndex) {
        this.stopIndex = stopIndex;
        LOGGER.log(Level.INFO, "new stop index: " + this.stopIndex);
        changedParameter = true;
    }


    public WaveSynthesizer()throws IOException
    {
        TableLoader loader = new TableLoader();

        files = loader.getTableNames("/com/erichizdepski/wavetable/");
        //put the wavetable names in the UI

        //load the wavetables for each file name
        tables = loader.loadTables(files);

        //setup the pipes for audio generation and playback.
        outflow = new PipedOutputStream();
        waveStream = new PipedInputStream();
    }

    public PipedInputStream getAudioStream()
    {
        return waveStream;
    }


    /**
     * Starts a thread and use it to write data to a pipe. Other half of the pipe is in a separate thread.
     */
    public void run()
    {
        //any data written to waveStream will cause audio playback
        byte[] data = null;

        try {
            waveStream.connect(outflow); //connecting one half is enough
            //need to make the data repeat if desired
            int max = 0;
            //default is -1 to ensure first time through it forces call to generateWaveStream()
            int cachedIndex = -1;
            while(alive) {

                //get a complete data tream of all samples in the wavetable per the scan performed
                /*
                would be smart to cache the data unless the wavetable index changes
                 */
                if (changedParameter) {
                    data = generateWaveStream();
                    changedParameter = false;
                }
                max = data.length/WavesynConstants.BUFFERSIZE;

                for (int i = 0; i < max; i++) {
                    //write buffers of data to the player thread
                    outflow.write(data, i * WavesynConstants.BUFFERSIZE, WavesynConstants.BUFFERSIZE);
                    if (changedParameter)
                    {
                        break;
                    }
                }
            }
        }
        catch (IOException e)
        {
            LOGGER.log(Level.ALL, "IOException", e);
        }
    }


    /*
   Creates a stream of audio data using wavtables. For testing.
    */
    public byte[] generateWaveStream()
    {
        ByteBuffer bigBuffer = ByteBuffer.allocate(5000 * 32768);
        byte [] data = new byte[1];
        int index = 0;
        int repeat = 0;
        int tablePick = 0;

        //for (int k = 0; k < 40; k++) {
            //now lets play an audio file


            for (int i = startIndex; i < stopIndex; i++) {
                //build big buffer- make it a patch
                //index = 35 + (int)Math.floor(Math.random() * 3);
                tablePick = 50 +  (int) Math.floor(Math.random() * 3);
                repeat = 5 + (int) Math.floor(Math.random() * 10);
                //super fast
                repeat = 10;
                for (int j = 0; j < repeat; j++) {
                    bigBuffer.put(TableLoader.getWaveForm(tables.get(getWavetableIndex()), i));
                }
            }


            //now run through backwards

            for (int i = (stopIndex -2); i > startIndex; i--) {
                //build big buffer- make it a patch
                //index = 35 + (int)Math.floor(Math.random() * 3);
                tablePick = 50 +  (int) Math.floor(Math.random() * 3);
                repeat = 5 + (int) Math.floor(Math.random() * 10);
                //super fast
                repeat = 10;
                for (int j = 0; j < repeat; j++) {
                    bigBuffer.put(TableLoader.getWaveForm(tables.get(getWavetableIndex()), i));
                }
            }

        //}

        data = new byte[bigBuffer.position()];
        bigBuffer.rewind();
        bigBuffer.get(data);

        return data;
    }

    public void setLfoType(String lfo)
    {
        //use enumeration to match string name. ehh.
        LOGGER.log(Level.INFO, "lfo type selected: " + lfo);
    }
}
