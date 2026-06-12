package com.telemetria.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class SenhaAlterarController {

    @FXML
    private PasswordField txtNovaSenha;

    @FXML
    private Button btnConfirmarSenha;

    @FXML
    private void confirmarAlteracaoSenha(ActionEvent event) {
        String novaSenha = txtNovaSenha.getText();

        if (novaSenha == null || novaSenha.trim().isEmpty()) {
            exibirAlerta("Aviso", "Por favor, digite uma nova senha.");
            return;
        }

        
        RegistroLoginController.setSenhaSalva(novaSenha);

        exibirAlerta("Sucesso", "Senha alterada com sucesso!");

        
        Stage stage = (Stage) btnConfirmarSenha.getScene().getWindow();
        stage.close();
    }

    private void exibirAlerta(String titulo, String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}