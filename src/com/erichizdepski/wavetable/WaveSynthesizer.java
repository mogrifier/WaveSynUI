package com.erichizdepski.wavetable;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.PitchShifter;
import be.tarsos.dsp.WaveformSimilarityBasedOverlapAdd;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.io.jvm.WaveformWriter;
import be.tarsos.dsp.resample.RateTransposer;
import com.erichizdepski.util.AudioBufferProcessor;
import com.erichizdepski.util.MidiDeviceCheck;
import com.erichizdepski.util.ShiftPitchSynchronous;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.erichizdepski.wavetable.WavesynConstants.*;

public class WaveSynthesizer extends Thread {

    public enum LfoType
    {
        SAW(LFO_TYPE.get(0)),
        SINE(LFO_TYPE.get(1)),
        TRIANGLE(LFO_TYPE.get(2));

        private String lfo;

        LfoType(String lfo) {
            this.lfo = lfo;
        }

        public String getLfoType()
        {
            return lfo;
        }
    }

    private final static Logger LOGGER = Logger.getLogger(WaveSynthesizer.class.getName());
    PipedOutputStream outflow = null;
    PipedInputStream waveStream = null;
    //for loading wave tables
    List<String> files = null;
    List<ByteBuffer> tables = null;
    boolean alive = false;
    int wavetableIndex = 0;
    int startIndex = STARTINDEX_DEFAULT;
    int stopIndex = STOPINDEX_DEFAULT;
    int scanRate = SCANRATE_DEFAULT;
    //ensure at start up it loads wave data
    boolean changedParameter = true;
    boolean pitchChanged = false;
    int pitch = 1;
    int oldPitch = 1;
    LfoType lfo = LfoType.SAW;

    public WaveSynthesizer() throws IOException {
        TableLoader loader = new TableLoader();

        files = loader.getTableNames("/com/erichizdepski/wavetable/");
        //put the wavetable names in the UI

        //load the wavetables for each file name
        tables = loader.loadTables(files);

        //setup the pipes for audio generation and playback.
        outflow = new PipedOutputStream();
        waveStream = new PipedInputStream();
    }


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

    public int getScanRate() {
        return scanRate;
    }

    public void setScanRate(int scanRate) {
        this.scanRate = scanRate;
        LOGGER.log(Level.INFO, "new scan rate: " + this.scanRate);
        changedParameter = true;
    }


    public PipedInputStream getAudioStream() {
        return waveStream;
    }


