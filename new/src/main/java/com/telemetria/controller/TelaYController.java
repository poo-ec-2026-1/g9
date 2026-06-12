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
        // Recupera o nome do cliente que está logado no momento
        String nomeCliente = RegistroLoginController.getNomeSalvo();
        
        if (nomeCliente != null && !nomeCliente.isEmpty()) {
            labelBoasVindas.setText("Painel do Cliente: " + nomeCliente);
        } else {
            labelBoasVindas.setText("Painel do Cliente | Central de Monitoramento");
        }
    }

    // Ação do Botão: Meus Sensores Vinculados
    @FXML
    private void verificarMeusSensores(ActionEvent event) {
        // Pega o login (ID único) do usuário que está logado atualmente
        String usuarioLogado = RegistroLoginController.getUsuarioSalvo();
        
        // Se por acaso for o administrador testando a conta padrão
        if (usuarioLogado == null && LoginController.cargoLogado.equals("administrador")) {
            usuarioLogado = "adm";
        }

        // Pega a lista global de sensores que o operador cadastrou
        List<String> todosVinculos = RegistroLoginController.getListaSensoresUsuarios();
        StringBuilder meusSensores = new StringBuilder();

        // Filtra para exibir apenas os sensores vinculados a este cliente específico
        for (String vinculo : todosVinculos) {
            if (vinculo.contains("Usuário: " + usuarioLogado + " ")) {
                // Limpa o prefixo do usuário para ficar mais amigável na tela do cliente
                String dadosFormatados = vinculo.replace("Usuário: " + usuarioLogado + " | ", "");
                meusSensores.append("• ").append(dadosFormatados).append("\n");
            }
        }

        // Exibe o resultado com base no que foi encontrado
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