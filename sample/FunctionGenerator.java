package sample;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class FunctionGenerator extends BorderPane {
    Main tempMain;
     public FunctionGenerator(Main tempMainy){

        tempMain=tempMainy;
        Button Tri = new Button("Triangle Wave");
        Button Sin = new Button("Sine Wave");
        Button Sqr  = new Button("Square Wave");

        Button stop = new Button("Stop Function Generator");

        HBox DMMHBox = new HBox(Tri,Sin,Sqr,stop);



        this.setTop(DMMHBox);


         Tri.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                tempMain.dataReader.write(CMD.FunGenTriangle);
            }
        });

         Sin.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                tempMain.dataReader.write(CMD.FunGenSin);
            }
        });

         Sqr.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                tempMain.dataReader.write(CMD.FunGenSquare);
            }
        });
         stop.setOnAction(new EventHandler<ActionEvent>() {
             @Override public void handle(ActionEvent e) {
                 tempMain.dataReader.write(CMD.FunGenStop);
             }
         });



    }
}
