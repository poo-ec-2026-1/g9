  package com.telemetria.model;
 
 public interface Autenticavel {
    
    int getQuantidadeFatores();

    default boolean validarFator(String senha) {
        return true; 
    }

    default boolean validarFator(String senha, String token) {
        return true & true;
    }

    default boolean validarFator(String senha, String token, String biometria) {
        return true & true & true;
    }
}
