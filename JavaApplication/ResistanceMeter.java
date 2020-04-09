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

import java.util.EnumSet;

public class ResistanceMeter extends BorderPane {
    Label temp = new Label("xMA");
    boolean isLoopingRead=false;
    int numberOfSamplesTaken = 0;



    float arr[] = new float[1024];

    private DMM dmm;

    public ResistanceMeter(DMM dmmt){

        dmm=dmmt;

        this.setCenter(temp);
      //s  this.setLeft(makeResistorComboBox());


    }

    public void startUsing(){
        isLoopingRead=true;

        System.out.println(dmm==null);
        System.out.println(dmm.tempMain==null);

        dmm.tempMain.dataReader.write(CMD.ReadADC2_Ch2);

    }
    public void stopUsing(){
        isLoopingRead=false;
    }

    public void recieveValue(String s){

        System.out.println(s);
        if(numberOfSamplesTaken<arr.length) {

           // String voltages[] = s.split(",");
            float volA0 = Float.parseFloat(s)/4096*3.3f;
            //float volA1 = Float.parseFloat(voltages[1]);
            float rx = findUnknownResistance(volA0);
            arr[numberOfSamplesTaken] = rx;
            numberOfSamplesTaken++;
        }else {
            numberOfSamplesTaken=0;

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    float average = 0;
                    for (float f:arr) {
                        average+=f;
                    }
                    average=average/arr.length;
                    temp.setText(Oscilloscope.getScienceNumber(average)+"Ohms");
                }
            });
        }
        if(isLoopingRead){
            dmm.tempMain.dataReader.write(CMD.ReadADC2_Ch2);
        }

    }


    public float findUnknownResistance(float Sampled){
        float Fixed = 1.58f;
        float unknownResistance = (100000*Sampled)/(2*Fixed-Sampled);
        return unknownResistance;
    }
}
