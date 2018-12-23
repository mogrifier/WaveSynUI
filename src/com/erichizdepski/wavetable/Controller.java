package com.erichizdepski.wavetable;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Controller {

    private WaveSynthesizer synth;
    private PipedPlayer player;
    private final static Logger LOGGER = Logger.getLogger(Controller.class.getName());

    @FXML
    private void initialize()
    {
        LOGGER.log(Level.INFO, "initializing...");
        try {
            synth = new WaveSynthesizer();
            player = new PipedPlayer(synth.getAudioStream(), WavesynConstants.BUFFERSIZE);

            //synth.setAlive(true);
            synth.start();
            player.setAlive(true);
            player.start();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        LOGGER.log(Level.INFO, "initialization complete");
    }



    //FIXME cludge since synth does not keep runnign continuously, but this will stop playback.
    public void stopSynth(ActionEvent actionEvent) {

        synth.setAlive(false);
        player.setAlive(false);
    }


}
