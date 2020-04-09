package sample;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.Buffer;
import java.util.*;

import static javax.swing.text.StyleConstants.Orientation;

public class Oscilloscope extends BorderPane {

    //the gc will allow us to display the waves
    GraphicsContext gc;
    Canvas canvas;

    int a=0;

    //this is a user defined voltage gain
    float VoltagePerDivision = 1;

    final int BufferSize = 1024;

    final int canvasSizeXY = 520;

    float secondsPerDivision = 0.001024f;

    float pixelsPerSample=0;

    float triggerVoltage=0;

    float cursor1Time = 0;
    float cursor2Time = 0;

    Label Cursor1Label = new Label("Cursor 1:");
    Label Cursor2Label = new Label("Cursor 2:");
    Label CursorDiffLabel = new Label("Cursor Difference:");

    //This is a reference to the Main class so we can use it later
    Main main;

    //These arraylists store the data for two waves
    ArrayList<Float> wave1 = new ArrayList<>();
    ArrayList<Float> incommingWave = new ArrayList<>();


    ArrayList<Float> mathematicalWave = new ArrayList<>();

    //This is purely for displaying the wave, how many pixels does one voltage division take up
    int pixelsPerDivision = 20;

    //This will display the current user defined voltage division
    Text voltagePerDivisionLabel;
    Text SecondsPerDivisionLabel;

    //these define the different mathematical value we can do
    //mathOpType becomes a variable type like int, but can only take the values seen below
    enum mathOpType{
        None,
        AAddB,
        AMinusB,
        ADividedByB,
        AMultipiedByB,
        FFTA,
    }
    mathOpType currentMathOpType = mathOpType.None;

    enum mathWaveSelection{None,One,Two}
    mathWaveSelection mathWaveA = mathWaveSelection.None;
    mathWaveSelection mathWaveB = mathWaveSelection.None;


    enum cursorTypeSelection{
        None,
        SingleValue,
        DoubleValue,
    }

    cursorTypeSelection CursorType = cursorTypeSelection.None;







    ArrayList<String> lowerFixes = new ArrayList<>();
    ArrayList<String> higherFixes = new ArrayList<>();

   // ProgressBar recieveProgress = new ProgressBar(0);

    boolean showWave1 = true;
    boolean showWave2 = true;
    boolean showWaveMath = true;

    Color wave1Colour = Color.YELLOW;
    Color Cursor1Colour = Color.CYAN;
    Color Cursor2Colour = Color.LIGHTGREEN;
    Color backgroundColour = Color.BLACK;
    Color divisionsColour = Color.WHITE;
    Color voltageTextColour = Color.WHITE;
    int waveThickness = 2;

    Text tempWave1String = new Text("");
    Text tempWave2String = new Text("");

    boolean recievedAllValues;

    boolean requestDataLoop = false;

    float SecondsPerSample = 0;

    boolean drawFromRisingEdge = false;


    Label MeasurementFrequency = new Label("Frequency:\t\t -");
    Label MeasurementMin= new Label("Minimum:\t\t -");
    Label MeasurementMax= new Label("Maximum:\t\t -");
    Label MeasurementPeakToPeak= new Label("Peak To Peak:\t\t -");
    Label MeasurementRMS = new Label("RMS:\t\t\t -");






    ComboBox prescalarValueSelector;
    ComboBox multiplexerValueSelector;
    //This get called when we create a new Oscilliscope Object, this should only happen once
    public Oscilloscope(Main tempMain) {

        lowerFixes.add("m");
        lowerFixes.add("mn");
        lowerFixes.add("n");
        lowerFixes.add("p");

        higherFixes.add("K");
        higherFixes.add("M");
        higherFixes.add("G");
        higherFixes.add("T");



        //store the reference to main
        main=tempMain;

        //then we can add the canvas and graphics for displaying the wave
        canvas = new Canvas(canvasSizeXY, canvasSizeXY);
        gc = canvas.getGraphicsContext2D();
        this.setCenter(canvas);//and display that at the center of the pane

        //make the buttons





        TitledPane controlsPane = new TitledPane("Controls" , makeControlsPane());
        TitledPane divisionsPane = new TitledPane("Divisions" , makeDivisionsPane());
        TitledPane triggerPane = new TitledPane("Trigger",makeTriggerPane());
       // TitledPane mathematicalWavePane = new TitledPane("Mathematical Wave" , makeMathematicalWavePane());
        TitledPane displayPane = new TitledPane("Display" , makeDisplayPane());
        TitledPane cursorPane = new TitledPane("Cursors" , makeCursorPane());

        controlsPane.maxWidth(2000);


        ScrollPane sp=new ScrollPane();

        sp.maxWidth(2000);

        //We add these buttons to a horizontal layout box
        VBox Panes = new VBox();
        Panes.getChildren().addAll(controlsPane,divisionsPane,triggerPane,displayPane,cursorPane);
        Panes.setMaxWidth(2000);
        sp.setContent(Panes);
        //and add then add this boc to the boarder pane
        this.setRight(sp);



        //new we will add info for each wave
        VBox tempStrings = new VBox(MeasurementFrequency,MeasurementMin,MeasurementMax,MeasurementPeakToPeak,MeasurementRMS);
        this.setBottom(tempStrings);
        wave1=new ArrayList<>();
        wave1.add(0f);
        displayAllWaves();
    }

