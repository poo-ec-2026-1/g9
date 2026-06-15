package com.telemetria.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import java.util.List;

public class TelaYController {

    @FXML
    private Label labelBoasVindas;

    @FXML
    public void initialize() {
        
        String nomeCliente = RegistroLoginController.getNomeSalvo();
        
        if (nomeCliente != null && !nomeCliente.isEmpty()) {
            labelBoasVindas.setText("Painel do Cliente: " + nomeCliente);
        } else {
            labelBoasVindas.setText("Painel do Cliente | Central de Monitoramento");
        }
    }

  
    @FXML
    private void verificarMeusSensores(ActionEvent event) {
        
        String usuarioLogado = RegistroLoginController.getUsuarioSalvo();
        
        
        if (usuarioLogado == null && LoginController.cargoLogado.equals("administrador")) {
            usuarioLogado = "adm";
        }

        
        List<String> todosVinculos = RegistroLoginController.getListaSensoresUsuarios();
        StringBuilder meusSensores = new StringBuilder();

        
        for (String vinculo : todosVinculos) {
            if (vinculo.contains("Usuário: " + usuarioLogado + " ")) {
                
                String dadosFormatados = vinculo.replace("Usuário: " + usuarioLogado + " | ", "");
                meusSensores.append("• ").append(dadosFormatados).append("\n");
            }
        }

        
        if (meusSensores.length() == 0) {
            exibirAlerta("Meus Dispositivos", "Você ainda não possui nenhum sensor instalado ou vinculado à sua conta.");
        } else {
            exibirAlerta("Seus Sensores Ativos", "Abaixo estão os dispositivos monitorando sua conta:\n\n" + meusSensores.toString());
        }
    }

    private void exibirAlerta(String titulo, String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}