    /**
     * Starts a thread and use it to write data to a pipe. Other half of the pipe is in a separate thread.
     */
    public void run() {
        //any data written to waveStream will cause audio playback
        byte[] data = null;
        ByteBuffer pitchShifted = null;

        try {
            waveStream.connect(outflow); //connecting one half is enough
            //need to make the data repeat if desired
            int max = 0;
            //default is -1 to ensure first time through it forces call to generateWaveStream()
            int cachedIndex = -1;
            while (alive) {

                //get a complete data stream of all samples in the wavetable per the scan performed
                /*
                would be smart to cache the data unless the wavetable index, scan parameters, or pitch changes
                 */
                if (changedParameter) {
                    data = generateWaveStream();
                    changedParameter = false;
                }
                max = data.length / WavesynConstants.BUFFERSIZE;

                //now use the new bytebuffer for playback

                for (int i = 0; i < max; i++) {
                    //write buffers of data to the player thread
                    outflow.write(data, i * WavesynConstants.BUFFERSIZE, WavesynConstants.BUFFERSIZE);
                    if (changedParameter) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.ALL, "IOException", e);
        }
    }


    /*
   Creates a stream of audio data using wavtables.
    */
    public byte[] generateWaveStream() {
        ByteBuffer bigBuffer = ByteBuffer.allocate(5000 * 32768);
        byte[] data = new byte[1];
        int index = 0;
        int repeat = 0;
        int tablePick = 0;

        switch (lfo)
        {
            case SAW:
            {
                LOGGER.log(Level.INFO,"saw lfo");
                //now lets play an audio file
                for (int i = startIndex; i < stopIndex; i++) {
                    //build big buffer- make it a patch
                    //index = 35 + (int)Math.floor(Math.random() * 3);
                    tablePick = 50 + (int) Math.floor(Math.random() * 3);
                    repeat = 5 + (int) Math.floor(Math.random() * 10);
                    //super fast
                    repeat = 10;
                    for (int j = 0; j < scanRate; j++) {
                        bigBuffer.put(TableLoader.getWaveForm(tables.get(getWavetableIndex()), i));
                    }
                }
                break;
            }

            case SINE:
            {
                LOGGER.log(Level.INFO,"sine lfo");
                break;
            }

            case TRIANGLE:
            {
                LOGGER.log(Level.INFO,"triangle lfo");
                //now lets play an audio file
                for (int i = startIndex; i < stopIndex; i++) {
                    //build big buffer- make it a patch
                    //index = 35 + (int)Math.floor(Math.random() * 3);
                    tablePick = 50 + (int) Math.floor(Math.random() * 3);
                    repeat = 5 + (int) Math.floor(Math.random() * 10);
                    //super fast
                    repeat = 10;
                    for (int j = 0; j < scanRate; j++) {
                        bigBuffer.put(TableLoader.getWaveForm(tables.get(getWavetableIndex()), i));
                    }
                }

                //now run through backwards
                for (int i = (stopIndex - 2); i > startIndex; i--) {
                    //build big buffer- make it a patch
                    //index = 35 + (int)Math.floor(Math.random() * 3);
                    tablePick = 50 + (int) Math.floor(Math.random() * 3);
                    repeat = 5 + (int) Math.floor(Math.random() * 10);
                    //super fast
                    repeat = 10;
                    for (int j = 0; j < scanRate; j++) {
                        bigBuffer.put(TableLoader.getWaveForm(tables.get(getWavetableIndex()), i));
                    }
                }
                break;
            }
        }

        data = new byte[bigBuffer.position()];
        bigBuffer.rewind();
        bigBuffer.get(data);

        //be sure to set to current pitch
        if (pitch != oldPitch) {
            //should only shift pitch if it changed since last time
            ByteBuffer pitchShifted = ByteBuffer.allocate((int) (data.length * 0.98));
            shiftPitch(data, pitchShifted, pitch);
            pitchChanged = false;
            oldPitch = pitch;
            return pitchShifted.array();
        }
        //else
        return data;
    }


    public void setLfoType(String selectedLfo) {
        //use enumeration to match string name.
        lfo = LfoType.valueOf(selectedLfo);
        changedParameter = true;
    }


    public void setPitch(int pitch) {
        LOGGER.log(Level.INFO, Integer.toString(pitch));
        this.pitch = pitch;
        pitchChanged = true;
        changedParameter = true;
    }

    private void shiftPitch(byte[] source, ByteBuffer target, double cents) {

        //rate is always 44.1khz
        double factor = centToFactor(cents);
        RateTransposer rateTransposer = new RateTransposer(factor);
        WaveformSimilarityBasedOverlapAdd wsola =
                new WaveformSimilarityBasedOverlapAdd(WaveformSimilarityBasedOverlapAdd.Parameters.musicDefaults(factor, WavesynConstants.SAMPLERATE));

        //need a new writer since this writes to disk. Can implement own AudioProcessor.
        AudioBufferProcessor writer = new AudioBufferProcessor(target);
        AudioDispatcher dispatcher = null;

        try {
            //only dealing with mono audio formats
            //factory has a fromByteArray method - should use that. Use this UniversalAudioInputStream  to make inputstream needed
            dispatcher = AudioDispatcherFactory.fromByteArray(source, WavesynConstants.MONO_WAV, wsola.getInputBufferSize(), wsola.getOverlap());
            //dispatcher.setZeroPadLastBuffer(true);
        }
        catch (UnsupportedAudioFileException e)
        {
            e.printStackTrace();
        }

        wsola.setDispatcher(dispatcher);
        dispatcher.addAudioProcessor(wsola);
        dispatcher.addAudioProcessor(rateTransposer);
        dispatcher.addAudioProcessor(writer);
        //Dispatcher is a Runnable.
        dispatcher.run();
    }


    private  double centToFactor(double cents) {
        return 1 / Math.pow(Math.E, cents * Math.log(2) / 1200 / Math.log(Math.E));
    }


    private double factorToCents(double factor) {
        return 1200 * Math.log(1 / factor) / Math.log(2);
    }


}