    public Pane makeTriggerPane(){
        HBox h = new HBox();

        Label l = new Label("TriggerVoltage (V)");

        TextField textField = new TextField("0");
        textField.setMaxWidth(75);
        textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("-?\\d{0,7}([\\.]\\d{0,4})?")) {
                    textField.setText(oldValue);
                }else{
                    try{
                        triggerVoltage=Float.parseFloat(newValue);
                        displayAllWaves();
                    }catch (Exception e){

                    }
                }
            }
        });



        h.getChildren().addAll(l,textField);
        return h;
    }


    Pane Cursor1Box = new Pane();
    Pane Cursor2Box = new Pane();
    Separator Sep1 =  new Separator(javafx.geometry.Orientation.HORIZONTAL);
    Separator Sep2 =  new Separator(javafx.geometry.Orientation.HORIZONTAL);
    public Pane makeCursorPane(){
       VBox v = new VBox();

        Cursor1Box = makeSingleCursor1();
        Cursor2Box = makeSingleCursor2();


        ComboBox cursorTypeDropDown = new ComboBox();
        for (cursorTypeSelection type: EnumSet.allOf(cursorTypeSelection.class)) {
            cursorTypeDropDown.getItems().add(type.toString());
        }
        cursorTypeDropDown.getSelectionModel().selectFirst();

        //when the dropdown is changed change the current function
        cursorTypeDropDown.valueProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue ov, String t, String t1) {
                for (cursorTypeSelection type: EnumSet.allOf(cursorTypeSelection.class)) {
                    if(type.toString()==t1){
                        CursorType=type;
                        v.getChildren().remove(Cursor1Box);
                        v.getChildren().remove(Cursor2Box);
                        v.getChildren().remove(Sep1);
                        v.getChildren().remove(Sep2);

                        if(CursorType==cursorTypeSelection.SingleValue){
                            v.getChildren().add(Sep1);
                            v.getChildren().add(Cursor1Box);

                        }else if (CursorType==cursorTypeSelection.DoubleValue){
                            v.getChildren().add(Sep1);
                            v.getChildren().add(Cursor1Box);
                            v.getChildren().add(Sep2);
                            v.getChildren().add(Cursor2Box);
                            //makeDoubleCursor(gridPane);
                        }else{

                        }
                    }
                }

            }
        });

        v.getChildren().add(cursorTypeDropDown);

        return v;
    }

    public Pane makeSingleCursor1(){

        Label l = new Label("Time 1 (ms)");
        TextField cursorTime = new TextField();
        cursorTime.setText(cursor1Time+"");
        cursorTime.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d{0,7}([\\.]\\d{0,6})?")) {
                    cursorTime.setText(oldValue);
                }else{
                    try{
                        cursor1Time=Float.parseFloat(newValue);
                        displayAllWaves();
                    }catch (Exception e){

                    }
                }
            }
        });


        ColorPicker cursorColourPicker = new ColorPicker(Cursor1Colour);
        cursorColourPicker.setMaxWidth(50);
        cursorColourPicker.setOnAction(new EventHandler() {
            public void handle(Event t) {
                Cursor1Colour=cursorColourPicker.getValue();
                displayAllWaves();
            }
        });

        Button Plus1 = new Button("+1ms");
        Button Plus10 = new Button("+10ms");
        Button Plus100 = new Button("+100ms");

        Plus1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                cursor1Time+=1;
                cursorTime.setText(cursor1Time+"");
                displayAllWaves();
            }
        });
        Plus10.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                cursor1Time+=10;
                cursorTime.setText(cursor1Time+"");
                displayAllWaves();
            }
        });
        Plus100.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                cursor1Time+=100;
                cursorTime.setText(cursor1Time+"");
                displayAllWaves();
            }
        });

        Button Minus1 = new Button("-1ms");
        Button Minus10 = new Button("-10ms");
        Button Minus100 = new Button("-100ms");

        Minus1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                cursor1Time-=1;
                cursorTime.setText(cursor1Time+"");
                displayAllWaves();
            }
        });
        Minus10.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                cursor1Time-=10;
                cursorTime.setText(cursor1Time+"");
                displayAllWaves();
            }
        });
        Minus100.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                cursor1Time-=100;
                cursorTime.setText(cursor1Time+"");
                displayAllWaves();
            }
        });

        //temp.getChildren().addAll(l,cursorColourPicker,cursorTime,Cursor1Label);
        return new VBox(new HBox(l,cursorTime),new HBox(Plus1,Plus10,Plus100),new HBox(Minus1,Minus10,Minus100),new HBox(cursorColourPicker,Cursor1Label));

    }
    public Pane makeSingleCursor2(){
        Label l = new Label("Time 2 (ms)");
        TextField cursorTime = new TextField();
        cursorTime.setText(cursor2Time+"");
        cursorTime.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d{0,7}([\\.]\\d{0,6})?")) {
                    cursorTime.setText(oldValue);
                }else{
                    try{
                        cursor2Time=Float.parseFloat(newValue);
                        displayAllWaves();
                    }catch (Exception e){

                    }
                }
            }
        });


        ColorPicker cursorColourPicker = new ColorPicker(Cursor2Colour);
        cursorColourPicker.setMaxWidth(50);
        cursorColourPicker.setOnAction(new EventHandler() {
            public void handle(Event t) {
                Cursor1Colour=cursorColourPicker.getValue();
                displayAllWaves();
            }
        });


        Button Plus1 = new Button("+1ms");
        Button Plus10 = new Button("+10ms");
        Button Plus100 = new Button("+100ms");

        Plus1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                cursor2Time+=1;
                cursorTime.setText(cursor2Time+"");
                displayAllWaves();
            }
        });
        Plus10.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                cursor2Time+=10;
                cursorTime.setText(cursor2Time+"");
                displayAllWaves();
            }
        });
        Plus100.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                cursor2Time+=100;
                cursorTime.setText(cursor2Time+"");
                displayAllWaves();
            }
        });

        Button Minus1 = new Button("-1ms");
        Button Minus10 = new Button("-10ms");
        Button Minus100 = new Button("-100ms");

        Minus1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                cursor2Time-=1;
                cursorTime.setText(cursor2Time+"");
                displayAllWaves();
            }
        });
        Minus10.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                cursor2Time-=10;
                cursorTime.setText(cursor2Time+"");
                displayAllWaves();
            }
        });
        Minus100.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                cursor2Time-=100;
                cursorTime.setText(cursor2Time+"");
                displayAllWaves();
            }
        });

        //temp.getChildren().addAll(l,cursorColourPicker,cursorTime,Cursor1Label);
        return new VBox(new HBox(l,cursorTime),new HBox(Plus1,Plus10,Plus100),new HBox(Minus1,Minus10,Minus100),new HBox(cursorColourPicker,Cursor2Label),new HBox(CursorDiffLabel));

    }

    public Pane makeControlsPane(){


        prescalarValueSelector = new ComboBox();
        for (MicroControllerSettings.ADCPrescalarValues type: EnumSet.allOf(MicroControllerSettings.ADCPrescalarValues.class)) {

            prescalarValueSelector.getItems().add(type.toString());
        }
        prescalarValueSelector.getSelectionModel().select(main.microControllerSettings.getADCPrescalarValue().ordinal());

        prescalarValueSelector.valueProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue ov, String t, String t1) {

                for (MicroControllerSettings.ADCPrescalarValues type: EnumSet.allOf(MicroControllerSettings.ADCPrescalarValues.class)) {

                    if(type.toString()==t1){

                        main.microControllerSettings.setADCPrescalarValue(type);


                    }
                }

            }
        });

        multiplexerValueSelector = new ComboBox();
        for (MicroControllerSettings.multiplexerValues type: EnumSet.allOf(MicroControllerSettings.multiplexerValues.class)) {
            multiplexerValueSelector.getItems().add(type.toString());
        }
        multiplexerValueSelector.getSelectionModel().select(main.microControllerSettings.getMultiplexerValue().ordinal());

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



        Button startStop = new Button("Start");

        startStop.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if(requestDataLoop==true){
                    // main.stopRefreshingData();//if we are updating and this is clicked then stop
                    startStop.setText("Start");//and change the text

                    requestDataLoop=false;

                }else{
                    // main.startRefreshingData();//if we are not updating and this is clicked then start
                    startStop.setText("Stop");//update text
                    makeDataReading();
                    requestDataLoop=true;
                }

            }
        });


        HBox b = new HBox();
        b.getChildren().addAll(startStop,prescalarValueSelector,multiplexerValueSelector);
        return b;
    }

    public void makeDataReading(){

        ArrayList<Float> x=main.dataReader.makeDataReading();
      //  handleWaveData(x,x);

    }
    public void saveWave(){

        try (PrintStream out = new PrintStream(new FileOutputStream("wave.csv"))) {
            out.print(getCSV());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public String getCSV(){
        String StringToReturn = new String();
        float i=0;
        for (Float F:wave1) {
            StringToReturn+=String.format("%.8f",i)+","+F+"\n";
            i+=SecondsPerSample;
        }
        return StringToReturn;
    }

    public float findFrequency(ArrayList<Float> wave){

        //int smoother = 4;

        ArrayList<Float> newWave = new ArrayList<>();

        for(int i=0;i<wave.size()-4;i++){
            newWave.add((wave.get(i)+wave.get(i+1)+wave.get(i+2)+wave.get(i+4))/4f);
        }


        boolean isBelow = wave1.get(0)<triggerVoltage;

        ArrayList<Integer> triggerSamples = new ArrayList<>();


        System.out.println("starting f calc");

        for(int i=1;i<newWave.size();i++){
            if(isBelow){
                if(newWave.get(i)>triggerVoltage){
                    triggerSamples.add(i);
                    isBelow=false;
                }
            }else{
                if(newWave.get(i)<triggerVoltage){
                    triggerSamples.add(i);
                    isBelow=true;
                }
            }
        }
        System.out.println("F len in "+triggerSamples.size());

        ArrayList<Float> freqValues = new ArrayList<>();


        for(int i=0;i<triggerSamples.size()-2;i++){
            freqValues.add(1/(Math.abs(SecondsPerSample*(triggerSamples.get(i)-triggerSamples.get(i+2)))));
        }

        float finalAVG =0;
        for (Float f : freqValues) {
            finalAVG+=f;
        }
        finalAVG/=freqValues.size();


       return finalAVG;
        //System.out.println(finalAVG);
    }

    public void recievedNewDataValue(String value){
        try{
            if(recievedAllValues){
                recievedAllValues=false;
                incommingWave=new ArrayList<>();
            }
            float a = Float.parseFloat(value)/4096f*3.3f-1.59f;
            incommingWave.add(main.microControllerSettings.convertFrom3VtoTrueValue(a));



             /*   Platform.runLater(new Runnable() {
                    @Override
                    public void run() { try {
                        recieveProgress.setProgress(incommingWave.size() / (float)BufferSize);
                    }catch (Exception e){

                    }
                    }
                });
*/

           // displayAllWaves();
          //  System.out.println(wave1.size());

        }catch (Exception e){

            System.out.println(wave1.size());
            wave1=incommingWave;
            if(value.split(",")[0].equals("f")){
                recievedAllValues=true;
               // System.out.println(SecondsPerSample+"------------------------");
                SecondsPerSample=Float.parseFloat(value.split(",")[1])/8000000f;//total time for all samples
              //  System.out.println(SecondsPerSample+"total time to sample");

                SecondsPerSample/=BufferSize;//time per sample
               // System.out.println(SecondsPerSample+"seconds per sample");

                float DivPerSample = SecondsPerSample/secondsPerDivision;

                pixelsPerSample = DivPerSample*pixelsPerDivision;

             //   float divisionsOnScreen = canvasSizeXY/(float)pixelsPerDivision;
              //  System.out.println(pixelsPerSample+" pixels per sample");

               /* float samplesPerDivision= BufferSize/divisionsOnScreen;
                System.out.println(samplesPerDivision+"samples per division");

                float secondsPerDivision = SecondsPerSample/samplesPerDivision;
                System.out.println(secondsPerDivision+",sec Per Divison");
*/
                displayAllWaves();

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        float freq = findFrequency(wave1);
                        if(Float.isNaN(freq)){
                            MeasurementFrequency.setText("Frequency:\t\t? Hz");
                        }else{
                            MeasurementFrequency.setText("Frequency:\t\t" +getScienceNumber(findFrequency(wave1))+"Hz");
                        }

                        MeasurementMin.setText("Minimum:\t\t" +getScienceNumber(waveDataGetLowestValue(wave1))+"V");
                        MeasurementMax.setText("Maximum:\t\t" +getScienceNumber(waveDataGetHighestValue(wave1))+"V");
                        MeasurementPeakToPeak.setText("Peak To Peak:\t\t" +getScienceNumber(waveDataGetPeakToPeakValue(wave1))+"V");

                        MeasurementRMS.setText("RMS:\t\t\t"+getScienceNumber(waveDataGetRMSValue(wave1))+"V");
                    }
                });

                if(requestDataLoop) {
                    makeDataReading();
                }
            }
            //handleWaveData(wave1,wave1);

        }
    }

    public Pane makeMathematicalWavePane(){
        //make and fill the drop down
        ComboBox mathTypeSelector = new ComboBox();
        for (mathOpType type: EnumSet.allOf(mathOpType.class)) {
            mathTypeSelector.getItems().add(type.toString());
        }
        mathTypeSelector.getSelectionModel().selectFirst();

        //when the dropdown is changed change the current function
        mathTypeSelector.valueProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue ov, String t, String t1) {
                for (mathOpType type: EnumSet.allOf(mathOpType.class)) {
                    if(type.toString()==t1){
                        currentMathOpType=type;
                        if(type==mathOpType.None){
                            mathematicalWave= new ArrayList<>();
                        }
                    }
                }

            }
        });

        HBox waveAWithLabel= new HBox();
        //make and fill the drop down
        ComboBox mathWaveSelectionA = new ComboBox();
        for (mathWaveSelection type: EnumSet.allOf(mathWaveSelection.class)) {
            mathWaveSelectionA.getItems().add(type.toString());
        }
        mathWaveSelectionA.getSelectionModel().selectFirst();

        //when the dropdown is changed change the current function
        mathWaveSelectionA.valueProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue ov, String t, String t1) {
                for (mathWaveSelection type: EnumSet.allOf(mathWaveSelection.class)) {
                    if(type.toString()==t1){
                        mathWaveA=type;
                    }
                }

            }
        });
        Label waveALabel = new Label("Wave A:");
        waveAWithLabel.getChildren().addAll(waveALabel,mathWaveSelectionA);

        HBox waveBWithLabel= new HBox();
        //make and fill the drop down
        ComboBox mathWaveSelectionB = new ComboBox();
        for (mathWaveSelection type: EnumSet.allOf(mathWaveSelection.class)) {
            mathWaveSelectionB.getItems().add(type.toString());
        }
        mathWaveSelectionB.getSelectionModel().selectFirst();

        //when the dropdown is changed change the current function
        mathWaveSelectionB.valueProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue ov, String t, String t1) {
                for (mathWaveSelection type: EnumSet.allOf(mathWaveSelection.class)) {
                    if(type.toString()==t1){
                        mathWaveB=type;
                    }
                }

            }
        });
        Label waveBLabel = new Label("Wave B:");
        waveBWithLabel.getChildren().addAll(waveBLabel,mathWaveSelectionB);


        VBox waveSel = new VBox(mathTypeSelector,waveAWithLabel,waveBWithLabel);
        return waveSel;
    }

    public Pane makeDisplayPane(){
        //make wave view check boxes
        CheckBox cbWave1 = new CheckBox("Waveform");
       // CheckBox cbWave2 = new CheckBox("wave 2");
      //  CheckBox cbWaveMath = new CheckBox("wave math");

        //set them all as on
        cbWave1.setSelected(true);
      //  cbWave2.setSelected(true);
       // cbWaveMath.setSelected(true);


        //add functionallity
        cbWave1.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                showWave1=newValue;
                displayAllWaves();
            }
        });


        CheckBox drawFromRisingEdgeCB = new CheckBox("Draw from rising edge");
        drawFromRisingEdgeCB.setSelected(drawFromRisingEdge);
        //  cbWave2.setSelected(true);
        // cbWaveMath.setSelected(true);


        //add functionallity
        drawFromRisingEdgeCB.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                drawFromRisingEdge=newValue;
                displayAllWaves();
            }
        });

        /*
        cbWave2.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                showWave2=newValue;
                displayAllWaves();
            }
        });
        cbWaveMath.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                showWaveMath=newValue;
                displayAllWaves();
            }
        });
*/
        ColorPicker wave1ColourPicker = new ColorPicker(wave1Colour);
        wave1ColourPicker.setOnAction(new EventHandler() {
            public void handle(Event t) {
                wave1Colour=wave1ColourPicker.getValue();
                displayAllWaves();
            }
        });
