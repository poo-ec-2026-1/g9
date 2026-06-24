package com.telemetria.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class Main extends Application {

    @Override   
public void start(Stage stage) throws Exception 
{
    try {
        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Tela de Login");
        stage.show();
    } catch (Exception e) {
        System.out.println("--- O JAVAFX TRAVOU PELO SEGUINTE MOTIVO: ---");
        e.printStackTrace();
        System.out.println("--------------------------------------------");
    }
}
}