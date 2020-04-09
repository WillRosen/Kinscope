package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;

public class DMM extends BorderPane {

    Main tempMain;
    enum  DMMType{
        Voltage,
        Current,
        Resistance
    }
    DMMType currentDMMType = DMMType.Voltage;


    public VoltMeter voltMeter;
    public CurrentMeter currentMeter ;
    public ResistanceMeter resistanceMeter;



    public DMM(Main tempMainy){

        tempMain=tempMainy;

        voltMeter=new VoltMeter(this);
        currentMeter=new CurrentMeter(this);
        resistanceMeter=new ResistanceMeter(this);
        Button voltMeterButton = new Button("VoltMeter");
        Button currentMeterButton = new Button("currentMeterButton");
        Button resistanceMeterButton = new Button("resistanceMeterButton");

        HBox DMMHBox = new HBox(voltMeterButton,currentMeterButton,resistanceMeterButton);



        this.setTop(DMMHBox);


        voltMeterButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                setCenterTo(DMMType.Voltage);
            }
        });

        currentMeterButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                setCenterTo(DMMType.Current);
            }
        });

        resistanceMeterButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                setCenterTo(DMMType.Resistance);
            }
        });

        //setCenterTo(currentDMMType);


    }

    public void stopUsingDMM(){
        resistanceMeter.stopUsing();
        voltMeter.stopUsing();
        currentMeter.stopUsing();
    }
    public void startUsingDMM(){

        if(currentDMMType==DMMType.Voltage){
            voltMeter.startUsing();
        }else if(currentDMMType==DMMType.Current){
            currentMeter.startUsing();
        }else if(currentDMMType==DMMType.Resistance){
            resistanceMeter.startUsing();

        }
    }

    public void setCenterTo(DMMType dmmType){
        currentDMMType=dmmType;

        voltMeter.stopUsing();
        currentMeter.stopUsing();
        resistanceMeter.stopUsing();

        if(dmmType==DMMType.Voltage){

            this.setCenter(voltMeter);

            voltMeter.startUsing();

        }else if (dmmType==DMMType.Current){

            this.setCenter(currentMeter);

            currentMeter.startUsing();



        }else{

            this.setCenter(resistanceMeter);

            resistanceMeter.startUsing();

        }

    }


    public void recievedNewDataValue(String value){
        try{
           // float inputVoltage = Float.parseFloat(value)/4096f*3.3f-1.5f;
          //  System.out.println(inputVoltage+"asd");
            if(currentDMMType==DMMType.Voltage){
                voltMeter.recieveValue(value);
            }else if(currentDMMType==DMMType.Current){
                currentMeter.recieveValue(value);
            }else if(currentDMMType==DMMType.Resistance){
                resistanceMeter.recieveValue(value);

            }


        }catch (Exception e){

        }

    }


    //public void setKnownResistorTo()



}
