package sample;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;

public class DataReader {


    enum DataReciever {
        Oscilloscope,
        DMM,
        FunctionGen
    }

    DataReciever dataReciever = DataReciever.Oscilloscope;


    //reference to main
    Main mainClass;
    //the stream of characters that the mictrocontroller gives
    InputStream in;
    //The port on which we communicate with the mictrocontroller
    SerialPort comPort;
    //keep track of all of the characters we read from the microcontroller
    String currentStringRead="";


    boolean isPortOpen=false;

    String prevInterruptValue = "";

    public DataReader(Main mainClassTemp){
        mainClass=mainClassTemp;





//setUpReadData add back in

    }
    public SerialPort[] getCommPorts(){
        return SerialPort.getCommPorts();

    }
    public boolean trySetUpCommPort(SerialPort port){

        try {
            comPort = port;
            comPort.setBaudRate(256000);
            comPort.openPort();
            comPort.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() {
                    return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
                }
                @Override
                public void serialEvent(SerialPortEvent event) {
                    byte[] newData = event.getReceivedData();
                    String temp = "";
                    for (int i = 0; i < newData.length; ++i) {
                        temp += (char) newData[i];
                    }
                    System.out.println(temp);
                    handleCommInterruptData(temp);
                }
            });
            return true;
        }catch (Exception e){
            System.out.println("Exception A");
            return false;
        }
    }

    public boolean isCommPortFound(){

        return comPort!=null;
    }

    //this will get everything ready for when we click start, if the microcontroller isnt connected then SerialPort.getCommPorts().length is 0
    //i just added the if statement for this so the code can be run without the board connected if we work on the UI or something
    // everything inside the for loop was on the arduino's website, or something. So not sure what it does but it works

    public void write(char data){
        write(String.valueOf(data));
    }

    public void  write(String dataToWrite){
        // while(in.hasNext()) {
        //     in.next();
        //  }
        byte[] b = dataToWrite.getBytes();
        //comPort.writeBytes(b,b.length);//hhahahahahahhaa
        comPort.writeBytes(b,b.length);
        System.out.println("write "+dataToWrite);
    }

    //to close the port properly, used when we exit the program
    public void closeDownReadData(){
        if(comPort!=null) {
            comPort.closePort();

        }   }

    //this will get the value of the pin from the current string
    public float getValueFromCurrentString(String currentString){

        return Float.parseFloat(currentString.split(":")[0]);

    }

    public void handleCommInterruptData(String newData){
        for (char c:newData.toCharArray()) {
            if(c!='\n'&&c!='\r'){
                prevInterruptValue+=c;
            }else if(c=='\r'){
                sendDataValue(prevInterruptValue);
                prevInterruptValue="";
            }
        }
    }

    public void sendDataValue(String value){




        if(dataReciever==DataReciever.Oscilloscope){
            mainClass.oscilloscope.recievedNewDataValue(value);
        }else if (dataReciever==DataReciever.DMM){

            mainClass.dmm.recievedNewDataValue(value);

        }

    }

    public ArrayList<Float> makeDataReading() {
        write("a");
        return  null;
        /*
        boolean isReading = true;
        ArrayList<Float> values = new ArrayList<Float>();
        currentStringRead = "";
        try {
            while (isReading){
                //so j is the index of each character, the characters are '-','0','.','3' for -0.3 so we need to combine them
                for (int j = 0; j < in.available() - 1; ++j) {
                    //tempChar will be the character we read this turn
                    char tempChar = (char) in.read();
                    //if the character is valid eg '.-:0123456789' then we add it to the currentStringRead string
                    // if(Character.isDigit(tempChar)||tempChar=='.'||tempChar=='-'||tempChar==':') {
                    if (tempChar != '\n' && tempChar != '\r') {//these two characters seperate lines sent by the board, each line is a new value
                        //System.out.println(Character.getName(tempChar));
                        currentStringRead += Character.toString(tempChar);
                        //System.out.println(currentStringRead);
                        //inCharArray.add(tempChar);
                    } else if (tempChar=='f'){
                        isReading=false;
                        currentStringRead="";
                    }
                    else if(currentStringRead!=""){//when we detect one of the line characters we know we have read an entire line.
                        //if(tempChar=='\n'||tempChar=='\r'){//if the character is not valid, then we have detected a special character that the STM board uses
                        //this special character means the end of a print statement, hence we can take advantage of this
                        //and when we see it we know we have read a 'block' of data from the board
                        //System.out.println("here1:"+currentStringRead);
                        //now we will try and separate the channel and value of the '-0.3:1' string
                        float value = Float.parseFloat(currentStringRead);
                        // System.out.println("here2:"+currentStringRead);
                        values.add(value);
                        //then we can reset the currentStringRead string so we can start working on the next block
                        currentStringRead = "";


                        //dont exactly know why ive repeated it but im scared to remove this
                        currentStringRead = "";
                    }
                }
                //also dont know why we have this here, once again it will probably work without it
                //but it was in the other code so ill keep it here
                in.close();
            }

        }catch(Exception e){
            System.out.println("here3");
        }

        for (float f: values             ) {
            // f=(f/4096)*3.3f;
            System.out.println(":::"+f);
        }
        return values;

         */
    }

    //this will get the channel that a value is sent over
    public int getChannelFromCurrentString(String currentString){
        return Integer.parseInt(currentString.split(":")[1]);
    }


    public ArrayList<ArrayList<Float>> readFromFile(){

        ArrayList<String> lines = new ArrayList<>();

        String fileName = "C:/Users/Will/Desktop/Kinscope/Data.txt";
        File file = new File(fileName);
        FileReader fr = null;
        try{
            fr = new FileReader(file);
        }catch (Exception e ){

        }
        BufferedReader br = new BufferedReader(fr);
        String line="";
        try{
            line=br.readLine();
        }catch (Exception e){
        }
        while(line != null){
            try{
                line=br.readLine();
            }catch (Exception e){


            }
            //process the line
            lines.add(line);
            // System.out.println(line);
        }
        try {
            fr.close();
        }catch (Exception e){

            System.out.println("failed to close");
        }

        return linesToArrays(lines);

//        return lines;
    }


    public ArrayList<ArrayList<Float>> linesToArrays(ArrayList<String> lines){

        ArrayList<ArrayList<Float>> channels = new ArrayList<ArrayList<Float>>();

        //We have two arraylists one for each channel, this will store the voltages that we find
        ArrayList<Float> newReadValueChannelOne = new ArrayList<>();
        ArrayList<Float> newReadValueChannelTwo = new ArrayList<>();

        for (String s : lines) {
            if(s!=null&&s!=""){
                //System.out.println("now Trying "+ s );
                try{
                    float value = getValueFromCurrentString(s );//1024f*3.3f;
                    //int channel = getChannelFromCurrentString(s );
                    newReadValueChannelOne.add(value);
                    // if(channel == 1 ){
                    //     newReadValueChannelOne.add(value);

                    //}else{

                    //newReadValueChannelTwo.add(value);
                    //}
                }catch (Exception e){


                }            }
        }


        channels.add(newReadValueChannelOne);
        channels.add(newReadValueChannelTwo);

        //check if anything needs to be sent
        // String string = "sMP1";
        //byte[] b = string.getBytes();
        //comPort.writeBytes(b,b.length);

        //and return what we have found this time
        return channels;

    }
    //So the way im sending data here is to send 'value:channel' eg '-0.3:2' is value -0.3, over channel 2
    //this is needed because if the microcontroller just sends '-0.3' we dont know what channel this is over
    //the channel may not be needed for a DMM, but we can treat it as channel 1 because the code might not work, but we'll see
    //this does mean in the STM code i have Serial.println(String("")+value+(":")+channel); meaning the channel has to be defined by the board
    public ArrayList<ArrayList<Float>> readData(){

        //now this is the array list of array lists
        //the inner one is the values of a single channel
        //the outer one in an array of channels
        ArrayList<ArrayList<Float>> channels = new ArrayList<ArrayList<Float>>();

        //We have two arraylists one for each channel, this will store the voltages that we find
        ArrayList<Float> newReadValueChannelOne = new ArrayList<>();
        ArrayList<Float> newReadValueChannelTwo = new ArrayList<>();

        if(!isPortOpen){
            newReadValueChannelOne.add(0f);
            newReadValueChannelTwo.add(0f);
            newReadValueChannelOne.add(0f);
            newReadValueChannelTwo.add(0f);
            //then we add the two  channels
            channels.add(newReadValueChannelOne);
            channels.add(newReadValueChannelTwo);

            //check if anything needs to be sent
            // String string = "sMP1";
            //byte[] b = string.getBytes();
            //comPort.writeBytes(b,b.length);

            //and return what we have found this time
            return channels;
        }

        //the try is here in case anything goes wrong the program wont crash, hopefully
        try
        {
            //so j is the index of each character, the characters are '-','0','.','3' for -0.3 so we need to combine them
            for (int j = 0; j < in.available()-1; ++j) {
                //tempChar will be the character we read this turn
                char tempChar = (char) in.read();
                //if the character is valid eg '.-:0123456789' then we add it to the currentStringRead string
                // if(Character.isDigit(tempChar)||tempChar=='.'||tempChar=='-'||tempChar==':') {
                if(tempChar!='\n'&&tempChar!='\r') {//these two characters seperate lines sent by the board, each line is a new value
                    //System.out.println(Character.getName(tempChar));
                    currentStringRead+=Character.toString(tempChar);
                    //inCharArray.add(tempChar);
                }else{//when we detect one of the line characters we know we have read an entire line.
                    //if(tempChar=='\n'||tempChar=='\r'){//if the character is not valid, then we have detected a special character that the STM board uses
                    //this special character means the end of a print statement, hence we can take advantage of this
                    //and when we see it we know we have read a 'block' of data from the board
                    try {
                        //now we will try and separate the channel and value of the '-0.3:1' string
                        float value = getValueFromCurrentString(currentStringRead);
                        int channel = getChannelFromCurrentString(currentStringRead);

                        //then we add the value to the correct wave array
                        if(channel==1){
                            newReadValueChannelOne.add(value);
                        }else if (channel==2){
                            newReadValueChannelTwo.add(value);
                        }
                        //then we can reset the currentStringRead string so we can start working on the next block
                        currentStringRead ="";

                    } catch (Exception e) {

                    }
                    //dont exactly know why ive repeated it but im scared to remove this
                    currentStringRead ="";
                }
            }
            //also dont know why we have this here, once again it will probably work without it
            //but it was in the other code so ill keep it here
            in.close();
        } catch (Exception e) { e.printStackTrace(); }//this is the exception in case anything goes wrong





        //then we add the two  channels
        channels.add(newReadValueChannelOne);
        channels.add(newReadValueChannelTwo);

        //check if anything needs to be sent
        // String string = "sMP1";
        //byte[] b = string.getBytes();
        //comPort.writeBytes(b,b.length);

        //and return what we have found this time
        return channels;
    }
}
