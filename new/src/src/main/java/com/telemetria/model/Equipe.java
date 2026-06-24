 package com.telemetria.model;

import java.util.List;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.telemetria.db.ConexaoBanco;
import com.telemetria.repository.GeralDAO;
import com.telemetria.repository.SensorDAO;
import com.telemetria.repository.UsuarioDAO;
import com.telemetria.repository.VeiculoDAO;

public class Equipe extends Usuario implements Autenticavel {
    
    public Equipe(String login, String senha, String nome, String email, PerfilAcesso perfil) {
        super(login, senha, nome, email, perfil);
    }
    
    @Override
    public int getQuantidadeFatores() {
        return 3;
    }
    
    @Override
    public boolean validarFator(String senhaDigitada) {
        return super.getSenha().equals(senhaDigitada); 
    }
    
    @Override
    public boolean validarFator(String senhaDigitada, String token) {
        return token.equals("000000");
    }
    
    @Override
    public boolean validarFator(String senha, String token, String biometria) {
        return biometria.equals("Digital ok");
    }
    
    @Override
    public void acessarSistema() {
        Scanner leitor = new Scanner(System.in);
        int opcao;

        do {
            System.out.println("\n--- PAINEL ADMINISTRATIVO: " + this.nome.toUpperCase() + " ---");
            System.out.println("Perfil: " + this.perfil.getDescricao());
            System.out.println("-------------------------------------------");
            
            if (podeExecutar("VER_FROTA")){
                System.out.println("1. Visualizar Frota Completa");
            }
            if (podeExecutar("MANTER_VEICULO")){
                System.out.println("2. Cadastrar Novo Veículo"); 
            }
            if (podeExecutar("VER_USUARIO")){
                System.out.println("3. Visualizar a lista de Usuários Completa");
            }
            if (podeExecutar("CRIAR_USUARIO")){
                System.out.println("4. Adicionar Novo Usuário");
            }
            if (podeExecutar("EDITAR_USUARIO")){
                System.out.println("5. Editar Dados de Usuário");
            }
            if (podeExecutar("EXCLUIR_USUARIO")) {
                System.out.println("6. Excluir Usuário");
            }
            if (podeExecutar("VER_LOGS")) { 
                System.out.println("7. Ver Logs do Sistema");
            }
            if (podeExecutar("LIMPAR_LOGS")) {
                System.out.println("8. Limpar Logs do Sistema");
            }
            
            if (podeExecutar("MANTER_VEICULO")){
                System.out.println("9. Adicionar Sensor a um Veículo"); 
            }
            System.out.println("0. Sair");
            System.out.print("Escolha uma opção: ");
            
            opcao = leitor.nextInt();
            leitor.nextLine(); // Limpar buffer

            processarEscolha(opcao, leitor);

        } while (opcao != 0);
    }

    private void processarEscolha(int opcao, Scanner leitor) {
        switch (opcao) {
            case 1: if (podeExecutar("VER_FROTA")) verFrotaFiltrada(leitor); break;
            case 2: if (podeExecutar("MANTER_VEICULO")) cadastrarNovoVeiculo(leitor); break;
            case 3: if (podeExecutar("VER_USUARIO")) listarUsuarios(leitor); break;
            case 4: if (podeExecutar("CRIAR_USUARIO")) cadastrarNovoUsuario(leitor); break;
            case 5: if (podeExecutar("EDITAR_USUARIO")) editarUsuario(leitor); break;
            case 6: if (podeExecutar("EXCLUIR_USUARIO")) removerUsuario(leitor); break;
            
            case 7:
                if (podeExecutar("VER_LOGS")) {
                    GeralDAO.lerLogsBanco(); 
                }
                break;
                
            case 8:
                if (podeExecutar("LIMPAR_LOGS")) {
                    System.out.print("Deseja realmente limpar todos os logs? (S/N): ");
                    if (leitor.nextLine().equalsIgnoreCase("S")) {
                        // Lógica de limpar logs viria aqui
                        System.out.println("Logs excluídos permanentemente.");
                    }
                }
                break;
                
            case 9:
                if (podeExecutar("MANTER_VEICULO")) adicionarSensorAoVeiculo(leitor);
                break;
                
            case 0:
                System.out.println("Saindo do painel...");
                break;
            default:
                System.out.println("Opção inválida ou sem permissão.");
        }
    }

