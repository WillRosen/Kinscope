package sample;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;

public class VoltMeter extends BorderPane {

    Label temp = new Label();

    float voltageValue = 0;

    ArrayList<Float> wave = new ArrayList<>();

    DMM dmm;

    public boolean isLoopingRead;

    public boolean recievedAllValues = false;

    public enum VoltMeterType{
        AC,
        DC
    }

    public VoltMeterType currentVoltMeterType = VoltMeterType.AC;

    ComboBox prescalarValueSelector;
    ComboBox multiplexerValueSelector;
    public VoltMeter(DMM dmm){
        this.dmm=dmm;

        ToggleButton AC = new ToggleButton("AC");
        ToggleButton DC = new ToggleButton("DC");

        DC.setSelected(true);

        ToggleGroup ACDC = new ToggleGroup();
        AC.setToggleGroup(ACDC);
        DC.setToggleGroup(ACDC);

        AC.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                currentVoltMeterType=VoltMeterType.AC;
            }
        });

        DC.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                currentVoltMeterType=VoltMeterType.DC;
            }
        });

        System.out.println("made voltmeter");

        prescalarValueSelector = new ComboBox();
        for (MicroControllerSettings.ADCPrescalarValues type: EnumSet.allOf(MicroControllerSettings.ADCPrescalarValues.class)) {

            prescalarValueSelector.getItems().add(type.toString());
        }

        prescalarValueSelector.getSelectionModel().select(dmm.tempMain.microControllerSettings.getADCPrescalarValue().ordinal());

        prescalarValueSelector.valueProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue ov, String t, String t1) {

                for (MicroControllerSettings.ADCPrescalarValues type: EnumSet.allOf(MicroControllerSettings.ADCPrescalarValues.class)) {

                    if(type.toString()==t1){

                        dmm.tempMain.microControllerSettings.setADCPrescalarValue(type);


                    }
                }

            }
        });

        multiplexerValueSelector = new ComboBox();
        for (MicroControllerSettings.multiplexerValues type: EnumSet.allOf(MicroControllerSettings.multiplexerValues.class)) {
            multiplexerValueSelector.getItems().add(type.toString());
        }
        multiplexerValueSelector.getSelectionModel().select(dmm.tempMain.microControllerSettings.getMultiplexerValue().ordinal());

        //when the dropdown is changed change the current function
        multiplexerValueSelector.valueProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue ov, String t, String t1) {

                for (MicroControllerSettings.multiplexerValues type: EnumSet.allOf(MicroControllerSettings.multiplexerValues.class)) {

                    if(type.toString()==t1){

                        changeMultiplexerValue(type);


                    }
                }

            }
        });

        HBox h = new HBox();
        h.getChildren().addAll(DC,AC);

        HBox h2 = new HBox();
        h2.getChildren().addAll(prescalarValueSelector,multiplexerValueSelector);

        this.setTop(h);
        this.setBottom(temp);
        this.setCenter(h2);
    }
    public void changeMultiplexerValue(MicroControllerSettings.multiplexerValues multiplexerValue){
        dmm.tempMain.microControllerSettings.setMultiplexerValue(multiplexerValue);


    }
    public void recieveValue(String value){
        if(currentVoltMeterType==VoltMeterType.AC){
            handleACData(value);

            //handleWaveData(wave1,wave1);

        }else{
            handleDCData(value);
        }
    }

    public void startUsing(){
        isLoopingRead=true;
        prescalarValueSelector.getSelectionModel().select(dmm.tempMain.microControllerSettings.getADCPrescalarValue().ordinal());
        multiplexerValueSelector.getSelectionModel().select(dmm.tempMain.microControllerSettings.getMultiplexerValue().ordinal());
        System.out.println(dmm==null);
        System.out.println(dmm.tempMain==null);


        dmm.tempMain.dataReader.write(CMD.OscTakeSample);

    }
    public void stopUsing(){
        isLoopingRead=false;
    }



    public void handleDCData(String value){
        try {
            if (recievedAllValues) {
                recievedAllValues = false;
                wave = new ArrayList<>();
            }
            float a = Float.parseFloat(value) / 4096f * 3.3f - 1.59f;
            wave.add(dmm.tempMain.microControllerSettings.convertFrom3VtoTrueValue(a));
            // displayAllWaves();
            //  System.out.println(wave1.size());

        } catch (Exception e) {

            System.out.println(wave.size());

            if (value.split(",")[0].equals("f")) {
                recievedAllValues = true;




                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        temp.setText(Oscilloscope.getScienceNumber(waveDataGetAverageValue(wave))+"V");
                    }
                });

                if (isLoopingRead) {
                    dmm.tempMain.dataReader.write(CMD.OscTakeSample);
                }
            }
        }
    }

    public void handleACData(String value) {
        try {
            if (recievedAllValues) {
                recievedAllValues = false;
                wave = new ArrayList<>();
            }
            float a = Float.parseFloat(value) / 4096f * 3.3f - 1.59f;
            wave.add(dmm.tempMain.microControllerSettings.convertFrom3VtoTrueValue(a));
            // displayAllWaves();
            //  System.out.println(wave1.size());

        } catch (Exception e) {

            System.out.println(wave.size());

            if (value.split(",")[0].equals("f")) {
                recievedAllValues = true;




                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        temp.setText(Oscilloscope.getScienceNumber(waveDataGetRMSValue(wave))+"V");
                    }
                });

                if (isLoopingRead) {
                    dmm.tempMain.dataReader.write(CMD.OscTakeSample);
                }
            }
        }
    }

    public static float waveDataGetRMSValue(ArrayList<Float> wave){
        float peakToPeakValue = (Collections.max(wave)-Collections.min(wave))/2f;
        float rmsValue = peakToPeakValue/((float)(Math.sqrt(2)));
        return rmsValue;
    }
    public static float waveDataGetAverageValue(ArrayList<Float> wave){

        float total = 0;
        for (Float f : wave) {
            total+=f;
        }
        return total/((float)wave.size());
    }

}
