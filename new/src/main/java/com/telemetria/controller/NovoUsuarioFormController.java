package com.telemetria.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NovoUsuarioFormController {

    @FXML private TextField txtNome;
    @FXML private TextField txtLogin;
    @FXML private PasswordField txtSenha;
    @FXML private ComboBox<String> comboPerfil;
    @FXML private Button btnSalvar;

    @FXML
    public void initialize() {
        comboPerfil.setItems(FXCollections.observableArrayList("Cliente", "Operador", "Administrador"));
        comboPerfil.setValue("Cliente");
    }

    @FXML
    private void salvarUsuario(ActionEvent event) {
        String nome = txtNome.getText();
        String login = txtLogin.getText();
        String senha = txtSenha.getText();
        String perfil = comboPerfil.getValue();

        if (nome.trim().isEmpty() || login.trim().isEmpty() || senha.trim().isEmpty()) {
            Alert alerta = new Alert(Alert.AlertType.WARNING, "Por favor, preencha todos os campos do formulário.");
            alerta.showAndWait();
            return;
        }

        // Enviando a senha do formulário para o método atualizado
        RegistroLoginController.adicionarUsuarioLista(login, senha, nome, perfil);

        Alert sucesso = new Alert(Alert.AlertType.INFORMATION, "Usuário " + login + " criado e registrado!");
        sucesso.showAndWait();

        Stage stage = (Stage) btnSalvar.getScene().getWindow();
        stage.close();
    }
}