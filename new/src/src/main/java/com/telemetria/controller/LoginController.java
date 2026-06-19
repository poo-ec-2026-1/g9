package com.telemetria.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usuarioBotao;

    @FXML
    private PasswordField senhaBotao;

    @FXML
    private Button entrarBotao;

    public static String cargoLogado;
    java.util.List<String> todosUsuarios = RegistroLoginController.getListaUsuarios();

    @FXML
    private void entrar(ActionEvent event) throws Exception {
        String usuarioDigitado = usuarioBotao.getText();
        String senhaDigitada = senhaBotao.getText();

        if (usuarioDigitado.trim().isEmpty() || senhaDigitada.trim().isEmpty()) {
            exibirErro("Campos Vazios", "Por favor, preencha o usuário e a senha.");
            return;
        }

        if (usuarioDigitado.equals("adm") && senhaDigitada.equals("123")) {
            cargoLogado = "administrador";
            RegistroLoginController.setUsuarioSalvo("adm"); 
            RegistroLoginController.setNomeSalvo("Administrador Geral");
            irParaMenu(event);
            return;
        } 
        
        if (usuarioDigitado.equals("adm1") && senhaDigitada.equals("1234")) {
            cargoLogado = "operador";
            RegistroLoginController.setUsuarioSalvo("adm1"); 
            RegistroLoginController.setNomeSalvo("Operador Padrão");
            irParaMenu(event);
            return;
        }

        java.util.List<String> todosUsuarios = RegistroLoginController.getListaUsuarios();
        
        

        for (String linhaUsuario : todosUsuarios) {
            String loginRegistrado = extrairValor(linhaUsuario, "Login:");
            String senhaRegistrada = extrairValor(linhaUsuario, "Senha:");
            String nomeRegistrado = extrairValor(linhaUsuario, "Nome:");
            String cargoRegistrado = extrairValor(linhaUsuario, "Cargo:");
            
            if (usuarioDigitado.equals(loginRegistrado)) {
                
                if (senhaDigitada.equals(senhaRegistrada)) {
                    cargoLogado = cargoRegistrado.toLowerCase();
                    RegistroLoginController.setNomeSalvo(nomeRegistrado);
                    
                    irParaMenu(event);
                    return;
                }
            }
        }

        exibirErro("Falha na Autenticação", "Usuário ou senha incorretos!");
    }
    
    @FXML
private void abrirCadastro(ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("registrologin.fxml"));
        Parent root = loader.load();
        
        Stage stage = new Stage();
        stage.setTitle("Cadastro de Usuário");
        stage.setScene(new Scene(root));
        stage.show();
    } catch (Exception e) {
        e.printStackTrace();
        exibirErro("Erro ao Abrir", "Não foi possível abrir a tela de cadastro.");
    }
}

    private String extrairValor(String linha, String chave) {
        try {
            String[] partes = linha.split("\\|");
            for (String parte : partes) {
                if (parte.trim().startsWith(chave)) {
                    return parte.replace(chave, "").trim();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private void irParaMenu(ActionEvent event) {
        try {
            String arquivoFXML = "Menu.fxml";
            String tituloJanela = "Menu Principal";

            if (cargoLogado != null) {
                switch (cargoLogado.toLowerCase().trim()) {
                    case "administrador" -> {
                        arquivoFXML = "TelaX.fxml";
                        tituloJanela = "Painel do Administrador";
                    }
                    case "cliente" -> {
                        arquivoFXML = "TelaY.fxml";
                        tituloJanela = "Painel do Cliente";
                    }
                    case "operador" -> {
                        arquivoFXML = "TelaZ.fxml";
                        tituloJanela = "Painel do Operador";
                    }
                }
            }

            java.net.URL fxmlUrl = getClass().getResource(arquivoFXML);
            if (fxmlUrl == null) {
                fxmlUrl = getClass().getResource(arquivoFXML.toLowerCase());
            }

            if (fxmlUrl == null) {
                exibirErro("Erro de Arquivo", "O arquivo '" + arquivoFXML + "' não foi localizado.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage stage = (Stage) entrarBotao.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(tituloJanela);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            exibirErro("Falha no Módulo", "Não foi possível abrir a tela correspondente.");
        }
    }

    private void exibirErro(String titulo, String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Erro de Acesso");
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}
