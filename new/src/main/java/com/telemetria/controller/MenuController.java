package com.telemetria.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class MenuController {

    @FXML
    private Label labelBoasVindas;

    @FXML
    public void initialize() {
        String nomeUsuario = RegistroLoginController.getNomeSalvo();
        
        if (nomeUsuario != null && !nomeUsuario.isEmpty()) {
            labelBoasVindas.setText("Olá, " + nomeUsuario);
        } else {
            labelBoasVindas.setText("Olá, adm");
        }
    }
    
    private void mostrarErro(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setHeaderText("Erro");
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
    
   @FXML

private void acessarModulo(ActionEvent event) {
    try {
        String cargo = LoginController.cargoLogado;
        String arquivoFXML = "";
        String tituloJanela = "";

        if (cargo != null && !cargo.trim().isEmpty()) {
            // Normaliza o cargo eliminando espaços e deixando em minúsculo
            cargo = cargo.trim().toLowerCase();

            switch (cargo) {
                case "administrador" -> {
                    arquivoFXML = "TelaX.fxml"; // Certifique-se que o arquivo se chama TelaX.fxml exatamente assim!
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
                default -> {
                    mostrarErro("Cargo não reconhecido no sistema: " + cargo);
                    return;
                }
            }
        } else {
            mostrarErro("Nenhum usuário logado identificado.");
            return;
        }

        // Validação crucial: Verifica se o arquivo FXML realmente existe no projeto
        java.net.URL fxmlUrl = getClass().getResource(arquivoFXML);
        if (fxmlUrl == null) {
            mostrarErro("Erro de Sistema: O arquivo de tela '" + arquivoFXML + "' não foi encontrado na pasta do projeto. Verifique o nome real do arquivo.");
            return;
        }

        // Carrega a tela correspondente definida no switch case acima
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle(tituloJanela);
        stage.setScene(new Scene(root));
        stage.show();

    } catch (Exception e) {
        System.out.println("--- ERRO AO CARREGAR INTERFACE ---");
        e.printStackTrace(); // Mostra o erro exato no terminal do BlueJ para você saber o que quebrou
        System.out.println("----------------------------------");
        mostrarErro("Não foi possível abrir o módulo correspondente ao seu cargo.");
    }

}
    @FXML
    private void alterarDados(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AlterarDados.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Alterar");
            stage.setScene(new Scene(root));
            stage.show();
        } catch(Exception e) {
            mostrarErro("Não foi possível abrir o módulo.");
        }
    }

   @FXML
private void excluirConta(ActionEvent event) {
    Alert alerta = new Alert(
            Alert.AlertType.CONFIRMATION,
            "Deseja realmente excluir sua conta? Todos os seus dados serão apagados.",
            ButtonType.YES,
            ButtonType.NO);

    alerta.setHeaderText("Excluir Conta");

    if (alerta.showAndWait().get() == ButtonType.YES) {
        try {
            // APAGA OS DADOS DA MEMÓRIA: Define tudo como null
            RegistroLoginController.setNomeSalvo(null);
            RegistroLoginController.setUsuarioSalvo(null);
            RegistroLoginController.setSenhaSalva(null);

            // Mostra a confirmação para o usuário
            Alert confirmacao = new Alert(Alert.AlertType.INFORMATION);
            confirmacao.setHeaderText(null);
            confirmacao.setContentText("Sua conta foi excluída permanentemente.");
            confirmacao.showAndWait();

            // Redireciona para a tela de Login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();

            Stage stageAtual = (Stage)((javafx.scene.Node)event.getSource())
                    .getScene()
                    .getWindow();

            stageAtual.setScene(new Scene(root));
            stageAtual.setTitle("Login");
        } catch(Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao processar a exclusão da conta.");
        }
    }
}

    @FXML
    private void logout(ActionEvent event) {
        try {
           
            RegistroLoginController.setNomeSalvo(null);
            RegistroLoginController.setUsuarioSalvo(null);
            RegistroLoginController.setSenhaSalva(null);
            LoginController.cargoLogado = null; 

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();

            Stage stageAtual = (Stage) ((javafx.scene.Node) event.getSource())
                    .getScene()
                    .getWindow();

            stageAtual.setScene(new Scene(root));
            stageAtual.setTitle("Login");
        } catch(Exception e) {
            System.out.println("--- ERRO NO LOGOUT ---");
            e.printStackTrace(); 
            mostrarErro("Erro ao realizar logout.");
        }
    }
    
    @FXML
private void abrirAlterarSenha(ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("AlterarSenha.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("Alterar Senha");
        stage.setScene(new Scene(root));
        stage.show();
    } catch(Exception e) {
        mostrarErro("Não foi possível abrir a tela de alteração de senha.");
    }
}




}