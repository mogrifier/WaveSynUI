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
    boolean alive = true;


    public void setAlive(boolean alive) {
        this.alive = alive;
    }


    public WaveSynthesizer()throws IOException
    {
        TableLoader loader = new TableLoader();

        files = loader.getTableNames("/com/erichizdepski/wavetable/");
        tables = loader.loadTables(files);

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
        byte[] data = generateWaveStream();

        try {
            waveStream.connect(outflow); //connecting one half is enough
            //need to make the data repeat if desired
            int max = data.length/WavesynConstants.BUFFERSIZE;
            while(alive) {
                for (int i = 0; i < max; i++) {
                    //write buffers of data to the player thread
                    outflow.write(data, i * WavesynConstants.BUFFERSIZE, WavesynConstants.BUFFERSIZE);

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


            for (int i = 7; i < 60; i++) {
                //build big buffer- make it a patch
                //index = 35 + (int)Math.floor(Math.random() * 3);
                tablePick = 50 +  (int) Math.floor(Math.random() * 3);
                repeat = 5 + (int) Math.floor(Math.random() * 10);
                //super fast
                repeat = 10;
                for (int j = 0; j < repeat; j++) {
                    bigBuffer.put(TableLoader.getWaveForm(tables.get(69), i));
                }
            }


            //now run through backwards

            for (int i = 58; i > 7; i--) {
                //build big buffer- make it a patch
                //index = 35 + (int)Math.floor(Math.random() * 3);
                tablePick = 50 +  (int) Math.floor(Math.random() * 3);
                repeat = 5 + (int) Math.floor(Math.random() * 10);
                //super fast
                repeat = 10;
                for (int j = 0; j < repeat; j++) {
                    bigBuffer.put(TableLoader.getWaveForm(tables.get(69), i));
                }
            }

        //}

        data = new byte[bigBuffer.position()];
        bigBuffer.rewind();
        bigBuffer.get(data);

        return data;
    }
}