/*
        ColorPicker wave2ColourPicker = new ColorPicker(wave2Colour);
        wave2ColourPicker.setOnAction(new EventHandler() {
            public void handle(Event t) {
                wave2Colour=wave2ColourPicker.getValue();
                displayAllWaves();
            }
        });

        ColorPicker waveMathColourPicker = new ColorPicker(waveMathColour);
        waveMathColourPicker.setOnAction(new EventHandler() {
            public void handle(Event t) {
                waveMathColour=waveMathColourPicker.getValue();
                displayAllWaves();
            }
        });
*/

        HBox w1 = new HBox();
        w1.getChildren().addAll(cbWave1,wave1ColourPicker);
/*
        HBox w2 = new HBox();
        w2.getChildren().addAll(cbWave2,wave2ColourPicker);

        HBox wm = new HBox();
        wm.getChildren().addAll(cbWaveMath,waveMathColourPicker);
*/
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, waveThickness);

        Spinner waveThicknessSpinner = new Spinner(valueFactory);
        waveThicknessSpinner.valueProperty().addListener((obs, oldValue, newValue) -> waveThickness=(int)newValue);
        waveThicknessSpinner.valueProperty().addListener((obs, oldValue, newValue) -> displayAllWaves());

        Label l = new Label("Thickness");

        //add them to a vertical box to look nicer and save space
        //VBox cbVBox = new VBox(w1,w2,wm,waveThicknessSpinner);

        GridPane gp = new GridPane();
        gp.add(cbWave1,0,0);
        gp.add(wave1ColourPicker,1,0);
