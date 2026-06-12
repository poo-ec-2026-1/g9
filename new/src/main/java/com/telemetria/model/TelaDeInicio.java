 package com.telemetria.model;

public class TelaDeInicio {
    
    public TelaDeInicio(){
        System.out.println("Bem-vindo a Telemetria!");
        
        try {
            InicializadorBanco.main(new String[0]);
        } catch (Exception e) {
            System.err.println("Erro ao inicializar o banco: " + e.getMessage());
        }
        
        Login telaLogin = new Login();
        
    }
    
    public class Main {
    public static void main(String[] args) {
        TelaDeInicio inicio = new TelaDeInicio();
    }
    }   
}