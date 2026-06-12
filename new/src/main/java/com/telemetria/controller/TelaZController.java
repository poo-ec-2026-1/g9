package com.telemetria.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ChoiceDialog;
import java.util.Optional;

public class TelaZController {

    @FXML
    private Label labelStatus;

    @FXML
    public void initialize() {
        String nomeInstalador = RegistroLoginController.getNomeSalvo();
        if (nomeInstalador != null) {
            labelStatus.setText("Instalador: " + nomeInstalador + " | Autenticado (1 Fator)");
        } else {
            labelStatus.setText("Instalador: Técnico Padrão | Autenticado");
        }
    }

   
    @FXML
    private void verificarTelemetria(ActionEvent event) {
        java.util.List<String> vinculos = RegistroLoginController.getListaSensoresUsuarios();

        if (vinculos.isEmpty()) {
            exibirAlerta("Telemetria do Sistema", "Nenhum sensor foi associado a um usuário até o momento.");
            return;
        }

        StringBuilder listagem = new StringBuilder("--- SENSORES INSTALADOS E VINCULADOS ---\n\n");
        for (String v : vinculos) {
            listagem.append(v).append("\n");
        }

        exibirAlerta("Lista de Sensores Ativos", listagem.toString());
    }

 
    @FXML
    private void realizarInstalacao(ActionEvent event) {
        java.util.List<String> usuariosString = RegistroLoginController.getListaUsuarios();
        java.util.List<String> loginsValidos = new java.util.ArrayList<>();

        
        for (String linha : usuariosString) {
            try {
                String[] partes = linha.split("\\|");
                for (String parte : partes) {
                    if (parte.trim().startsWith("Login:")) {
                        loginsValidos.add(parte.replace("Login:", "").trim());
                    }
                }
            } catch (Exception e) {
                
            }
        }

        if (loginsValidos.isEmpty()) {
            Alert alertaErro = new Alert(Alert.AlertType.ERROR, "Não há usuários cadastrados no sistema para vincular um sensor.");
            alertaErro.showAndWait();
            return;
        }

       
        ChoiceDialog<String> dialogUser = new ChoiceDialog<>(loginsValidos.get(0), loginsValidos);
        dialogUser.setTitle("Configurar Instalação");
        dialogUser.setHeaderText("Passo 1: Seleção de Usuário");
        dialogUser.setContentText("Escolha o usuário que receberá o sensor:");
        
        Optional<String> resUser = dialogUser.showAndWait();
        if (!resUser.isPresent()) return;
        String usuarioSelecionado = resUser.get();

       
        TextInputDialog dialogSensor = new TextInputDialog("SNSR-");
        dialogSensor.setTitle("Configurar Instalação");
        dialogSensor.setHeaderText("Passo 2: Identificador do Dispositivo");
        dialogSensor.setContentText("Digite o código ou número de série do Sensor:");
        
        Optional<String> resSensor = dialogSensor.showAndWait();
        if (!resSensor.isPresent() || resSensor.get().trim().isEmpty()) return;
        String sensorDigitado = resSensor.get().trim().toUpperCase();

        
        TextInputDialog dialogModelo = new TextInputDialog("Presença / Temperatura");
        dialogModelo.setTitle("Configurar Instalação");
        dialogModelo.setHeaderText("Passo 3: Tipo/Modelo");
        dialogModelo.setContentText("Informe o modelo/função deste sensor:");
        
        Optional<String> resModelo = dialogModelo.showAndWait();
        if (!resModelo.isPresent() || resModelo.get().trim().isEmpty()) return;
        String modeloDigitado = resModelo.get().trim();

        
        RegistroLoginController.vincularSensorUsuario(usuarioSelecionado, sensorDigitado, modeloDigitado);

        
        try {
            RegistroLoginController.getListaLogs().add("[LOG] Sensor " + sensorDigitado + " vinculado ao usuário: " + usuarioSelecionado);
        } catch (Exception e) {
           
        }

        exibirAlerta("Instalação Concluída", "Sensor " + sensorDigitado + " configurado e vinculado com sucesso ao usuário '" + usuarioSelecionado + "'!");
    }

    private void exibirAlerta(String titulo, String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}