/*
        gp.add(cbWave2,0,1);
        gp.add(wave2ColourPicker,1,1);

        gp.add(cbWaveMath,0,2);
        gp.add(waveMathColourPicker,1,2);
*/
        gp.add(l,0,3);
        gp.add(waveThicknessSpinner,1,3);
        VBox V = new VBox(gp,drawFromRisingEdgeCB);
        return  V;
    }

    public void changeMultiplexerValue(MicroControllerSettings.multiplexerValues multiplexerValue){
        main.microControllerSettings.setMultiplexerValue(multiplexerValue);

        displayAllWaves();

    }


    public Pane makeDivisionsPane(){



        Button incVPerDiv = new Button("+ V/Div");
        Button decVPerDiv = new Button("- V/Div");
        voltagePerDivisionLabel = new Text("x V/Div");
        SecondsPerDivisionLabel = new Text("x S/Div");
        Button incSecondsPerDivisionButton = new Button("+ S/Div");
        Button decSecondsPerDivisionButton = new Button("- S/Div");

        incSecondsPerDivisionButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                secondsPerDivision *= 2;//increase divisions will make the wave bigger

                float DivPerSample = SecondsPerSample/secondsPerDivision;

                pixelsPerSample = DivPerSample*pixelsPerDivision;

                divisionsChanged();//then call this function to update any UI we have
                displayAllWaves();
            }
        });
        decSecondsPerDivisionButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                secondsPerDivision /= 2;//increase divisions will make the wave bigger

                float DivPerSample = SecondsPerSample/secondsPerDivision;

                pixelsPerSample = DivPerSample*pixelsPerDivision;

                divisionsChanged();//then call this function to update any UI we have
                displayAllWaves();
            }
        });

        //make the functions for the buttons
        incVPerDiv.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                VoltagePerDivision *= 2;//increase divisions will make the wave bigger
                divisionsChanged();//then call this function to update any UI we have
                displayAllWaves();
            }
        });
        decVPerDiv.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {

                VoltagePerDivision /= 2;//make the wave look smaller
                divisionsChanged();//then update UI ect
                displayAllWaves();
            }
        });

        HBox VoltagePerDivs = new HBox(incVPerDiv,decVPerDiv);
        HBox SecPerDivs = new HBox(incSecondsPerDivisionButton,decSecondsPerDivisionButton);
        //add inc and dec to vBox
        VBox incDecVBoc = new VBox(voltagePerDivisionLabel,VoltagePerDivs,SecondsPerDivisionLabel,SecPerDivs);
        divisionsChanged();
        return  incDecVBoc;
    }



    //This calls any other mathematical functions it needs

    // this function should return a ArrayList<Float> which is the sum of firstWave and secondWave
    // firstWave and secondWave are both  ArrayList<Float> but may have a different length
    // if they have a different length then we can only add up what we know, which is up to the length of the smaller array
    // eg [1,2,3,4] and [1,1,1] should give [2,3,4] and ignore the last value in the first array
    public static ArrayList<Float> mathematicalAdd(ArrayList<Float> firstWave, ArrayList<Float> secondWave) {
        ArrayList<Float> newWave = new ArrayList<>();
        int size = getSize(firstWave,secondWave);
        for (int i = 0; i < size; i++) {
            newWave.add(i, firstWave.get(i) + secondWave.get(i));
        }
        return newWave;
    }

    //similarly to the first one, the arrays may be different lengths, use up to length of smaller array
