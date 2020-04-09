package sample;

import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends Application {
    //This is what controls how often we refresh the wave displaying
  //  Timer dataReadRefresher;
   // Timer dataDisplayRefresher;


    Timer DMMValuesRefresher;

    Image KImage =new Image(getClass().getResourceAsStream("K.png"));

    // This will read the data that the microcontroller gives
    public DataReader dataReader;
    // Oscilloscope is a custom class of containing everything we need to display the oscilloscope (Buttons graphics ECT)
    public  Oscilloscope oscilloscope;
    public DMM dmm;
    public FunctionGenerator functionGenerator;

    public MicroControllerSettings microControllerSettings;

    enum Functions{
        Oscilloscope,
        DMM,
        FunctionGenerator
    }
    Functions currentFunction = Functions.Oscilloscope;

    VBox mainPane;

    @Override
    public void start(Stage primaryStage) throws Exception{
        //We start by setting the scene
        primaryStage.setTitle("Kinscope");
        primaryStage.getIcons().add(KImage);
        //we create the data reading class
        dataReader= new DataReader(this);
      //  if(dataReader.isCommPortFound()) {

         //   createMainPane();


           // primaryStage.setScene(new Scene(mainPane, 650, 600));

            //primaryStage.show();

      //  }else{


      //  }
        createComPortSelect(primaryStage);
        //finally we can show the window to the user
       // primaryStage.setScene(new Scene(mainPane, 700, 600));




    }
    public void createComPortSelect(Stage primaryStage){
        final Stage dialog = new Stage();
        dialog.getIcons().add(KImage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        VBox dialogVbox = new VBox(20);

        Button a = new Button("Auto-Connect");
        a.setOnAction(
        new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                a.setText("Trying....");
                if(dataReader.getCommPorts().length>=1){
                if(dataReader.trySetUpCommPort(dataReader.getCommPorts()[0])) {
                    a.setText("Comm Port Found");
                    a.setDisable(true);
                    createMainPane();
                    primaryStage.setScene(new Scene(mainPane, 790, 630));
                    primaryStage.setResizable(false);
                    primaryStage.show();
                    dialog.close();

                }else {
                    a.setText("Retry Auto-Connect");

                }
                }else{
                    a.setText("Retry Auto-Connect");
                }
            }
        });

        VBox buttons = new VBox();
        buttons.setAlignment(Pos.CENTER);
        for (SerialPort port : dataReader.getCommPorts()) {
            Button PortButton = new Button(port.toString());
            buttons.getChildren().add(PortButton);

            PortButton.setOnAction(
                    new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {

                            if(dataReader.trySetUpCommPort(port)) {

                                a.setDisable(true);
                                createMainPane();
                                primaryStage.setScene(new Scene(mainPane, 700, 600));
                                primaryStage.show();
                                dialog.close();

                            }
                        }
                    });

        }

        Label label = new Label(" "+dataReader.getCommPorts().length+" Port(s) Found");

        dialogVbox.setAlignment(Pos.CENTER);

        dialogVbox.getChildren().addAll(a,label,buttons);
        Scene dialogScene = new Scene(dialogVbox, 300, 200);
        dialog.setScene(dialogScene);
        dialog.show();

    }

    public void createMainPane(){
        //The TopMenuBar is a custom class containing the top menu bar (File,Options,Help ect)
        TopMenuBar menu = new TopMenuBar(this);
        microControllerSettings = new MicroControllerSettings(this);
        //This VBox is so we can have the top menu bar at the top of the window
        mainPane = new VBox(menu);

        //Here we make a new oscilloscope class and add it to the current window
        oscilloscope = new Oscilloscope(this);
        mainPane.getChildren().add(oscilloscope);

        dmm = new DMM(this);

        functionGenerator = new FunctionGenerator(this);


        // then we make a new timer to call the read data function every x milliseconds
       // dataReadRefresher = new Timer();



    }





    public void setFunction(Functions function){
        if(function!=currentFunction) {

            dmm.stopUsingDMM();

            if (function == Functions.Oscilloscope) {
                mainPane.getChildren().add(oscilloscope);
                mainPane.getChildren().remove(dmm);
                mainPane.getChildren().remove(functionGenerator);
                oscilloscope.opened();
                dataReader.dataReciever=DataReader.DataReciever.Oscilloscope;

            }else if (function == Functions.DMM){
                mainPane.getChildren().remove(oscilloscope);
                mainPane.getChildren().add(dmm);
                mainPane.getChildren().remove(functionGenerator);
                dmm.startUsingDMM();
                dataReader.dataReciever=DataReader.DataReciever.DMM;

            }else if( function == Functions.FunctionGenerator){
                mainPane.getChildren().remove(oscilloscope);
                mainPane.getChildren().remove(dmm);
                mainPane.getChildren().add(functionGenerator);

                dataReader.dataReciever=DataReader.DataReciever.FunctionGen;

            }
            currentFunction=function;
        }
    }


    //currently this just passes the datat to the oscilloscope, but will eventually pass data to DMM and FunctionGen
    //public void handleInputData(ArrayList<ArrayList<Float>> listOfChannels){
      //  oscilloscope.handleWaveData(listOfChannels.get(0),listOfChannels.get(1));
   // }


    public void writeDataToBoard(String dataToWrite){
        dataReader.write(dataToWrite);

    }

    public static void main(String[] args) {
        launch(args);

    }

    //This happens when the window closes, its best to stop any processes we currently have running.
    @Override
    public void stop(){
        dataReader.closeDownReadData();
    }
}