    private void adicionarSensorAoVeiculo(Scanner leitor) {
        System.out.println("\n--- CONFIGURAÇÃO DE SENSOR ---");
        
        // 1. Identifica o usuário
        System.out.print("Digite o e-mail do cliente (proprietário): ");
        String emailDono = leitor.nextLine();
        
        // 2. Exibe os veículos vinculados a esse usuário
        System.out.println("\nBuscando frota do cliente...");
        GeralDAO.listarVeiculosPorDono(emailDono);
        
        // 3. Seleção do veículo com opção de cancelamento
        System.out.print("\nDigite o Identificador do Veículo escolhido (Placa/Serial) ou '0' para cancelar: ");
        String placa = leitor.nextLine();
        
        if (placa.equals("0")) {
            System.out.println("Operação cancelada. Retornando ao menu...");
            return; // Interrompe o método e volta ao menu
        }
        
        // 4. Coleta os dados do sensor normalmente
        System.out.print("Categoria do Sensor (ex: Telemetria, Motor): ");
        String categoria = leitor.nextLine();
        
        System.out.print("Nome do Sensor (ex: Termômetro, GPS Frontal): ");
        String nome = leitor.nextLine();
        
        System.out.print("Unidade de Medida (ex: ºC, km/h, Status): ");
        String und = leitor.nextLine();
        
        System.out.print("Tipo do Dado (ex: Decimal, 1/0): ");
        String tipo = leitor.nextLine();
        
        System.out.print("Limite Máximo de Segurança (0 se não houver): ");
        double limite = leitor.nextDouble();
        leitor.nextLine(); // Limpar buffer

        // Cria o sensor usando o construtor da classe Sensor
        Sensor novoSensor = new Sensor(categoria, nome, tipo, und);
        novoSensor.setLimiteMaximo(limite);
        novoSensor.setValorAtual(0.0); 

        // Envia para o banco de dados
        if (SensorDAO.cadastrarSensorNoBanco(novoSensor, placa)) {
            System.out.println("Sucesso: Sensor '" + nome + "' vinculado ao veículo " + placa + ".");
        } else {
            System.out.println("Erro: Falha na vinculação. Verifique se o identificador foi digitado corretamente.");
        }
    }

    // --- LISTAR USUÁRIOS ATUALIZADO COM SUBMENU ---
    private void listarUsuarios(Scanner leitor) {
        System.out.println("\n--- LISTA DE USUÁRIOS CADASTRADOS ---");
    
        List<Usuario> usuarios = UsuarioDAO.listarUsuarios(this.perfil);

        if (usuarios.isEmpty()) {
            System.out.println("Nenhum usuário encontrado no sistema.");
            System.out.println("\nPressione Enter para voltar...");
            leitor.nextLine();
            return;
        } 
        
        System.out.printf("%-15s | %-20s | %-25s | %-15s\n", "Login", "Nome", "Email", "Perfil");
        System.out.println("---------------------------------------------------------------------------------");
    
        for (Usuario u : usuarios) {
            System.out.printf("%-15s | %-20s | %-25s | %-15s\n", 
                u.getLogin(), u.getNome(), u.getEmail(), u.getPerfil().getDescricao());
        }
        
        System.out.println("---------------------------------------------------------------------------------");
        System.out.print("\nDigite o LOGIN de um usuário para ver seus dados completos e veículos (ou '0' para VOLTAR): ");
        String escolha = leitor.nextLine();

        if (escolha.equals("0")) {
            return; // Retorna imediatamente ao menu principal
        }

        // Busca o usuário selecionado na lista
        Usuario usuarioSelecionado = null;
        for (Usuario u : usuarios) {
            if (u.getLogin().equalsIgnoreCase(escolha)) {
                usuarioSelecionado = u;
                break;
            }
        }

        if (usuarioSelecionado != null) {
            detalharUsuario(usuarioSelecionado, leitor);
        } else {
            System.out.println("Usuário não encontrado. Pressione Enter para voltar.");
            leitor.nextLine();
        }
    }