//should give out an arraylist<Float> of the firstWave divided by SecondWave, on the case that the
//denominator is 0, meaning x/0 is infinity, the output should be 0
//eg [2,4,8] as firstWave and  [1,2,0] as second wave should give [2,2,0]
    public static ArrayList<Float> mathematicalDivide(ArrayList<Float> firstWave, ArrayList<Float> secondWave) {
        ArrayList<Float> newWave = new ArrayList<>();
        int size = getSize(firstWave,secondWave);
        for (int i = 0; i < size; i++) {
            if (secondWave.get(i) == 0) {
                newWave.add(i, 0f);
            } else {
                newWave.add(i, firstWave.get(i) / secondWave.get(i));
            }
        }
        return newWave;
    }

    //similarly to the first one, the arrays may be different lengths, use up to length of smaller array
//should give out an arraylist<Float> of the firstWave multiplied by the SecondWave
//eg [2,4,8] as firstWave and  [1,2,0] as second wave should give [2,8,0]
    public static ArrayList<Float> mathematicalMultiply(ArrayList<Float> firstWave, ArrayList<Float> secondWave) {
        ArrayList<Float> newWave = new ArrayList<>();
        int size = getSize(firstWave,secondWave);
        for (int i = 0; i < size; i++) {
            newWave.add(i, firstWave.get(i) * secondWave.get(i));
        }
        return newWave;
    }

    //similarly to the first one, the arrays may be different lengths, use up to length of smaller array
