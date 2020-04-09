package sample;

import java.util.HashMap;

public class MicroControllerSettings {

    Main main;


    public enum ADCPrescalarValues {
        Prescale1,
        Prescale2,
        Prescale4,
        Prescale8,
        Prescale16,
        Prescale32,
        Prescale64,
        Prescale128,
        Prescale256,
    }

    enum multiplexerValues {
        max0p3V,
        max1V,
        max2V,
        max3V,
        max10V,
        max20V,
        max30V,
        max40V,
    }

    multiplexerValues currentMultiplexerValue = multiplexerValues.max3V;

    HashMap<multiplexerValues,Float> multiplexerValuesToGain = new HashMap<multiplexerValues, Float>();

    ADCPrescalarValues currentADCPrescalarValue;


    public MicroControllerSettings(Main tempMain){
        main=tempMain;



        multiplexerValuesToGain.put(multiplexerValues.max0p3V,0.3f/2.79f);//for now these are just test values
        multiplexerValuesToGain.put(multiplexerValues.max1V,1f/2.5f);
        multiplexerValuesToGain.put(multiplexerValues.max2V,2/2.74f);
        multiplexerValuesToGain.put(multiplexerValues.max3V,3.3f/3.3f);
        multiplexerValuesToGain.put(multiplexerValues.max10V,10f/2.66f);
        multiplexerValuesToGain.put(multiplexerValues.max20V,20f/2.92f);
        multiplexerValuesToGain.put(multiplexerValues.max30V,30f/3.4f);
        multiplexerValuesToGain.put(multiplexerValues.max40V,40f/3.17f);

        reset();

    }
    public void reset(){
        setMultiplexerValue(multiplexerValues.max3V);
        setADCPrescalarValue(ADCPrescalarValues.Prescale32);
    }

    public void setMultiplexerValue(multiplexerValues multiplexerValue){
        currentMultiplexerValue= multiplexerValue;


        if(multiplexerValue==multiplexerValues.max0p3V) {
            main.dataReader.write(CMD.SetMUX00);
        }else if(multiplexerValue==multiplexerValues.max1V){
            main.dataReader.write(CMD.SetMUX01);
        }else if(multiplexerValue==multiplexerValues.max2V){
            main.dataReader.write(CMD.SetMUX10);
        }else if(multiplexerValue==multiplexerValues.max3V){
            main.dataReader.write(CMD.SetMUX11);
        }else if(multiplexerValue==multiplexerValues.max10V) {
            main.dataReader.write(CMD.SetMUX00);
        }else if(multiplexerValue==multiplexerValues.max20V){
            main.dataReader.write(CMD.SetMUX01);
        }else if(multiplexerValue==multiplexerValues.max30V){
            main.dataReader.write(CMD.SetMUX10);
        }else if(multiplexerValue==multiplexerValues.max40V){
            main.dataReader.write(CMD.SetMUX11);
        }
    }
    public multiplexerValues getMultiplexerValue(){
        return currentMultiplexerValue;
    }

    public float convertFrom3VtoTrueValue(float voltage){
        return voltage*multiplexerValuesToGain.get(currentMultiplexerValue);
    }

    public void setADCPrescalarValue(ADCPrescalarValues prescalarValue){
        currentADCPrescalarValue=prescalarValue;

        if(prescalarValue==ADCPrescalarValues.Prescale1){
            main.dataReader.write(CMD.PreScalar1);
        }else if(prescalarValue==ADCPrescalarValues.Prescale2){
            main.dataReader.write(CMD.PreScalar2);
        }else if(prescalarValue==ADCPrescalarValues.Prescale4){
            main.dataReader.write(CMD.PreScalar4);
        }else if(prescalarValue==ADCPrescalarValues.Prescale8){
            main.dataReader.write(CMD.PreScalar8);
        }else if(prescalarValue==ADCPrescalarValues.Prescale16){
            main.dataReader.write(CMD.PreScalar16);
        }else if(prescalarValue==ADCPrescalarValues.Prescale32){
            main.dataReader.write(CMD.PreScalar32);
        }else if(prescalarValue==ADCPrescalarValues.Prescale64){
            main.dataReader.write(CMD.PreScalar64);
        }else if(prescalarValue==ADCPrescalarValues.Prescale128){
            main.dataReader.write(CMD.PreScalar128);
        }else if(prescalarValue==ADCPrescalarValues.Prescale256){
            main.dataReader.write(CMD.PreScalar256);
        }

    }
    public ADCPrescalarValues getADCPrescalarValue(){
        return currentADCPrescalarValue;
    }
}
