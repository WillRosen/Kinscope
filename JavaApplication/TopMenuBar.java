package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

public class TopMenuBar extends MenuBar {

    Main main;

    public TopMenuBar(Main mains){
        main = mains;
        //we create the main headers
        Menu file = new Menu("File");

        Menu oscilloscope = new Menu("Oscilloscope");
        Menu dmm = new Menu("DMM");
        Menu functionGenerator = new Menu("Function Generator");


        //and the sub headers
        MenuItem quit = new MenuItem("Quit");
        MenuItem saveWave = new MenuItem("Save wave");

        MenuItem startOscilloscope = new MenuItem("Open Oscilloscope");
        MenuItem startMultimeter = new MenuItem("Open Multimeter");
        MenuItem startFunctionGenerator = new MenuItem("Open FunctionGenerator");

        //then add them to the correct main headers
        file.getItems().add(quit);



        oscilloscope.getItems().add(startOscilloscope);
        oscilloscope.getItems().add(saveWave);
        dmm.getItems().add(startMultimeter);
        functionGenerator.getItems().add(startFunctionGenerator);


        //add functions
        quit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Platform.exit();
                System.exit(0);
            }
        });
        saveWave.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                main.oscilloscope.saveWave();
            }
        });
        startOscilloscope.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                main.setFunction(Main.Functions.Oscilloscope);
            }
        });
        startMultimeter.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                main.setFunction(Main.Functions.DMM);
            }
        });
        startFunctionGenerator.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                main.setFunction(Main.Functions.FunctionGenerator);
            }
        });

        //then add them to this, which is a MenuBar
        this.getMenus().addAll(file,oscilloscope,dmm,functionGenerator);
    }
}
