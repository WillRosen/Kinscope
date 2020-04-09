package sample;

        import javafx.application.Platform;
        import javafx.event.ActionEvent;
        import javafx.event.EventHandler;
        import javafx.scene.control.Button;
        import javafx.scene.control.Label;
        import javafx.scene.layout.BorderPane;

public class CurrentMeter extends BorderPane {
    Label temp = new Label("xMA");
    boolean isLoopingRead=false;
    int numberOfSamplesTaken = 0;
    float currentValue = 0;

    float arr[] = new float[1024];

    private DMM dmm;

    public CurrentMeter(DMM dmmt){

        dmm=dmmt;
        this.setCenter(temp);


    }
    public void recieveValue(String value){

        if(numberOfSamplesTaken<arr.length) {

        //String voltages[] = value.split(",");
        float volA0 = Float.parseFloat(value);
      //  float volA1 = Float.parseFloat(voltages[1]);

        float current = findCurrent(volA0);
            arr[numberOfSamplesTaken] = current;
            numberOfSamplesTaken++;
        }else {
            numberOfSamplesTaken = 0;

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    float average = 0;
                    for (float f : arr) {
                        average += f;
                    }
                    average = average / arr.length;
                    temp.setText(Oscilloscope.getScienceNumber(average) + "A");
                }
            });
        }
        if(isLoopingRead){
            dmm.tempMain.dataReader.write(CMD.ReadADC2_Ch3);
        }

    }
    public void startUsing(){
        isLoopingRead=true;

        System.out.println(dmm==null);
        System.out.println(dmm.tempMain==null);

        dmm.tempMain.dataReader.write(CMD.ReadADC2_Ch3);

    }
    public void stopUsing(){
        isLoopingRead=false;
    }

    public float findCurrent(float volA0){
        float knownResistance = 10;
        float current = 0;
        float trueVoltage = ((volA0*3.3f)/4096);
        current = (trueVoltage/knownResistance)+0.005f;
        return current;
    }
}
