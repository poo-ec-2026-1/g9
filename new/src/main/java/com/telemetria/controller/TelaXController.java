package com.telemetria.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;

public class TelaXController {

    @FXML
    private void vincularSensor(ActionEvent event) {
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
            mostrarErro("Não há usuários cadastrados no sistema para vincular um sensor.");
            return;
        }

        javafx.scene.control.ChoiceDialog<String> dialogUser = new javafx.scene.control.ChoiceDialog<>(loginsValidos.get(0), loginsValidos);
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
        RegistroLoginController.adicionarLog("[" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "] ADM: Vinculou o sensor " + sensorDigitado + " ao usuário " + usuarioSelecionado + ".");
        exibirAlerta("Sucesso", "Sensor " + sensorDigitado + " associado ao usuário '" + usuarioSelecionado + "' com sucesso!");
    }

    @FXML
    private void verTodosSensores(ActionEvent event) {
        java.util.List<String> vinculos = RegistroLoginController.getListaSensoresUsuarios();

        if (vinculos.isEmpty()) {
            exibirAlerta("Sensores do Sistema", "Nenhum sensor foi associado a um usuário até o momento.");
            return;
        }

        StringBuilder listagem = new StringBuilder("--- SENSORES VINCULADOS NO SISTEMA ---\n\n");
        for (String v : vinculos) {
            listagem.append(v).append("\n");
        }

        exibirAlerta("Lista Global de Sensores", listagem.toString());
    }

    @FXML
    private void editarUsuario(ActionEvent event) {
        java.util.List<String> usuarios = RegistroLoginController.getListaUsuarios();

        if (usuarios.isEmpty()) {
            mostrarErro("Não existem usuários cadastrados no sistema para editar.");
            return;
        }

        javafx.scene.control.ChoiceDialog<String> dialogSelecao = new javafx.scene.control.ChoiceDialog<>(usuarios.get(0), usuarios);
        dialogSelecao.setTitle("Editar Usuário");
        dialogSelecao.setHeaderText("Modificar Credenciais de Acesso");
        dialogSelecao.setContentText("Selecione o usuário que deseja alterar:");

        Optional<String> resultadoSelecao = dialogSelecao.showAndWait();

        if (resultadoSelecao.isPresent()) {
            String usuarioSelecionado = resultadoSelecao.get();

            String login = extrairValorAuxiliar(usuarioSelecionado, "Login:");
            String senha = extrairValorAuxiliar(usuarioSelecionado, "Senha:");
            String nomeAtual = extrairValorAuxiliar(usuarioSelecionado, "Nome:");
            String cargoAtual = extrairValorAuxiliar(usuarioSelecionado, "Cargo:");

            TextInputDialog dialogNome = new TextInputDialog(nomeAtual);
            dialogNome.setTitle("Alterar Nome");
            dialogNome.setHeaderText("Editar dados de: " + login);
            dialogNome.setContentText("Digite o novo nome completo:");
            
            Optional<String> novoNomeResult = dialogNome.showAndWait();
            if (!novoNomeResult.isPresent() || novoNomeResult.get().trim().isEmpty()) return;
            String novoNome = novoNomeResult.get().trim();

            java.util.List<String> cargosDisponiveis = java.util.List.of("Cliente", "Operador", "Administrador");
            javafx.scene.control.ChoiceDialog<String> dialogCargo = new javafx.scene.control.ChoiceDialog<>(cargoAtual, cargosDisponiveis);
            dialogCargo.setTitle("Alterar Cargo");
            dialogCargo.setHeaderText("Editar perfil de: " + login);
            dialogCargo.setContentText("Selecione o novo nível de acesso:");

            Optional<String> novoCargoResult = dialogCargo.showAndWait();
            if (!novoCargoResult.isPresent()) return;
            String novoCargo = novoCargoResult.get();

            String dadosAtualizados = "Login: " + login + " | Senha: " + senha + " | Nome: " + novoNome + " | Cargo: " + novoCargo;
            RegistroLoginController.atualizarUsuarioLista(usuarioSelecionado, dadosAtualizados);

            if (login.equals("adm") || login.equals(RegistroLoginController.getUsuarioSalvo())) {
                RegistroLoginController.setNomeSalvo(novoNome);
            }

            exibirAlerta("Sucesso", "Os dados do usuário foram atualizados com sucesso!");
        }
    }

    @FXML
    private void excluirUsuario(ActionEvent event) {
        java.util.List<String> usuarios = RegistroLoginController.getListaUsuarios();

        if (usuarios.isEmpty()) {
            mostrarErro("Não existem usuários cadastrados no sistema para excluir.");
            return;
        }

        javafx.scene.control.ChoiceDialog<String> dialog = new javafx.scene.control.ChoiceDialog<>(usuarios.get(0), usuarios);
        dialog.setTitle("Remover Usuário");
        dialog.setHeaderText("Zona Crítica: Exclusão de Credenciais");
        dialog.setContentText("Selecione o usuário que deseja apagar permanentemente:");

        Optional<String> resultado = dialog.showAndWait();
        
        resultado.ifPresent(usuarioSelecionado -> {
            if (usuarioSelecionado.contains("Login: adm |")) {
                mostrarErro("Por segurança, o administrador principal ('adm') não pode ser removido.");
                return;
            }

            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacao.setTitle("Confirmar Exclusão");
            confirmacao.setHeaderText("Aviso de segurança");
            confirmacao.setContentText("Tem certeza que deseja apagar permanentemente:\n" + usuarioSelecionado + "?");

            Optional<ButtonType> botaoClicado = confirmacao.showAndWait();
            if (botaoClicado.isPresent() && botaoClicado.get() == ButtonType.OK) {
                RegistroLoginController.removerUsuarioLista(usuarioSelecionado);
                exibirAlerta("Sucesso", "Usuário removido do sistema com sucesso!");
            }
        });
    }

    @FXML
    private void limparLogs(ActionEvent event) {
        java.util.List<String> logsAtuais = RegistroLoginController.getListaLogs();

        if (logsAtuais.isEmpty()) {
            exibirAlerta("Limpeza de Logs", "O banco de dados de logs já está completamente vazio.");
            return;
        }

        StringBuilder historico = new StringBuilder("Logs atuais no sistema:\n\n");
        for (String log : logsAtuais) {
            historico.append(log).append("\n");
        }
        
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Limpeza de Logs");
        confirmacao.setHeaderText("ZONA CRÍTICA: Apagar Histórico do Sistema");
        confirmacao.setContentText(historico.toString() + "\n\nDeseja realmente apagar todos os logs acima permanentemente?");
        
        Optional<ButtonType> resposta = confirmacao.showAndWait();
        
        if (resposta.isPresent() && resposta.get() == ButtonType.OK) {
            RegistroLoginController.limparTodosLogs();
            exibirAlerta("Sucesso", "Todos os logs do sistema foram excluídos permanentemente!");
        }
    }

    private String extrairValorAuxiliar(String linha, String chave) {
        try {
            String[] partes = linha.split("\\|");
            for (String parte : partes) {
                if (parte.trim().startsWith(chave)) {
                    return parte.replace(chave, "").trim();
                }
            }
        } catch (Exception e) {
        }
        return "";
    }

    private void exibirAlerta(String titulo, String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    private void mostrarErro(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Erro");
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}