package com.telemetria.model;

import com.telemetria.model.PerfilAcesso;

public class Instalador extends Usuario implements Autenticavel {
    
    public Instalador(String login, String senha, String nome, String email, PerfilAcesso perfil) {
        super(login, senha, nome, email, perfil);
    }
    
    @Override
    public void acessarSistema() {
        System.out.println("Bem-vindo ao Painel de controle!");
        
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