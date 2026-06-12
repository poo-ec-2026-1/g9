 package com.telemetria.model;
 
public class Cliente extends Usuario implements Autenticavel {
    
    public Cliente(String login, String senha, String nome, String email, PerfilAcesso perfil) {
        // Trava o perfil de segurança como CLIENTE
        super(login, senha, nome, email, PerfilAcesso.CLIENTE);
    }

    @Override
    public void acessarSistema() {
        System.out.println("Acesso concedido. Bem-vindo ao painel do Cliente.");
        System.out.println("Privilégio: " + this.perfil.getDescricao() + " (Visualização do próprio veículo).");
    }
    
    @Override
    public int getQuantidadeFatores() {
        return 1;
    }
    
    @Override
    public boolean validarFator(String senhaDigitada) {
        return super.getSenha().equals(senhaDigitada); 
    }
}
