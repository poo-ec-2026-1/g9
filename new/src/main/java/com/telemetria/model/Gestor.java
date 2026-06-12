 package com.telemetria.model;
 
import java.util.List;
import java.util.Scanner;

public class Gestor extends Usuario implements Autenticavel {
    
    public Gestor(String login, String senha, String nome, String email, PerfilAcesso perfil) {
        super(login, senha, nome, email, perfil);
    }
    
    @Override
    public int getQuantidadeFatores() {
        return 2;
    }
    
    @   Override
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
            System.out.println("\n--- PAINEL GESTÃO: " + this.nome.toUpperCase() + " ---");
            System.out.println("Perfil: " + this.perfil.getDescricao());
            System.out.println("-------------------------------------------");
            
            // Opções dinâmicas baseadas em permissões do PerfilAcesso
            if (podeExecutar("VER_FROTA")){
            System.out.println("1. Visualizar Frota Completa");
            }
            if (podeExecutar("MANTER_VEICULO")){
                System.out.println("2. Cadastrar Novo Veículo/Sensor");
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
             if (podeExecutar("")) {
                System.out.println("7. Ver Logs do Sistema");
            }
            if (podeExecutar("LIMPAR_LOGS")) {
                System.out.println("8. Limpar Logs do Sistema");
            }
            System.out.println("0. Sair");
            System.out.print("Escolha uma opção: ");
            
            opcao = leitor.nextInt();
            leitor.nextLine(); // Limpar buffer

            processarEscolha(opcao, leitor);

        } while (opcao != 0);
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
    
    private void processarEscolha(int opcao, Scanner leitor) {
        switch (opcao) {
            case 1:
                if (podeExecutar("VER_FROTA")) verFrotaFiltrada(leitor);
                break;
            case 2:
                if (podeExecutar("MANTER_VEICULO")) System.out.println("Redirecionando para Coleta de Telemetria...");
                // Aqui você chamaria o método processarLeituraSensores()
                break;
            case 3:
                if (podeExecutar("VER_USUARIO")) listarUsuarios(leitor);
                break;
            case 4:
                if (podeExecutar("CRIAR_USUARIO")) cadastrarNovoUsuario(leitor);
                break;
            case 5:
                if (podeExecutar("EDITAR_USUARIO")) editarUsuario(leitor);
                break;
            case 6:
                if (podeExecutar("EXCLUIR_USUARIO")) removerUsuario(leitor);
                break;
            case 7:
                if (podeExecutar("VER_LOGS")) {
                    System.out.println("--- LOGS DO SISTEMA ---");
                    
                    }
                break;
            case 8:
                    if (podeExecutar("LIMPAR_LOGS")) {
                        System.out.print("Deseja realmente limpar todos os logs? (S/N): ");
                        if (leitor.nextLine().equalsIgnoreCase("S")) {

                    System.out.println("Logs excluídos permanentemente.");
                }
                }
                break;
            case 0:
                System.out.println("Saindo do painel...");
                break;
            default:
                System.out.println("Opção inválida ou sem permissão.");
        }
    }

    // --- MÉTODOS DE AÇÃO ---

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
