 package com.telemetria.model;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

import com.telemetria.repository.GeralDAO;
import com.telemetria.repository.SensorDAO;
import com.telemetria.repository.UsuarioDAO;
import com.telemetria.repository.VeiculoDAO;


public class Operador extends Usuario implements Autenticavel {
    
    private List<Veiculo> frotaLocal = new ArrayList<>();

    public Operador(String login, String senha, String nome, String email, PerfilAcesso perfil) {

        super(login, senha, nome, email, PerfilAcesso.OPERADOR);
    }

    // --- MÉTODOS DE AUTENTICAÇÃO ---

    @Override
    public int getQuantidadeFatores() { 
        return 2; 
    }

    @Override
    public boolean validarFator(String senhaDigitada) {
        return super.getSenha().equals(senhaDigitada); 
    }

    @Override
    public boolean validarFator(String senhaDigitada, String token) {
        return token.equals("000000"); // Simulação de token para o operador
    }

    @Override
    public boolean validarFator(String senha, String token, String biometria) {
        return biometria.equals("Digital ok");
    }

    // --- INTERFACE DE SISTEMA ---

    @Override
    public void acessarSistema() {
        Scanner leitor = new Scanner(System.in);
        int opcao;

        do {
            System.out.println("\n--- TERMINAL DE CAMPO: " + this.nome.toUpperCase() + " ---");
            System.out.println("Perfil: " + this.perfil.getDescricao());
            System.out.println("-------------------------------------------");
            
            // Opções baseadas no Enum PerfilAcesso
            if (podeExecutar("VER_FROTA")){
                System.out.println("1. Visualizar Frota ");
            }
            if (podeExecutar("MANTER_VEICULO")){
                System.out.println("2. Iniciar Coleta de Telemetria (Sensores)");
            }
            if (podeExecutar("CRIAR_USUARIO")){
                System.out.println("3. Adicionar Novo Usuário");
            }
            if (podeExecutar("EDITAR_USUARIO")){
                System.out.println("4. Editar Dados de Usuário");
            }
            if (podeExecutar("EXCLUIR_USUARIO")) {
                System.out.println("5. Excluir Usuário");
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
            case 1:
                if (podeExecutar("VER_FROTA")) GeralDAO.listarClientesEVeiculos();
                break;
            case 2:
                if (podeExecutar("MANTER_VEICULO")) processarLeituraSensores(leitor);
                break;
            case 3:
                if (podeExecutar("CRIAR_USUARIO")) cadastrarNovoUsuario(leitor);
                break;
            case 4:
                if (podeExecutar("EDITAR_USUARIO")) editarUsuario(leitor);
                break;
            case 5:
                if (podeExecutar("EXCLUIR_USUARIO")) removerUsuario(leitor);
                break;
            case 0:
                System.out.println("Saindo do terminal de campo...");
                break;
            default:
                System.out.println("Opção inválida ou sem permissão.");
        }
    }
    
    private void listarUsuarios(Scanner leitor) {
        System.out.println("\n--- LISTA DE USUÁRIOS CADASTRADOS ---");
    
   
        List<Usuario> usuarios = UsuarioDAO.listarUsuarios(this.perfil);

        if (usuarios.isEmpty()) {
            System.out.println("Nenhum usuário encontrado no sistema.");
        } else {
        
            System.out.printf("%-15s | %-20s | %-25s | %-15s\n", "Login", "Nome", "Email", "Perfil");
            System.out.println("---------------------------------------------------------------------------------");
        
            for (Usuario u : usuarios) {
                System.out.printf("%-15s | %-20s | %-25s | %-15s\n", 
                    u.getLogin(), 
                    u.getNome(), 
                    u.getEmail(), 
                    u.getPerfil().getDescricao());
            }
        }
        System.out.println("\nPressione Enter para voltar ao menu...");
        leitor.nextLine(); 
    }

     private void editarUsuario(Scanner leitor) {
        System.out.println("\n--- MÓDULO DE GESTÃO COMPLETA (OPERADOR) ---");
        System.out.print("Digite o e-mail do cliente: ");
        String emailAlvo = leitor.nextLine();

        System.out.println("\nO que deseja gerenciar?");
        System.out.println("1. Dados Pessoais");
        System.out.println("2. Veículos");
        System.out.println("3. Sensores");
        System.out.println("0. Sair");
        System.out.print("Escolha: ");
        int escolha = Integer.parseInt(leitor.nextLine());
    
        switch (escolha) {
            case 1 -> editarDadosPessoais(leitor, emailAlvo);
            case 2 -> editarDadosVeiculo(leitor, emailAlvo);
            case 3 -> menuGerenciarSensores(leitor);
            case 0 -> System.out.println("Saindo...");
        }
    }

        private void editarDadosPessoais(Scanner leitor, String emailAlvo) {
        System.out.println("\n--- Editando Dados Pessoais ---");
        System.out.print("Novo Nome: ");
        String novoNome = leitor.nextLine();
        System.out.print("Nova Senha: ");
        String novaSenha = leitor.nextLine();
    
        boolean sucesso = UsuarioDAO.atualizarUsuario(this, emailAlvo, novoNome, novaSenha, PerfilAcesso.FROTISTA);
        
        if (sucesso) System.out.println("✅ Dados atualizados com sucesso!");
        else System.out.println("❌ Erro ao atualizar ou permissão negada.");
    }
    
    private void editarDadosVeiculo(Scanner leitor, String emailAlvo) {
        System.out.println("\n--- Editando Veículo do Cliente ---");
        GeralDAO.listarVeiculosPorDono(emailAlvo);
    
        System.out.print("\nPlaca Atual do veículo: ");
        String placaAntiga = leitor.nextLine();
        System.out.print("Nova Placa: ");
        String novaPlaca = leitor.nextLine();
        System.out.print("Novo Tipo (ex: Caminhão): ");
        String novoTipo = leitor.nextLine();
    
        if (VeiculoDAO.atualizarVeiculoNoBanco(placaAntiga, novaPlaca, novoTipo, emailAlvo)) {
            System.out.println("✅ Veículo atualizado!");
        } else {
            System.out.println("❌ Falha na atualização do veículo.");
        }
    }
    
    private void menuGerenciarSensores(Scanner leitor) {
        System.out.print("\nDigite a PLACA do veículo para gerenciar os sensores: ");
        String placa = leitor.nextLine();
    
   
        SensorDAO.listarSensoresPorVeiculo(placa);

        System.out.println("\nAções:");
        System.out.println("1. Adicionar Novo");
        System.out.println("2. Atualizar Existente");
        System.out.println(" 3. Excluir");
        System.out.println("0. Voltar");
        int acao = Integer.parseInt(leitor.nextLine());

        switch (acao) {
            case 1 -> {
            System.out.print("Categoria: "); String cat = leitor.nextLine();
            System.out.print("Nome: "); String nome = leitor.nextLine();
            System.out.print("Unidade Medida: "); String und = leitor.nextLine();
            System.out.print("Tipo Dado: "); String tipo = leitor.nextLine();
            System.out.print("Limite Máximo: "); double limite = Double.parseDouble(leitor.nextLine());
            
            Sensor s = new Sensor(cat, nome, tipo, und);
            s.setLimiteMaximo(limite);
            if(SensorDAO.cadastrarSensorNoBanco(s, placa)) System.out.println("✅ Adicionado!");
            }
         case 2 -> {
            System.out.print("ID do sensor: ");
            int id = Integer.parseInt(leitor.nextLine());
            
            System.out.print("Nova Categoria: "); String cat = leitor.nextLine();
            System.out.print("Novo Nome: "); String nome = leitor.nextLine();
            System.out.print("Nova Unidade: "); String und = leitor.nextLine();
            System.out.print("Novo Tipo Dado: "); String tipo = leitor.nextLine();
            System.out.print("Novo Limite Máximo: "); double limite = Double.parseDouble(leitor.nextLine());
        
            if(SensorDAO.atualizarSensorCompleto(id, cat, nome, und, tipo, limite)) {
                System.out.println("✅ Atualizado com sucesso!");
            }
        }
            case 3 -> {
            System.out.print("ID do sensor para excluir: ");
            int id = Integer.parseInt(leitor.nextLine());
            if(SensorDAO.excluirSensorNoBanco(id)) System.out.println("✅ Excluído!");
            }
        }
    }
    
    public void processarLeituraSensores(Scanner leitor) {
        String continuarVeiculo;
        System.out.println("\n---- Iniciando Coleta de Dados de Telemetria ----");

        do {
            System.out.print("\nIdentificador do veículo (ex: ABC-1234): ");
            String idPlaca = leitor.nextLine();
            System.out.print("Tipo (ex: Placa/Chassi): ");
            String tipo = leitor.nextLine();
            
            Localizacao locAtual = new Localizacao(-1.0, -21.34, 0.0, java.time.Instant.now());  
            Veiculo v = new Veiculo(0, idPlaca, tipo, locAtual);

            String continuarSensor;
            do {
                System.out.println("\n--- Adicionando Sensor ao Veículo " + idPlaca + " ---");
                System.out.print("Categoria: "); String cat = leitor.nextLine();
                System.out.print("Nome do Sensor: "); String nomeS = leitor.nextLine();
                System.out.print("Tipo de dado: "); String tipoD = leitor.nextLine();
                System.out.print("Unidade: "); String und = leitor.nextLine();
                
                System.out.print("Limite Máximo: ");
                double limite = leitor.nextDouble();
                leitor.nextLine(); // Limpa o buffer

                Sensor s = new Sensor(cat, nomeS, tipoD, und);
                s.setLimiteMaximo(limite);
                v.setSensor(s);

                System.out.print("Cadastrar outro SENSOR? (S/N): ");
                continuarSensor = leitor.nextLine();
            } while (continuarSensor.equalsIgnoreCase("S"));

            frotaLocal.add(v);
            System.out.print("\nCadastrar outro VEÍCULO na sessão local? (S/N): ");
            continuarVeiculo = leitor.nextLine();

        } while (continuarVeiculo.equalsIgnoreCase("S"));

        exibirRelatorioSessao();
    }

    private void exibirRelatorioSessao() {
        System.out.println("\n---- Relatório da Sessão de Coleta ----");
        for (Veiculo veic : frotaLocal) {
            veic.exibirStatusSensores();
        }
    }

    // --- MÉTODOS DE APOIO (Delegando ao GeralDAO) ---

    private void verFrotaNoBanco() {
        GeralDAO dao = new GeralDAO();
        dao.listarFrotaCompleta().forEach(Veiculo::exibirStatusSensores);
    }

    private void cadastrarNovoUsuario(Scanner leitor) {
        System.out.println("Encaminhando para cadastro...");
    }


    private void removerUsuario(Scanner leitor) {
    }
}
