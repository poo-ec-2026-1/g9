package com.telemetria.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CadastroVeiculoController {

    @FXML
    private TextField txtIdentificador;

    @FXML
    private TextField txtModelo;

    @FXML
    private TextField txtSensor;

    @FXML
    private Button btnSalvar;

    @FXML
    private void salvarVeiculo(ActionEvent event) {
        String identificador = txtIdentificador.getText();
        String modelo = txtModelo.getText();
        String sensor = txtSensor.getText();

        if (identificador.trim().isEmpty() || modelo.trim().isEmpty() || sensor.trim().isEmpty()) {
            exibirAlerta(Alert.AlertType.WARNING, "Campos Incompletos", "Por favor, preencha todos os campos.");
            return;
        }

        // Formata os dados digitados em uma única String para salvar na lista
        String dadosVeiculo = "Placa: " + identificador.toUpperCase() + 
                              " | Modelo: " + modelo + 
                              " | Sensor: " + sensor.toUpperCase();

        // ADICIONA NA LISTA GLOBAL
        RegistroLoginController.adicionarVeiculo(dadosVeiculo);

        exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Veículo cadastrado e adicionado à frota com sucesso!");

        // Fecha a janela de cadastro
        Stage stage = (Stage) btnSalvar.getScene().getWindow();
        stage.close();
    }

    private void exibirAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}