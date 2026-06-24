 package com.telemetria.model;
 
import java.util.Scanner;

import com.telemetria.repository.UsuarioDAO;


public class Login {
        
    private Scanner scanner = new Scanner(System.in);
    
    public Login() {
        int opcao = -1;
        
        while (opcao != 0) {
            System.out.println("\n ---- MENU PRINCIPAL ----");
            System.out.println("1. Cadastrar Novo Usuário");
            System.out.println("2. Realizar Login");
            System.out.println("0. Sair");
            System.out.print("Escolha: ");
            
            try {
                opcao = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Digite apenas números.");
                continue;
            }
            
            switch (opcao) {
                case 1 -> cadastrarUsuario();
                case 2 -> realizarLogin();
                case 0 -> System.out.println("Saindo....");
                default -> System.out.println("Opção Inválida!");
            }
        }
    }
    
    public void cadastrarUsuario() {
        System.out.println("\n--- NOVO CADASTRO ---");
        
        System.out.print("Nome completo: ");
        String nome = scanner.nextLine();
        
        System.out.print("E-mail (será seu login): ");
        String email = scanner.nextLine();
        
        System.out.print("Senha: ");
        String senha = scanner.nextLine();

        // Salva direto no Banco de Dados usando o DAO!
        boolean sucesso = UsuarioDAO.cadastrarUsuario(nome, email, email, senha, PerfilAcesso.FROTISTA);
        
        if (sucesso) {
            System.out.println("\nUsuário " + nome + " cadastrado com sucesso no banco de dados!");
        } else {
            System.out.println("\nErro: Não foi possível cadastrar. Verifique se o e-mail já existe.");
        }
    }
    
    public void realizarLogin() {
        System.out.println("\n ---- LOGIN ----");
        
        System.out.print("Login (E-mail): ");
        String loginDigitado = scanner.nextLine();
        
        System.out.print("Senha: ");
        String senhaDigitada = scanner.nextLine();
        
        // Busca o usuário direto no Banco de Dados!
        Usuario usuarioLogado = UsuarioDAO.autenticarUsuario(loginDigitado, senhaDigitada);
        
        if (usuarioLogado != null) {
            System.out.println("\nAutenticação realizada com sucesso!");
            
            // Redireciona para o painel restrito
            menuUsuarioLogado(usuarioLogado); 
            
        } else {
            System.out.println("Acesso negado: Usuário ou senha incorretos.");
        }
    }
    
    private void menuUsuarioLogado(Usuario usuarioLogado) {
        int opcao = -1;
        
        while (opcao != 0) {
            System.out.println("\n--- PAINEL DO USUÁRIO ---");
            System.out.println("Olá, " + usuarioLogado.getNome());
            System.out.println("1. Acessar Módulo do Sistema (Telemetria/Gestão)");
            System.out.println("2. Alterar Minha Senha/Nome");
            System.out.println("3. Excluir Minha Conta");
            System.out.println("0. Fazer Logout (Sair)");
            System.out.print("Escolha: ");
            
            try {
                opcao = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Digite apenas números.");
                continue;
            }
            
            switch (opcao) {
                case 1 -> usuarioLogado.acessarSistema(); 
                case 2 -> alterarMeusDados(usuarioLogado);
                case 3 -> {
                    boolean excluiu = excluirMinhaConta(usuarioLogado);
                    if (excluiu) {
                        return; // Se excluiu a conta, força o logout
                    }
                }
                case 0 -> System.out.println("Fazendo logout... Retornando ao Menu Principal.");
                default -> System.out.println("Opção Inválida!");
            }
        }
    }

    private void alterarMeusDados(Usuario usuarioLogado) {
        System.out.println("\n--- ALTERAR MEUS DADOS ---");
        System.out.print("Digite o NOVO Nome: ");
        String novoNome = scanner.nextLine();
        
        System.out.print("Digite a NOVA Senha: ");
        String novaSenha = scanner.nextLine();

        String emailDoUsuario = usuarioLogado.getLogin(); 

        boolean sucesso = UsuarioDAO.atualizarUsuario(usuarioLogado, emailDoUsuario, novoNome, novaSenha, usuarioLogado.getPerfil());
        
        if (sucesso) {
            // Atualiza o objeto na memória para que o painel mostre o novo nome imediatamente
            usuarioLogado.setNome(novoNome);
            usuarioLogado.setSenha(novaSenha);
            System.out.println("Seus dados foram atualizados com sucesso!");
        } else {
            System.out.println("Erro ao atualizar os dados no banco de dados.");
        }
    }

    private boolean excluirMinhaConta(Usuario usuarioLogado) {
        System.out.println("\n--- EXCLUIR MINHA CONTA ---");
        System.out.println("ATENÇÃO: Essa ação é irreversível e apagará todos os seus veículos!");
        System.out.print("Digite sua senha novamente para confirmar: ");
        String senhaConfirmacao = scanner.nextLine();
        
        if (usuarioLogado.autenticar(usuarioLogado.getLogin(), senhaConfirmacao)) {
            
            boolean sucesso = UsuarioDAO.excluirUsuario(usuarioLogado, usuarioLogado.getLogin());
            if (sucesso) {
                System.out.println("Sua conta foi excluída permanentemente. Adeus!");
                return true; 
            } else {
                System.out.println("Ocorreu um erro no banco de dados ao excluir.");
                return false;
            }
        } else {
            System.out.println("Senha incorreta. Exclusão cancelada.");
            return false;
        }
    }
}
