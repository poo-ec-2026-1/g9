package com.telemetria.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class RegistroLoginController {

    @FXML
    private TextField nomeCadastro;

    @FXML
    private TextField emailCadastro;

    @FXML
    private TextField senhaCadastro;

    @FXML
    private ComboBox<String> comboCargo; 

    @FXML
    private Button botaoRegistro;

    private static String usuarioSalvo;
    private static String senhaSalva;
    private static String nomeSalvo;
    private static String cargoSalvo; 

    private static List<String> listaUsuarios = new ArrayList<>();
    private static List<String> listaFrota = new ArrayList<>();
    private static List<String> listaSensoresUsuarios = new ArrayList<>();

    // 1. Procure onde estão as listas e adicione a lista de logs logo abaixo delas:
private static List<String> listaLogs = new ArrayList<>();

// 2. Dentro do bloco "static { ... }", adicione estes logs iniciais para teste:
static {
    listaFrota.add("Placa: ABC-1234 | Modelo: Scania R450 | Sensor: SNSR-01");
    listaFrota.add("Placa: XYZ-9876 | Modelo: Volvo FH540 | Sensor: SNSR-02");

    listaUsuarios.add("Login: adm | Senha: 123 | Nome: Administrador Geral | Cargo: Administrador");
    listaUsuarios.add("Login: adm1 | Senha: 1234 | Nome: Operador Padrão | Cargo: Operador");

    // ALIMENTAÇÃO INICIAL DE LOGS (NOVO)
    listaLogs.add("[05/06/2026 10:14] SISTEMA: Inicializado com sucesso.");
    listaLogs.add("[05/06/2026 11:02] LOGIN: Usuário 'adm' efetuou acesso.");
    listaLogs.add("[05/06/2026 14:35] CADASTRO: Novo veículo ABC-1234 adicionado.");
}

// 3. Lá no final da classe, junto com os outros métodos públicos, adicione estes três:
public static List<String> getListaLogs() {
    return listaLogs;
}

public static void adicionarLog(String mensagem) {
    listaLogs.add(mensagem);
}

public static void limparTodosLogs() {
    listaLogs.clear();
}
    @FXML
    public void initialize() {
        if (comboCargo != null) {
            comboCargo.setItems(FXCollections.observableArrayList("Cliente", "Operador"));
            comboCargo.setValue("Cliente"); 
        }
    }

    @FXML
    private void registrar(ActionEvent event) {
        nomeSalvo = nomeCadastro.getText();
        usuarioSalvo = emailCadastro.getText();
        senhaSalva = senhaCadastro.getText();
        cargoSalvo = comboCargo.getValue(); 

        if (nomeSalvo.trim().isEmpty() || usuarioSalvo.trim().isEmpty() || senhaSalva.trim().isEmpty()) {
            Alert alertaAviso = new Alert(Alert.AlertType.WARNING, "Por favor, preencha todos os campos do cadastro.");
            alertaAviso.showAndWait();
            return;
        }

        // Passando a senha informada no cadastro para a lista
        adicionarUsuarioLista(usuarioSalvo, senhaSalva, nomeSalvo, cargoSalvo);

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Cadastro");
        alerta.setHeaderText(null);
        alerta.setContentText("Registrado como " + cargoSalvo + " com sucesso!");
        alerta.showAndWait();

        Stage stage = (Stage) botaoRegistro.getScene().getWindow();
        stage.close();
    }

    public static List<String> getListaUsuarios() {
        return listaUsuarios;
    }
    
    public static void removerUsuarioLista(String dadosUsuario) {
    listaUsuarios.remove(dadosUsuario);
}

    // Método atualizado para incluir o campo Senha
    public static void adicionarUsuarioLista(String login, String senha, String nome, String cargo) {
        String dadosFormatados = "Login: " + login + " | Senha: " + senha + " | Nome: " + nome + " | Cargo: " + cargo;
        listaUsuarios.add(dadosFormatados);
    }

    public static List<String> getListaFrota() {
        return listaFrota;
    }

    public static void adicionarVeiculo(String veiculoFormatado) {
        listaFrota.add(veiculoFormatado);
    }
    public static void atualizarUsuarioLista(String dadosAntigos, String dadosNovos) {
    int indice = listaUsuarios.indexOf(dadosAntigos);
    if (indice != -1) {
        listaUsuarios.set(indice, dadosNovos);
    }
    
}
public static List<String> getListaSensoresUsuarios() {
    return listaSensoresUsuarios;
}

public static void vincularSensorUsuario(String usuario, String sensor, String modelo) {
    String vinculoFormatado = "Usuário: " + usuario + " | Sensor: " + sensor + " | Modelo: " + modelo;
    listaSensoresUsuarios.add(vinculoFormatado);
}

    public static String getUsuarioSalvo() { return usuarioSalvo; }
    public static void setUsuarioSalvo(String novoUsuario) { usuarioSalvo = novoUsuario; }
    public static String getSenhaSalva() { return senhaSalva; }
    public static void setSenhaSalva(String novaSenha) { senhaSalva = novaSenha; }
    public static String getNomeSalvo() { return nomeSalvo; }
    public static void setNomeSalvo(String novoNome) { nomeSalvo = novoNome; }
    public static String getCargoSalvo() { return cargoSalvo; }
    public static void setCargoSalvo(String novoCargo) { cargoSalvo = novoCargo; }
}