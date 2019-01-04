package com.erichizdepski.util;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;

/**
 * This class writes the ongoing sound to an output specified by the programmer
 *
 */
public class AudioBufferProcessor implements AudioProcessor {
    ByteBuffer output;
    private final static Logger LOGGER = Logger.getLogger(AudioBufferProcessor.class.getName());
    private int audioLen=0;

    /**
     *
     * @param output bytebuffer for the output
     */
    public AudioBufferProcessor(ByteBuffer output){
        this.output=output;
    }
    @Override
    public boolean process(AudioEvent audioEvent) {

        audioLen+=audioEvent.getByteBuffer().length;

        if (output.capacity() < audioLen)
        {
            //re-allocate. This prevents a buffer overflow crash, but still have some clicking.
            ByteBuffer bigger = ByteBuffer.allocate(audioLen);
            LOGGER.log(Level.INFO, "reallocating");
            bigger.put(output);
            output = bigger;
        }

        output.put(audioEvent.getByteBuffer());
        return true;
    }

    @Override
    public void processingFinished() {
        //noop
    }
}