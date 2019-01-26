package com.erichizdepski.wavetable;

import com.erichizdepski.util.PatchList;
import com.erichizdepski.util.WavePatch;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextInputDialog;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.erichizdepski.wavetable.WavesynConstants.*;

public class Controller {

    private WaveSynthesizer synth;
    private PipedPlayer player;
    private final static Logger LOGGER = Logger.getLogger(Controller.class.getName());

    @FXML
    private ChoiceBox<String> lfoType, wavetableSelect, patchSelect;

    @FXML
    private Slider startIndex, stopIndex, scanRate, pitchSlider;

    @FXML
    private Button savePatch;

    @FXML
    private TextInputDialog patchDaialog;

    ObservableList<String> wavetableOptions;
    ObservableList<String> patchOptions;

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
            scanRate.setValue(MAXSCAN - SCANRATE_DEFAULT);

            //populate the wavetable names and initialize choicebox
            List<String> names = synth.getWavetableNames();
            wavetableOptions = FXCollections.observableList(names);
            //populate the UI choice list
            wavetableOptions.sort(Comparator.comparing(String::toString));
            wavetableSelect.setItems(wavetableOptions);
            wavetableSelect.getSelectionModel().selectFirst();

            //populate the patch names and initialize choicebox
            this.setPatchList();

            //set to default patch
            patchSelect.getSelectionModel().select("default");
            syncUIWithPatch(WavePatch.getDefaultPatch());

            wavetableSelect.valueProperty().addListener((observable, oldValue, newValue) -> {
                synth.setWavetableIndex(wavetableSelect.getSelectionModel().getSelectedIndex());
            });

            //selecting a patch autoloads it
            patchSelect.valueProperty().addListener((observable, oldValue, newValue) -> {
                //just set through the UI- the synth will pick it up automatically
                int patch = patchSelect.getSelectionModel().getSelectedIndex();

                if (patch == -1)
                {
                    return;
                }

                //get the patch values by looking up the patch index in synth
                synth.setPatchIndex(patch);
                WavePatch selectedPatch = synth.getPatch(patch);
                //set the values
                syncUIWithPatch(selectedPatch);
                synth.cacheNotesForPatch();
            });

            // Handle Slider value change events.
            startIndex.valueProperty().addListener((observable, oldValue, newValue) -> {
                synth.setStartIndex(newValue.intValue());
            });

            stopIndex.valueProperty().addListener((observable, oldValue, newValue) -> {
                synth.setStopIndex(newValue.intValue());
            });


            scanRate.valueProperty().addListener((observable, oldValue, newValue) -> {
                //value is 1 to 95. Need to invert so low scan rate is a high number of sample repeats, effectively
                //slowing down the scan rate, Must be >=1.
                synth.setScanRate(MAXSCAN - newValue.intValue());
            });


            //for saving patches
            savePatch.setOnAction((event) ->
            {
                //popup dialog to get patch name
                //create dialog to get patch name on save patch

                //create default from patch settings
                String defaultVal = wavetableSelect.getValue() + "-" +  (int)startIndex.getValue() + ":" + (int)stopIndex.getValue();
                patchDaialog = new TextInputDialog(defaultVal);
                patchDaialog.setTitle(PATCHDIALOGTITLE);
                patchDaialog.setHeaderText(PATCHNAME);

                Optional<String> result = patchDaialog.showAndWait();
                String entered = defaultVal;

                if (result.isPresent()) {

                    entered = result.get();
                }

                //get the enum type for the lfo
               synth.savePatch((int)startIndex.getValue(), (int)stopIndex.getValue(), (int)(MAXSCAN - scanRate.getValue()),
                       wavetableSelect.getSelectionModel().getSelectedIndex(),
                       WaveSynthesizer.LfoType.valueOf(lfoType.getSelectionModel().getSelectedItem()), entered);

                //update patch list to include newly saved patch and select it
                this.setPatchList();

                int index = patchOptions.indexOf(entered);
                //set to new patch
                patchSelect.getSelectionModel().select(index);
                patchSelect.getSelectionModel().isSelected(index);
                syncUIWithPatch(new WavePatch((int)startIndex.getValue(), (int)stopIndex.getValue(), (int)scanRate.getValue(),
                        wavetableSelect.getSelectionModel().getSelectedIndex(),
                        WaveSynthesizer.LfoType.valueOf(lfoType.getSelectionModel().getSelectedItem()), entered));

                //run through all notes for patch and cache them
                synth.cacheNotesForPatch();

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


    private void setPatchList()
    {
        //populate the patch names and initialize choicebox
        List<String> patchNames = synth.getPatchNames();
        patchOptions = FXCollections.observableList(patchNames);
        //sort the list
        patchOptions.sort(Comparator.comparing(String::toString));
        //populate the UI choice list
        patchSelect.setItems(patchOptions);
    }


    private void syncUIWithPatch(WavePatch patch)
    {
        //ensure the UI matches selected patch
        startIndex.setValue(patch.getStartIndex());
        stopIndex.setValue(patch.getStopIndex());
        scanRate.setValue(patch.getScanRate());
        wavetableSelect.setValue(wavetableOptions.get(patch.getWaveTableIndex()));
        lfoType.setValue(patch.getLfoType());
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
