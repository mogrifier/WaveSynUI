package com.erichizdepski.wavetable;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.Synthesizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WaveSynChannel implements MidiChannel {

    private final static Logger LOGGER = Logger.getLogger(WaveSynChannel.class.getName());
    WaveSynthesizer synth;


    public WaveSynChannel(WaveSynthesizer synth)
    {
        this.synth = synth;
    }


    @Override
    public void noteOn(int noteNumber, int velocity) {

        //just send messages to synth to play a note
        LOGGER.log(Level.INFO, "note on " + noteNumber);
    }



    @Override
    public void noteOff(int noteNumber) {

        //send a message to turn off synth
        LOGGER.log(Level.INFO, "note off " + noteNumber);
    }


    //all of these are ignored. just need low level interface to play the synth


    @Override
    public void noteOff(int noteNumber, int velocity) {

    }

    @Override
    public void setPolyPressure(int noteNumber, int pressure) {

    }

    @Override
    public int getPolyPressure(int noteNumber) {
        return 0;
    }

    @Override
    public void setChannelPressure(int pressure) {

    }

    @Override
    public int getChannelPressure() {
        return 0;
    }

    @Override
    public void controlChange(int controller, int value) {

    }

    @Override
    public int getController(int controller) {
        return 0;
    }

    @Override
    public void programChange(int program) {

    }

    @Override
    public void programChange(int bank, int program) {

    }

    @Override
    public int getProgram() {
        return 0;
    }

    @Override
    public void setPitchBend(int bend) {

    }

    @Override
    public int getPitchBend() {
        return 0;
    }

    @Override
    public void resetAllControllers() {

    }

    @Override
    public void allNotesOff() {

    }

    @Override
    public void allSoundOff() {

    }

    @Override
    public boolean localControl(boolean on) {
        return false;
    }

    @Override
    public void setMono(boolean on) {

    }

    @Override
    public boolean getMono() {
        return false;
    }

    @Override
    public void setOmni(boolean on) {

    }

    @Override
    public boolean getOmni() {
        return false;
    }

    @Override
    public void setMute(boolean mute) {

    }

    @Override
    public boolean getMute() {
        return false;
    }

    @Override
    public void setSolo(boolean soloState) {

    }

    @Override
    public boolean getSolo() {
        return false;
    }
}
