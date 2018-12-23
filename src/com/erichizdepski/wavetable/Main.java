package com.erichizdepski.wavetable;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Main extends Application {

    private Controller controls;

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("wavesynui.fxml"));
        AnchorPane root = (AnchorPane)loader.load();

        primaryStage.setTitle("Wavesyn");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
        controls = loader.getController();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
