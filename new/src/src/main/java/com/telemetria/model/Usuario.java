 package com.telemetria.model;
 
public abstract class Usuario {
    protected String login;
    protected String senha;
    protected String nome;
    protected String email;
    protected PerfilAcesso perfil;

    public Usuario(String login, String senha, String nome, String email, PerfilAcesso perfil) {
        this.login = login;
        this.senha = senha;
        this.nome = nome;
        this.email = email;
        this.perfil = perfil;
    }

    // Método comum para validar credenciais
    public boolean autenticar(String loginDigitado, String senhaDigitada) {
        return this.login.equals(loginDigitado) && this.senha.equals(senhaDigitada);
    }

    
    public abstract void acessarSistema();
    
    public PerfilAcesso getPerfil() {
        return perfil;
    }
    
     public String getEmail() {
        return email;
    }
     
        public String getNome() {
        return nome;
    }
    
    public String getLogin() {
        return login;
    }  
    
    public void setPerfil (PerfilAcesso perfil){
        this.perfil = perfil;
    }

    public String getSenha() {
        return senha;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setLogin(String login) {
        this.login = login;
    }
    
    public void setSenha(String senha) {
        this.senha = senha;
    }   

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean podeExecutar(String tipoAcao) {
        PerfilAcesso perfil = this.getPerfil();
        if (perfil == null) return false;
    
        return perfil.temPermissao(tipoAcao);
    }
    
}
