import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class RegistroLoginController {

    private static String usuarioSalvo;
    private static String senhaSalva;
    private static String nomeSalvo;
    private static String cargoSalvo; 

    private static List<String> listaUsuarios = new ArrayList<>();
    private static List<String> listaFrota = new ArrayList<>();
    private static List<String> listaSensoresUsuarios = new ArrayList<>();
    private static List<String> listaLogs = new ArrayList<>();

    static {
        listaFrota.add("Placa: ABC-1234 | Modelo: Scania R450 | Sensor: SNSR-01");
        listaFrota.add("Placa: XYZ-9876 | Modelo: Volvo FH540 | Sensor: SNSR-02");

        listaUsuarios.add("Login: adm | Senha: 123 | Nome: Administrador Geral | Cargo: Administrador");
        listaUsuarios.add("Login: adm1 | Senha: 1234 | Nome: Operador Padrão | Cargo: Operador");

        listaLogs.add("[05/06/2026 10:14] SISTEMA: Inicializado com sucesso.");
        listaLogs.add("[05/06/2026 11:02] LOGIN: Usuário 'adm' efetuou acesso.");
        listaLogs.add("[05/06/2026 14:35] CADASTRO: Novo veículo ABC-1234 adicionado.");
    }

    @FXML private TextField nomeCadastro;
    @FXML private TextField emailCadastro;
    @FXML private TextField senhaCadastro;
    @FXML private Button botaoRegistro;

    @FXML
    public void initialize() {
    }

    @FXML
    private void registrar(ActionEvent event) {
        if (nomeCadastro == null || emailCadastro == null || senhaCadastro == null) {
            return;
        }

        nomeSalvo = nomeCadastro.getText();
        usuarioSalvo = emailCadastro.getText();
        senhaSalva = senhaCadastro.getText();
        cargoSalvo = "Cliente"; 

        if (nomeSalvo.trim().isEmpty() || usuarioSalvo.trim().isEmpty() || senhaSalva.trim().isEmpty()) {
            Alert alertaAviso = new Alert(Alert.AlertType.WARNING, "Por favor, preencha todos os campos do cadastro.");
            alertaAviso.showAndWait();
            return;
        }

        adicionarUsuarioLista(usuarioSalvo, senhaSalva, nomeSalvo, cargoSalvo);

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Cadastro");
        alerta.setHeaderText(null);
        alerta.setContentText("Registrado como " + cargoSalvo + " com sucesso!");
        alerta.showAndWait();

        if (botaoRegistro != null && botaoRegistro.getScene() != null) {
            Stage stage = (Stage) botaoRegistro.getScene().getWindow();
            stage.close();
        }
    }

    public static List<String> getListaLogs() { return listaLogs; }
    public static void adicionarLog(String mensagem) { listaLogs.add(mensagem); }
    public static void limparTodosLogs() { listaLogs.clear(); }
    public static List<String> getListaUsuarios() { return listaUsuarios; }
    public static void removerUsuarioLista(String dadosUsuario) { listaUsuarios.remove(dadosUsuario); }
    
    public static void adicionarUsuarioLista(String login, String senha, String nome, String cargo) {
        String dadosFormatados = "Login: " + login + " | Senha: " + senha + " | Nome: " + nome + " | Cargo: " + cargo;
        listaUsuarios.add(dadosFormatados);
    }

    public static List<String> getListaFrota() { return listaFrota; }
    public static void adicionarVeiculo(String veiculoFormatado) { listaFrota.add(veiculoFormatado); }
    
    public static void atualizarUsuarioLista(String dadosAntigos, String dadosNovos) {
        int indice = listaUsuarios.indexOf(dadosAntigos);
        if (indice != -1) {
            listaUsuarios.set(indice, dadosNovos);
        }
    }

    public static List<String> getListaSensoresUsuarios() { return listaSensoresUsuarios; }
    
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
