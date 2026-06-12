package com.telemetria.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoBanco {
    
    private static final String URL = "jdbc:sqlite:jhctelemetria.db";

    public static Connection getConnection() throws SQLException {
        try {
            
            return DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.err.println("ERRO CRÍTICO: Não foi possível conectar ao SQLite!");
            throw e; 
        }
    }
}