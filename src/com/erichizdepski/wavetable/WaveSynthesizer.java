package com.erichizdepski.wavetable;

import be.tarsos.dsp.*;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.resample.RateTransposer;
import com.erichizdepski.util.*;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    int wavetableIndex = TABLEINDEX_DEFAULT;
    int startIndex = STARTINDEX_DEFAULT;
    int stopIndex = STOPINDEX_DEFAULT;
    int scanRate = SCANRATE_DEFAULT;
    //ensure at start up it loads wave data
    boolean changedParameter = true;
    int desiredPitch = 0;
    int actualPitch = 0;
    LfoType lfo = LfoType.SAW;
    //patch handling
    PatchList patches;

    Map<String, ByteBuffer> patchHash = new HashMap<>(4000);



    public WaveSynthesizer() throws IOException {
        TableLoader loader = new TableLoader();

        files = loader.getTableNames("/com/erichizdepski/wavetable/");
        //put the wavetable names in the UI

        //load the wavetables for each file name
        tables = loader.loadTables(files);

        patches = new PatchList(50);

        //setup the pipes for audio generation and playback.
        outflow = new PipedOutputStream();
        waveStream = new PipedInputStream();
    }

    public List<String> getPatchNames() {

        return patches.getPatchNames();
    }

    public void savePatch(int start, int stop, int rate, int index, LfoType type, String name) {
        //delegate to patch and patchlist classes
        //LOGGER.log(Level.INFO, "saving the patch");
        patches.savePatch (new WavePatch(start, stop, rate, index, type, name));
    }


    public WavePatch getPatch(int index)
    {
        return patches.getPatch(index);
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

        //LOGGER.log(Level.INFO, "new wavetable index: " + this.wavetableIndex);
        changedParameter = true;
    }


    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
        //LOGGER.log(Level.INFO, "new start index: " + this.startIndex);
        changedParameter = true;
    }

    public int getStopIndex() {
        return stopIndex;
    }

    public void setStopIndex(int stopIndex) {
        this.stopIndex = stopIndex;
        //LOGGER.log(Level.INFO, "new stop index: " + this.stopIndex);
        changedParameter = true;
    }

    public int getScanRate() {
        return scanRate;
    }

    public void setScanRate(int scanRate) {
        this.scanRate = scanRate;
        //LOGGER.log(Level.INFO, "new scan rate: " + this.scanRate);
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

        try {
            waveStream.connect(outflow); //connecting one half is enough
            //need to make the data repeat if desired
            double max = 0;
            //default is -1 to ensure first time through it forces call to generateWaveStream()
            int cachedIndex = -1;
            while (alive) {

                //get a complete data stream of all samples in the wavetable per the scan performed
                /*
                would be smart to cache the data unless the wavetable index, scan parameters, or pitch changes
                 */
                if (changedParameter)
                {
                    //check cache
                    if (patchHash.containsKey(getHash(getPitch())))
                    {
                        //use cached value
                        data = patchHash.get(getHash(getPitch())).array();
                    }
                    else
                    {
                        //note- am only caching when you save a patch.
                        data = generateWaveStream();
                    }

                    //reloading data uses a new, un-pitchshifted sample
                    actualPitch = 0;
                    changedParameter = false;
                    LOGGER.log(Level.INFO,"parameter changed");
                    //AudioHelpers.saveFile(data, "audio.wav");
                }

                //now use the new bytebuffer for playback

                //I used short buffers for writing to allow realtime control of parameters
                //when pitch changes you get an unusual sized buffer and some it is no longer smooth for looping so
                //must be fixed.

                max = data.length / WavesynConstants.BUFFERSIZE;
                for (int i = 0; i < (int)max; i++) {
                    //write buffers of data to the player thread
                    outflow.write(data, i * BUFFERSIZE, BUFFERSIZE);
                    if (changedParameter) {
                        break;
                    }
                }

                //need to ensure this is proper length and even byte aligned. how?
                if ( data.length % BUFFERSIZE > 0)
                {
                    int overage = data.length % BUFFERSIZE;
                    //got extra data. copy the remaining data at end of buffer and fade it out
                    byte[] extra =  Arrays.copyOfRange(data, data.length - overage, data.length);   // AudioHelpers.fadeOut(Arrays.copyOfRange(data, data.length - overage, data.length));
                    outflow.write(extra);
                    //LOGGER.log(Level.INFO, "wrote extra faded audio " + extra.length);
                    //AudioHelpers.saveFile(extra, "extra.wav");
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
        ByteBuffer bigBuffer = ByteBuffer.allocate(MAXSIZE); //10MB looks good
        byte[] data = new byte[1];
        byte[] sample;

        switch (lfo)
        {
            case SAW:
            {
                //now lets play an audio file
                for (int i = startIndex; i < stopIndex; i++) {
                    //build big buffer- make it a patch
                    sample = TableLoader.getWaveForm(tables.get(getWavetableIndex()), i);
                    for (int j = 0; j < scanRate; j++) {
                        bigBuffer.put(sample);
                    }
                }
                break;
            }

            case SINE: {
                break;
            }

            case TRIANGLE:
            {
                //now lets play an audio file
                for (int i = startIndex; i < stopIndex; i++) {
                    //build big buffer- make it a patch
                    sample = TableLoader.getWaveForm(tables.get(getWavetableIndex()), i);
                    for (int j = 0; j < scanRate; j++) {
                        bigBuffer.put(sample);
                    }
                }

                //now run through backwards
                for (int i = (stopIndex - 2); i > startIndex; i--) {
                    //build big buffer- make it a patch
                    sample = TableLoader.getWaveForm(tables.get(getWavetableIndex()), i);
                    for (int j = 0; j < scanRate; j++) {
                        bigBuffer.put(sample);
                    }
                }
                break;
            }
        }

        data = new byte[bigBuffer.position()];
        LOGGER.log(Level.INFO, "audio length = " + data.length + " original buffer length = " + MAXSIZE);
        bigBuffer.rewind();
        bigBuffer.get(data);

        //be sure to set to current pitch
        if (desiredPitch != actualPitch) {
            //should only shift pitch if it changed since last time
            ByteBuffer pitchShifted = ByteBuffer.allocate((int) (data.length));
            shiftPitch(data, pitchShifted, desiredPitch);
            actualPitch = desiredPitch;
            //LOGGER.log(Level.INFO, "shifted pitch");

            //pitch shifting induces a string of zero byte values (about 100 bytes) wide near end of first buffer (4-5000 bytes)

            if (lfo == LfoType.SAW)
            {
                //no smoothing used
                return AudioHelpers.trim(pitchShifted.array());
            }

            //else
            //return AudioHelpers.smoothBySlope(AudioHelpers.trim(pitchShifted.array()));

            //AudioHelpers.saveFile(AudioHelpers.trim(pitchShifted.array()), "presmoothed.wav");

            return AudioHelpers.smoothByCycle(AudioHelpers.trim(pitchShifted.array()));

        }
        //else
        return data;
    }



    /*
    Generate a wave form for every note in a patch. Store in a hash table.
     */
    public void cacheNotesForPatch()
    {
        //save current pitch and restore to keep ui in sync with backing values
        int currentPitch = getPitch();

        //just doing 30 notes for now. remember, it is a pitch differential. '0' is really D3 I think.
        int notes = MAXPITCH/100 + 1;

        for (int i = 0; i < notes; i++)
        {
            setPitch(i * 100);
            ByteBuffer pitch = ByteBuffer.wrap(generateWaveStream());
            //cache the pitch using a hash of pitch and patch
            patchHash.put(getHash(i*100), pitch);
        }

        //restore pitch
        setPitch(currentPitch);
    }


    private String getHash(int pitch)
    {
        //use current patch settings int start, int stop, int rate, int index, LfoType type, in hash
        //plus the pitch to keep unique. colons prevent chance of collision. Example 1 15 22 is same as 11 52 2 without :

        String hash = String.valueOf(getStartIndex()) + ":" + String.valueOf(getStopIndex()) + ":" + String.valueOf(getScanRate())
                + ":" + String.valueOf(getWavetableIndex()) + ":" + lfo.getLfoType() + ":" + String.valueOf(pitch);
        return hash;
    }


    public void setLfoType(String selectedLfo) {
        //use enumeration to match string name.
        lfo = LfoType.valueOf(selectedLfo);
        changedParameter = true;
    }


    public void setPitch(int pitch) {
        //LOGGER.log(Level.INFO, "Pitch cents change " + Integer.toString(pitch));
        desiredPitch = pitch;
        changedParameter = true;
    }


    public int getPitch()
    {
        return desiredPitch;
    }

    private void shiftPitch(byte[] source, ByteBuffer target, double cents) {

        //rate is always 44.1khz
        double factor = centToFactor(cents);
        RateTransposer rateTransposer = new RateTransposer(factor);
        WaveformSimilarityBasedOverlapAdd wsola =
                new WaveformSimilarityBasedOverlapAdd(WaveformSimilarityBasedOverlapAdd.Parameters.musicDefaults(1, WavesynConstants.SAMPLERATE));

        //need a new writer since this writes to disk. Can implement own AudioProcessor.
        AudioBufferProcessor writer = new AudioBufferProcessor(target);
        AudioDispatcher dispatcher = null;

        try {
            //only dealing with mono audio formats
            //factory has a fromByteArray method - should use that. Use this UniversalAudioInputStream  to make inputstream needed
            //LOGGER.log(Level.INFO, "wsola buffer= " + wsola.getInputBufferSize());
            dispatcher = AudioDispatcherFactory.fromByteArray(source, MONO_WAV, wsola.getInputBufferSize(), wsola.getOverlap());
            dispatcher.setZeroPadLastBuffer(true);
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
