 package com.telemetria.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.telemetria.db.ConexaoBanco;
import com.telemetria.repository.GeralDAO;
import com.telemetria.repository.UsuarioDAO;
import com.telemetria.model.PerfilAcesso;

public class Cliente extends Usuario implements Autenticavel {
    
    public Cliente(String login, String senha, String nome, String email, PerfilAcesso perfil) {
        // Trava o perfil de segurança como FROTISTA / CLIENTE DONO DA FROTA
        super(login, senha, nome, email, perfil);
    }

    @Override
    public void acessarSistema() {
        Scanner leitor = new Scanner(System.in);
        int opcao;

        do {
            System.out.println("\n--- PAINEL DO GESTOR DA FROTA: " + this.getNome().toUpperCase() + " ---");
            System.out.println("1. Visualizar Frota e Gerenciar Motoristas");
            System.out.println("2. Enviar Mensagem para a Central");
            System.out.println("3. Criar conta de Motorista");
            System.out.println("0. Sair");
            System.out.print("Escolha uma opção: ");
            
            opcao = leitor.nextInt();
            leitor.nextLine(); // Limpar buffer

            switch (opcao) {
                case 1:
                    visualizarEGerenciarFrota(leitor);
                    break;
                case 2:
                    enviarMensagemParaCentral(leitor);
                    break;
                case 3:
                    criarMotoristaNivelZero(leitor);
                    break;
                case 0:
                    System.out.println("Saindo do painel do cliente...");
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        } while (opcao != 0);
    }

    // --- OPÇÃO 1: MENU INTERATIVO DE FROTA E MOTORISTAS ---
    private void visualizarEGerenciarFrota(Scanner leitor) {
        System.out.println("\n--- MINHA FROTA ---");
        List<String> placas = new ArrayList<>();
        List<String> tipos = new ArrayList<>();

        // Busca apenas os veículos vinculados ao e-mail deste gestor logado
        String sqlBusca = "SELECT identificador, tipo_veiculo FROM veiculos " +
                          "WHERE usuario_id = (SELECT id FROM usuario WHERE email = ?)";

        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBusca)) {

            stmt.setString(1, this.getEmail());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    placas.add(rs.getString("identificador"));
                    tipos.add(rs.getString("tipo_veiculo"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao carregar frota: " + e.getMessage());
            return;
        }

        if (placas.isEmpty()) {
            System.out.println("Você não possui veículos cadastrados.");
            return;
        }

        // Exibe a frota como uma lista numerada para facilitar a escolha
        for (int i = 0; i < placas.size(); i++) {
            System.out.println((i + 1) + ". Veículo: " + tipos.get(i) + " | Placa/Serial: " + placas.get(i));
        }

        System.out.println("0. Voltar ao menu");
        System.out.print("\nEscolha o número do carro que deseja adicionar um motorista: ");
        int escolha = leitor.nextInt();
        leitor.nextLine(); // Limpar buffer

        if (escolha <= 0 || escolha > placas.size()) {
            System.out.println("Operação cancelada. Voltando...");
            return;
        }

        String placaEscolhida = placas.get(escolha - 1);
        System.out.println("\nVeículo selecionado: [" + placaEscolhida + "]");
        System.out.println("1. Vincular uma conta de motorista JÁ EXISTENTE");
        System.out.println("2. Ver Motorista e Histórico de Localização");
        System.out.println("0. Cancelar");
        System.out.print("Opção: ");
        int opVinculo = leitor.nextInt();
        leitor.nextLine();

        String emailMotorista = "";

        if (opVinculo == 1) {
            System.out.print("Digite o e-mail do Motorista: ");
            emailMotorista = leitor.nextLine();
        } else if (escolha == 2) {
            GeralDAO.verHistoricoVeiculo(placaEscolhida); // <--- CHAMADA DA FUNÇÃO ABAIXO
        } else {
            return;
        }

        if (!emailMotorista.isEmpty()) {
            vincularMotoristaVeiculo(placaEscolhida, emailMotorista);
        }
    }

    private void vincularMotoristaVeiculo(String placa, String emailMotorista) {
        // Tenta criar a coluna motorista_id silenciosamente.
        // Se ela já existir, o erro é ignorado. Isso impede que o Gestor perca o veículo.
        try (Connection conn = ConexaoBanco.getConnection();
             Statement st = conn.createStatement()) {
            st.execute("ALTER TABLE veiculos ADD COLUMN motorista_id INTEGER");
        } catch (SQLException e) {
            // A coluna já existe, ignorar.
        }

        String sql = "UPDATE veiculos SET motorista_id = (SELECT id FROM usuario WHERE email = ?) " +
                     "WHERE identificador = ? AND usuario_id = (SELECT id FROM usuario WHERE email = ?)";

        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, emailMotorista);
            stmt.setString(2, placa);
            stmt.setString(3, this.getEmail()); // Trava de segurança para garantir que o carro é seu

            int linhas = stmt.executeUpdate();
            if (linhas > 0) {
                System.out.println("✅ Sucesso: O motorista " + emailMotorista + " foi vinculado ao veículo " + placa + ".");
            } else {
                System.out.println("❌ Erro: Não foi possível vincular. Verifique se o e-mail do motorista está correto.");
            }
        } catch (SQLException e) {
            System.out.println("Erro de banco de dados no vínculo: " + e.getMessage());
        }
    }

    // --- OPÇÃO 2: MENSAGEM PARA A CENTRAL ---
    private void enviarMensagemParaCentral(Scanner leitor) {
        System.out.println("\n--- MENSAGEM PARA A CENTRAL ---");
        System.out.print("Digite a mensagem de aviso/solicitação: ");
        String mensagem = leitor.nextLine();
        
        String sql = "INSERT INTO mensagens (usuario_email, acao) VALUES (?, ?)";
        
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setString(1, this.getEmail());
            // Insere uma tag visual para o admin identificar facilmente nos logs
            stmt.setString(2, "[MENSAGEM GESTOR] " + mensagem);
            
            stmt.executeUpdate();
            System.out.println("✅ Sucesso: Mensagem enviada para a central de controle!");
            
        } catch (SQLException e) {
            System.out.println("❌ Erro ao enviar mensagem: " + e.getMessage());
        }
    }
    
    // --- OPÇÃO 3: CRIAR MOTORISTA ---
    private String criarMotoristaNivelZero(Scanner leitor) {
        System.out.println("\n--- CADASTRO DE NOVO MOTORISTA ---");
        System.out.println("Atenção: Este usuário terá acesso de Nível 0.");
        
        System.out.print("Nome completo do motorista: ");
        String nome = leitor.nextLine();
        
        System.out.print("Email: ");
        String email = leitor.nextLine();
        
        System.out.print("Login de acesso: ");
        String login = leitor.nextLine();
        
        System.out.print("Senha: ");
        String senha = leitor.nextLine();
        
        try {
            PerfilAcesso perfilMotorista = PerfilAcesso.porNivel(0);
            
            if (UsuarioDAO.cadastrarUsuario(nome, email, login, senha, perfilMotorista)) {
                System.out.println("✅ Sucesso: Motorista '" + nome + "' criado com sucesso!");
                return email; // Retorna o email para que a Opção 1 consiga vincular automaticamente
            }
        } catch (IllegalArgumentException ex) {
            System.out.println("❌ Erro interno ao definir nível de acesso: " + ex.getMessage());
        }
        return ""; // Se falhar, retorna vazio
    }
    
    @Override
    public int getQuantidadeFatores() {
        return 2;
    }
    
    @Override
    public boolean validarFator(String senhaDigitada) {
        return super.getSenha().equals(senhaDigitada); 
    }
}