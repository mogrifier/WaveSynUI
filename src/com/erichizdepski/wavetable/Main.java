package com.erichizdepski.wavetable;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Main extends Application {

    private Controller controls;

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("wavesynui.fxml"));
        AnchorPane root = (AnchorPane)loader.load();

        primaryStage.setTitle("WaveSyn");

        Scene scene = new Scene(root, 740, 474);
        primaryStage.setScene(scene);

        primaryStage.show();
        controls = loader.getController();


        //add key listeners to scene
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                controls.noteOn(event);
            }

        });


        scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                controls.noteOff(event);
            }
        });


    }


    @Override
    public void stop() {

        if (controls.shutdown())
        {
            System.out.println("synth shutdown");
        }
        else
        {
            System.out.println("synth failed to hutdown");
        }

        //patches are saved each time you make a patch
        Platform.exit();
        System.exit(0);
    }



    public static void main(String[] args) {
        launch(args);
    }
}