//should give out an arraylist<Float> of the SecondWave subtracted from the FirstWave
//eg [2,4,8] as firstWave and  [1,2,0] as second wave should give [1,2,8]
    public static ArrayList<Float> mathematicalSubtract(ArrayList<Float> firstWave, ArrayList<Float> secondWave) {
        ArrayList<Float> newWave = new ArrayList<>();
        //int size = 0;
        //if (firstWave.size() < secondWave.size()) {
        //    size = firstWave.size();
        //} else {
        //    size = secondWave.size();
        //}
        int size = getSize(firstWave,secondWave);
        for (int i = 0; i < size; i++) {
            newWave.add(i, firstWave.get(i) - secondWave.get(i));
        }
        return newWave;
    }

    public static int getSize(ArrayList<Float> a, ArrayList<Float> b) {
        int sizeOfArrayList = 0;
        if (a.size() < b.size()) {
            sizeOfArrayList = a.size();
        } else {
            sizeOfArrayList = b.size();
        }
        return sizeOfArrayList;
    }

    //Challenge, to the FFT of the first wave, i dont know what the output should be for different values
    //so i cant give an example here
    public ArrayList<Float> mathematicalFFT(ArrayList<Float> wave){
        ArrayList<Float> newWave = new ArrayList<>();
        //note the line above is here so i can run the program without any errors, it is not meant to help
        return newWave;
    }

    public static float waveDataGetHighestValue(ArrayList<Float> wave){
         float highestWaveValue = Collections.max(wave);
        return highestWaveValue;

    }

    //this function should return a float value of then minimum value of the given array
    public static float waveDataGetLowestValue(ArrayList<Float> wave){
         float lowestWaveValue = Collections.min(wave);
         return lowestWaveValue;

    }

    //this function should return a float value peak to peak value, we can assume the wave is centered around 0
    public static float waveDataGetPeakToPeakValue(ArrayList<Float> wave){
        return Collections.max(wave)-Collections.min(wave);
    }
    //this function should return a float value peak to peak value, we can assume the wave is centered around 0
    public static float waveDataGetRMSValue(ArrayList<Float> wave){
        float peakToPeakValue = (Collections.max(wave)-Collections.min(wave))/2f;
        float rmsValue = peakToPeakValue/((float)(Math.sqrt(2)));
        return rmsValue;
    }

    //this function should return the peak to peak value of a wave

    //this will update and UI elements or logic once a user defined division is changed
    public void divisionsChanged(){
        voltagePerDivisionLabel.setText(getScienceNumber(VoltagePerDivision) + "V/Division");
        SecondsPerDivisionLabel.setText(getScienceNumber(secondsPerDivision)+"S/Div");
    }

    //This function will draw the background grid/squares where one square is one division
    public void drawGrid(){
        gc.setLineWidth(0.5f);//this is how thick the 'pen' will be, 1 pixel for now
        gc.setStroke(divisionsColour);
        //this for loop will start at (canvas.widthProperty().intValue()/2)%pixelsPerDivision
        //this means that one line will pass through the center of the screen, for 0 which we need
        //canvas width and height properties are the width and height of the display of the waves only
        //then we draw a line every pixelsPerDivision pixels, this is so one division is pixelsPerDivision pixels.
        //the one directly below is for the time domain
        for (int x = (canvas.widthProperty().intValue() / 2) % pixelsPerDivision; x < canvas.widthProperty().intValue(); x += pixelsPerDivision) {
            //we begin to draw a line
            gc.beginPath();
            //starting at the top of the page
            gc.moveTo(x, 0);
            //going to the bottom
            gc.lineTo(x, canvas.heightProperty().intValue());
            //then finish this line
            gc.stroke();
        }


        //the one directly below is for the voltage domain
        //this one it is much more important that a line goes through the center as this is where the 0 mark for the waves is
        for (int y = (canvas.heightProperty().intValue() / 2) % pixelsPerDivision; y < canvas.heightProperty().intValue(); y += pixelsPerDivision) {
            gc.beginPath();
            gc.moveTo(0, y);
            gc.lineTo(canvas.widthProperty().intValue(), y);
            gc.stroke();
        }
        gc.setStroke(voltageTextColour);
        int numberOfPositiveDivisions = (canvas.heightProperty().intValue() / 2) / pixelsPerDivision;




        for (int i = 0; i < numberOfPositiveDivisions; i+=2) {
            gc.strokeText("+" + getScienceNumber(i * VoltagePerDivision) + "V", 5, canvas.heightProperty().intValue() / 2 - (pixelsPerDivision * i)+5);
           // gc.strokeText("+" + i / voltageMultiplier + "V", canvas.widthProperty().intValue() - 25, canvas.heightProperty().intValue() / 2 - (pixelsPerDivision * i));
            if (i != 0) {
                gc.strokeText("-" + getScienceNumber(i * VoltagePerDivision) + "V", 5, canvas.heightProperty().intValue() / 2 + (pixelsPerDivision * i)+5);
              //  gc.strokeText("-" + i / voltageMultiplier + "V", canvas.widthProperty().intValue() - 25, canvas.heightProperty().intValue() / 2 + (pixelsPerDivision * i));
            }
        }
        gc.rotate(-90);

        for (int i=4; i<canvasSizeXY/pixelsPerDivision;i+=4){
            gc.strokeText(getScienceNumber(i*secondsPerDivision)+"ms", -(canvasSizeXY-10),(pixelsPerDivision * i)+5);
        }
        gc.rotate(90);
        //the following will draw a center line over the 0V axis part, but with a thicker pen so we can see 0 clearer
        gc.beginPath();
        gc.setLineWidth(3);//makes the line thicker
        gc.moveTo(0, canvas.heightProperty().intValue() / 2);
        gc.lineTo(canvas.widthProperty().intValue(), canvas.heightProperty().intValue() / 2);
        gc.stroke();

    }

    static public String getScienceNumber(float number){


        String fix = "?";

        float giga = 1000000000f;
        float mega = 1000000f;
        float kilo = 1000f;

        float milli = 0.001f;
        float micro = 0.000001f;
        float nano = 0.000000001f;

        int sign =1;

        if(number<0){
            sign=-1;
        }

        float ABSNumber = Math.abs(number);

        if(ABSNumber<1f){
            if(ABSNumber<milli){
                if(ABSNumber<micro){
                    if(ABSNumber<nano){
                        return ABSNumber*sign+"";
                    }else{

                        return String.format("%.2f", Math.round((ABSNumber/nano)*100f)/100f*sign)+"n";
                    }
                }else{
                    return  String.format("%.2f",Math.round((ABSNumber/micro)*100f)/100f*sign)+"u";
                }

            }else{
                return  String.format("%.2f",Math.round((ABSNumber/milli)*100f)/100f*sign)+"m";
            }

        }else if(ABSNumber>kilo){
            if(ABSNumber > mega){
                if(ABSNumber>giga){
                    return ABSNumber*sign+"";
                }else{

                    return  String.format("%.2f",Math.round((ABSNumber/mega)*100f)/100f*sign)+"M";
                }
            }else{
                return  String.format("%.2f",Math.round((ABSNumber/kilo)*100f)/100f*sign)+"K";
            }
        }else{
            return String.format("%.2f",ABSNumber*sign);
        }

    }
    //this function will be used to draw the two waves and any mathematical waves
    public void displayAllWaves(){
       // System.out.println("showing waves:"+wave1.size());

        //we start by clearing anything we have drawn previously
        gc.clearRect(0, 0, canvas.widthProperty().intValue(), canvas.heightProperty().intValue());
        gc.setFill(backgroundColour);
        gc.fillRect(0, 0, canvas.widthProperty().intValue(), canvas.heightProperty().intValue());
        //then we draw the grid mentioned previously
        drawGrid();

        drawTriggerVoltage();

        drawCursors();

        if(showWave1) {
            //we then start drawing the first wave in red
            displayWave(wave1, wave1Colour);
        }

        if(showWaveMath){

        }

    }

    public void drawCursors(){
        if(CursorType==cursorTypeSelection.None){
            return;
        }
        if(CursorType==cursorTypeSelection.SingleValue){
            int CenterYPixel = canvas.heightProperty().intValue()/2;//this is int division

            System.out.println(cursor1Time/secondsPerDivision*pixelsPerDivision/1000);
            gc.beginPath();
            gc.setStroke(Cursor1Colour);
            gc.setLineWidth(waveThickness);
            gc.moveTo(cursor1Time/secondsPerDivision*pixelsPerDivision/1000,0);
            gc.lineTo(cursor1Time/secondsPerDivision*pixelsPerDivision/1000, canvasSizeXY);
            gc.stroke();

            float evaluatedVoltage  = getVoltageAtTime((cursor1Time+(findRisingEdgeIndex()*SecondsPerSample*1000f))/1000f);



            gc.moveTo(0, CenterYPixel - (evaluatedVoltage / VoltagePerDivision) * pixelsPerDivision);

            gc.lineTo(canvasSizeXY, CenterYPixel - (evaluatedVoltage / VoltagePerDivision) * pixelsPerDivision);
            gc.stroke();


            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Cursor1Label.setText("Cursor 1: Voltage "+getScienceNumber(evaluatedVoltage)+"V");
                }
            });


        }
        if(CursorType==cursorTypeSelection.DoubleValue) {
            int CenterYPixel = canvas.heightProperty().intValue()/2;//this is int division

            System.out.println(cursor1Time/secondsPerDivision*pixelsPerDivision/1000);
            gc.beginPath();
            gc.setStroke(Cursor1Colour);
            gc.setLineWidth(waveThickness);
            gc.moveTo(cursor1Time/secondsPerDivision*pixelsPerDivision/1000,0);
            gc.lineTo(cursor1Time/secondsPerDivision*pixelsPerDivision/1000, canvasSizeXY);
            gc.stroke();

            float evaluatedVoltage = getVoltageAtTime((cursor1Time+(findRisingEdgeIndex()*SecondsPerSample*1000f))/1000f);

            gc.moveTo(0, CenterYPixel - (evaluatedVoltage / VoltagePerDivision) * pixelsPerDivision);

            gc.lineTo(canvasSizeXY, CenterYPixel - (evaluatedVoltage / VoltagePerDivision) * pixelsPerDivision);
            gc.stroke();


            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Cursor1Label.setText("Cursor 1: Voltage "+getScienceNumber(evaluatedVoltage)+"V");
                }
            });
            //now 2

            System.out.println(cursor2Time / secondsPerDivision * pixelsPerDivision / 1000);
            gc.beginPath();
            gc.setStroke(Cursor2Colour);
            gc.setLineWidth(waveThickness);
            gc.moveTo(cursor2Time / secondsPerDivision * pixelsPerDivision / 1000, 0);
            gc.lineTo(cursor2Time / secondsPerDivision * pixelsPerDivision / 1000, canvasSizeXY);
            gc.stroke();

            float evaluatedVoltage2 =  getVoltageAtTime((cursor2Time+(findRisingEdgeIndex()*SecondsPerSample*1000f))/1000f);

            gc.moveTo(0, CenterYPixel - (evaluatedVoltage2 / VoltagePerDivision) * pixelsPerDivision);

            gc.lineTo(canvasSizeXY, CenterYPixel - (evaluatedVoltage2 / VoltagePerDivision) * pixelsPerDivision);
            gc.stroke();

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Cursor2Label.setText("Cursor 2: Voltage "+getScienceNumber(evaluatedVoltage2)+"V");
                    CursorDiffLabel.setText("Cursor Difference: Voltage "+getScienceNumber(evaluatedVoltage-evaluatedVoltage2)+"V");
                }
            });

        }

    }
    public float getVoltageAtTime(float time){
        try {
            return wave1.get((int) (time / SecondsPerSample));
        }catch(Exception e){
            return 0;
        }
    }

    public void drawTriggerVoltage(){
        int CenterYPixel = canvas.heightProperty().intValue()/2;//this is int division
        gc.beginPath();
        gc.setStroke(Color.RED);
        gc.setLineWidth(waveThickness);
        gc.moveTo(0, CenterYPixel - (triggerVoltage / VoltagePerDivision) * pixelsPerDivision);

        gc.lineTo(canvasSizeXY, CenterYPixel - (triggerVoltage / VoltagePerDivision) * pixelsPerDivision);

        gc.stroke();

    }

    //this will be used to display a single wave with a given colour
    public void displayWave(ArrayList<Float> wave,Color color){

        //find the center pixel's Y value so we know where to start drawing the wave from
        //this will ensure the wave is drawn where the distance to the top and the bottom of the screen is equal
        int CenterYPixel = canvas.heightProperty().intValue()/2;//this is int division
        gc.beginPath();
        gc.setStroke(color);
        gc.setLineWidth(waveThickness);



        if(!drawFromRisingEdge) {
            gc.moveTo(0, CenterYPixel + Math.round(-wave.get(0) / VoltagePerDivision * pixelsPerDivision));
            for (int i = 0; i < wave.size(); i++) {
                //for the x value of the lineTo function, we use canvas.widthProperty().intValue()/wave1.size()
                //to convert the wave index number to pixels such that it fills the entire width of the canvas
                //for the y value, we start from the center CenterYPixel, and then add the value of the wave
                //(0,0) of the canvas is top left, so a high y value is lower, so we use the - to flip the wave
                //we then scale this by the voltageMultiplier for our user defined divisions
                //and lastly scale by pixelsPerDivision so convert the voltage into pixel values (1V is pixelsPerDivision pixels from the center line)
                float xPos = (int) (pixelsPerSample * i);
                // System.out.println("XVal:"+xPos+" pix per sample "+pixelsPerSample);
                // gc.lineTo(i * canvas.widthProperty().intValue() / wave.size(), CenterYPixel + Math.round(-wave.get(i) * voltageMultiplier * pixelsPerDivision));
                gc.lineTo(xPos, CenterYPixel + Math.round(-wave.get(i) / VoltagePerDivision * pixelsPerDivision));
            }
        }else{
            int risingIndex = findRisingEdgeIndex();
            gc.moveTo(0, CenterYPixel + Math.round(-wave.get(risingIndex) / VoltagePerDivision * pixelsPerDivision));
            for (int i = risingIndex; i < wave.size(); i++) {

                float xPos = (int) (pixelsPerSample * (i-risingIndex));

                gc.lineTo(xPos, CenterYPixel + Math.round(-wave.get(i) / VoltagePerDivision * pixelsPerDivision));
            }

        }
        gc.stroke();
    }


    public int findRisingEdgeIndex(){
        if(drawFromRisingEdge) {
            ArrayList<Float> newWave = new ArrayList<>();

            for (int i = 0; i < wave1.size() - 4; i++) {
                newWave.add((wave1.get(i) + wave1.get(i + 1) + wave1.get(i + 2) + wave1.get(i + 4)) / 4f);
            }


            boolean hasBeenBelow = false;
            int index = 0;
            for (Float f : newWave) {

                if (hasBeenBelow) {
                    if (f >= triggerVoltage) {
                        return index;
                    }
                }

                if (f < triggerVoltage) {
                    hasBeenBelow = true;
                }

                index++;
            }
        }

        return 0;
    }


    //HandleWaveData will take in an array list of array lists
    //the best way to think about this is the inner arraylist is all of the values for one channel
    //and then tne outer arraylist is an arraylist of channels
    /*public void handleWaveData(ArrayList<Float> channel1,ArrayList<Float> channel2){

        ArrayList<Float> scaleAndShifted = new ArrayList<>();
        for (Float f:channel1) {
            scaleAndShifted.add(f/4096f*3.3f-1.5f);
        }

        wave1=scaleAndShifted;
        wave2=scaleAndShifted;
       // findFrequency(wave1);
        displayAllWaves();

    }*/
    public void opened(){
        prescalarValueSelector.getSelectionModel().select(main.microControllerSettings.getADCPrescalarValue().ordinal());
        multiplexerValueSelector.getSelectionModel().select(main.microControllerSettings.getMultiplexerValue().ordinal());

    }

}
