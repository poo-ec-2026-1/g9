package com.telemetria.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LogDAO {
    
    public static void salvarLog(Usuario u, String descricao) {
        
        String sql = "INSERT INTO logs (usuario_email, acao) VALUES (?, ?)";
        
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, u.getLogin()); 
            stmt.setString(2, descricao);
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Erro ao salvar log: " + e.getMessage());
        }
    }
}
