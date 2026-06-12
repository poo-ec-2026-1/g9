package com.telemetria.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NomeAlterarController {

    @FXML
    private TextField txtNovoNome;

    @FXML
    private Button btnConfirmar;

    @FXML
    private void confirmarAlteracao(ActionEvent event) {
        String novoNome = txtNovoNome.getText();

        if (novoNome == null || novoNome.trim().isEmpty()) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Aviso");
            alerta.setHeaderText(null);
            alerta.setContentText("Por favor, digite um nome válido.");
            alerta.showAndWait();
            return;
        }

        // Atualiza o nome estático lá na classe de Registro
        RegistroLoginController.setNomeSalvo(novoNome);

        Alert alertaSucesso = new Alert(Alert.AlertType.INFORMATION);
        alertaSucesso.setTitle("Sucesso");
        alertaSucesso.setHeaderText(null);
        alertaSucesso.setContentText("Nome alterado com sucesso! As alterações serão visíveis ao reabrir o Menu.");
        alertaSucesso.showAndWait();

        // Fecha a janelinha de alteração de dados automaticamente
        Stage stage = (Stage) btnConfirmar.getScene().getWindow();
        stage.close();
    }
}