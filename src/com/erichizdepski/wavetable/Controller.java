package com.erichizdepski.wavetable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.erichizdepski.wavetable.WavesynConstants.*;

public class Controller {

    private WaveSynthesizer synth;
    private PipedPlayer player;
    private final static Logger LOGGER = Logger.getLogger(Controller.class.getName());

    @FXML
    private ChoiceBox<String> lfoType, wavetableSelect;

    @FXML
    private Slider startIndex, stopIndex, scanRate, pitchSlider;


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

            stopIndex.setValue(STOPINDEX_DEFAULT);
            startIndex.setValue(STARTINDEX_DEFAULT);
            scanRate.setValue(SCANRATE_DEFAULT);

            //populate the wavetable names and initialize choicebox
            List<String> names = synth.getWavetableNames();
            ObservableList<String> wavetableOptions = FXCollections.observableList(names);
            //populate the UI choice list
            wavetableSelect.setItems(wavetableOptions);
            wavetableSelect.getSelectionModel().selectFirst();


            wavetableSelect.valueProperty().addListener((observable, oldValue, newValue) -> {
                synth.setWavetableIndex(wavetableSelect.getSelectionModel().getSelectedIndex());
            });


            // Handle Slider value change events.
            startIndex.valueProperty().addListener((observable, oldValue, newValue) -> {
                synth.setStartIndex(newValue.intValue());
            });

            stopIndex.valueProperty().addListener((observable, oldValue, newValue) -> {
                synth.setStopIndex(newValue.intValue());
            });


            scanRate.valueProperty().addListener((observable, oldValue, newValue) -> {
                //value is 1 to 101. Need to invert so low scan rate is a high number of sample repeats, effectively
                //slowing down the scan rate, Must be >=1.
                synth.setScanRate(101 - newValue.intValue());
            });

            //pitch control test
            pitchSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                synth.setPitch(newValue.intValue());
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
