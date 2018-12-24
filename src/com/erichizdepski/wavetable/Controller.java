package com.erichizdepski.wavetable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Controller {

    private WaveSynthesizer synth;
    private PipedPlayer player;
    private final static Logger LOGGER = Logger.getLogger(Controller.class.getName());

    @FXML
    private ChoiceBox<String> lfoType, wavetableSelect;

    @FXML
    private void initialize()
    {
        try {
            LOGGER.log(Level.INFO, "initializing...");
            synth = new WaveSynthesizer();
            player = new PipedPlayer(synth.getAudioStream(), WavesynConstants.BUFFERSIZE);

            //handle ChoiceBox value change events
            lfoType.valueProperty().addListener((observable, oldValue, newValue) -> {
                LOGGER.info(observable.toString());

                synth.setLfoType(lfoType.getSelectionModel().getSelectedItem());

            });

            //populate the LFO types and initialize choicebox
            ObservableList<String> lfoOptions = FXCollections.observableList(WavesynConstants.LFO_TYPE);
            //populate the UI choice list
            lfoType.setItems(lfoOptions);
            lfoType.getSelectionModel().selectFirst();

            //populate the wavetable names and initialize choicebox
            List<String> names = synth.getWavetableNames();
            ObservableList<String> wavetableOptions = FXCollections.observableList(names);
            //populate the UI choice list
            wavetableSelect.setItems(wavetableOptions);
            wavetableSelect.getSelectionModel().selectFirst();


            wavetableSelect.valueProperty().addListener((observable, oldValue, newValue) -> {
                synth.setWavetableIndex(wavetableSelect.getSelectionModel().getSelectedIndex());
            });


            synth.setAlive(true);
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


    /*
     * Properly clean up the threads, else the synth can keep playing.
     * @return boolean True means it shutdown. False would be real bad.
     */
    public boolean shutdown()
    {
        synth.setAlive(false);
        player.setAlive(false);
        return true;
    }
}