    // --- NOVA FUNÇÃO: DETALHAR USUÁRIO, VEÍCULOS E SENSORES ---
    private void detalharUsuario(Usuario u, Scanner leitor) {
        System.out.println("\n=======================================================");
        System.out.println("FICHA COMPLETA DO USUÁRIO");
        System.out.println("Nome:   " + u.getNome());
        System.out.println("E-mail: " + u.getEmail());
        System.out.println("Login:  " + u.getLogin());
        System.out.println("Perfil: " + u.getPerfil().getDescricao());
        System.out.println("-------------------------------------------------------");
        System.out.println("FROTA VINCULADA:");

        String sqlVeiculos = "SELECT v.id AS veiculo_id, v.identificador, v.tipo_veiculo " +
                             "FROM veiculos v JOIN usuario us ON v.usuario_id = us.id " +
                             "WHERE us.login = ?";
                             
        String sqlSensores = "SELECT nome, und_medida, valor_atual FROM sensores WHERE veiculo_id = ?";

        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement stmtVeic = conn.prepareStatement(sqlVeiculos)) {

            stmtVeic.setString(1, u.getLogin());
            try (ResultSet rsVeic = stmtVeic.executeQuery()) {
                
                boolean temVeiculo = false;
                
                while (rsVeic.next()) {
                    temVeiculo = true;
                    int idVeiculo = rsVeic.getInt("veiculo_id");
                    String placa = rsVeic.getString("identificador");
                    String tipo = rsVeic.getString("tipo_veiculo");
                    
                    System.out.println("\n  🚗 Veículo: " + tipo + " [" + placa + "]");
                    
                    // Busca os sensores deste veículo específico
                    try (PreparedStatement stmtSens = conn.prepareStatement(sqlSensores)) {
                        stmtSens.setInt(1, idVeiculo);
                        try (ResultSet rsSens = stmtSens.executeQuery()) {
                            boolean temSensor = false;
                            while(rsSens.next()) {
                                temSensor = true;
                                String nomeSensor = rsSens.getString("nome");
                                double valor = rsSens.getDouble("valor_atual");
                                String und = rsSens.getString("und_medida");
                                
                                System.out.println("      ↳ Sensor: " + nomeSensor + " -> Leitura: " + valor + " " + (und != null ? und : ""));
                            }
                            if (!temSensor) {
                                System.out.println("      ↳ (Nenhum sensor instalado neste veículo)");
                            }
                        }
                    }
                }
                
                if (!temVeiculo) {
                    System.out.println("  Nenhum veículo cadastrado para este usuário.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar detalhes da frota: " + e.getMessage());
        }

        System.out.println("=======================================================");
        System.out.println("\nPressione Enter para voltar ao menu...");
        leitor.nextLine(); 
    }
    // ------------------------------------------------------------

    private void verFrotaFiltrada(Scanner leitor) {
        System.out.print("Digite um termo para filtrar (ou Enter para ver tudo): ");
        String filtro = leitor.nextLine();
        
        GeralDAO dao = new GeralDAO();
        List<Veiculo> frota = dao.listarFrotaCompleta();

        System.out.println("\n--- RESULTADO DA BUSCA ---");
        frota.stream()
             .filter(v -> v.getIdentificador().contains(filtro))
             .forEach(Veiculo::exibirStatusSensores);
    }

    private void cadastrarNovoVeiculo(Scanner leitor) {
        System.out.println("\n--- CADASTRAR NOVO VEÍCULO ---");
        
        System.out.print("Email do proprietário (para vincular no banco): ");
        String emailProprietario = leitor.nextLine();
        
        System.out.print("Tipo do Veículo (ex: Automovel, Drone): ");
        String tipoVeiculo = leitor.nextLine();
        
        System.out.print("Tipo do Identificador (ex: Placa, Serial): ");
        String tipoIdentificador = leitor.nextLine();
        
        System.out.print("Identificador (ex: ABC1245): ");
        String identificador = leitor.nextLine();

        Veiculo novoVeiculo = new Veiculo(0, identificador, tipoIdentificador, null);
    
        boolean sucesso = VeiculoDAO.cadastrarVeiculoNoBanco(novoVeiculo, emailProprietario);
        
        if (sucesso) {
            System.out.println("Sucesso: " + tipoVeiculo + " [" + identificador + "] cadastrado no sistema.");
            System.out.println("Atenção: A configuração dos sensores (GPS, etc.) deve ser feita no módulo técnico.");
        } else {
            System.out.println("Erro: Falha ao cadastrar o veículo no banco de dados. Verifique se o e-mail do proprietário existe no sistema.");
        }
    }

    private void cadastrarNovoUsuario(Scanner leitor) {
        System.out.print("Nome: "); 
        String nome = leitor.nextLine();
        System.out.print("Email: "); 
        String email = leitor.nextLine();
        System.out.print("Login: ");
        String login = leitor.nextLine();
        System.out.print("Senha: ");
        String senha = leitor.nextLine();
        System.out.print("Nível (0:Cliente, 1:Frotista, 2:Operador, 3:Admin): ");
        int nivelEscolhido = leitor.nextInt(); 
        leitor.nextLine();
        
        try{
            PerfilAcesso novoPerfil = PerfilAcesso.porNivel(nivelEscolhido);
            if (UsuarioDAO.cadastrarUsuario(nome, email, login, senha, novoPerfil)) {
                System.out.println("Sucesso: Usuário " + login + " criado.");
            }
        } catch (IllegalArgumentException ex){
            System.out.println("Erro: " + ex.getMessage());
        }
    }

    private void editarUsuario(Scanner leitor) {
        System.out.print("Login do usuário a editar: ");
        String login = leitor.nextLine();
        System.out.print("Novo nome: ");
        String nome = leitor.nextLine();
        System.out.print("Nova senha: ");
        String senha = leitor.nextLine();
        System.out.print("Nível (0:Cliente, 1:Frotista, 2:Operador, 3:Admin): ");
        int nivelEscolhido = leitor.nextInt(); 
        leitor.nextLine();

        try{
            PerfilAcesso novoPerfil = PerfilAcesso.porNivel(nivelEscolhido);
            if (UsuarioDAO.atualizarUsuario(this, login, nome, senha, novoPerfil)) {
                System.out.println("Dados atualizados.");
            }
         } catch (IllegalArgumentException ex){
            System.out.println("Erro: " + ex.getMessage());
        }
    }

    private void removerUsuario(Scanner leitor) {
        System.out.print("Login do usuário a EXCLUIR: ");
        String login = leitor.nextLine();
        System.out.print("Tem certeza? (S/N): ");
        if (leitor.nextLine().equalsIgnoreCase("S")) {
            UsuarioDAO.excluirUsuario(this, login);
        }
    }
}