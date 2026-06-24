package com.telemetria.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.telemetria.model.Usuario;
import com.telemetria.db.ConexaoBanco;

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

    public static void lerLogsBanco() {
        String sql = "SELECT * FROM logs ORDER BY data_hora DESC LIMIT 50";
        
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
             
            System.out.println("\n--- ÚLTIMOS LOGS DO SISTEMA ---");
            boolean temLog = false;
            
            while(rs.next()) {
                temLog = true;
                System.out.println(rs.getTimestamp("data_hora") + " | " + 
                                   rs.getString("autor") + " | " + 
                                   rs.getString("mensagem"));
            }
            
            if (!temLog) System.out.println("Nenhum log registrado ainda.");
            System.out.println("-------------------------------");
             
        } catch (SQLException e) {
            // Se a tabela logs não existir, avisa sem travar o sistema
            System.out.println("Aviso: Tabela de logs indisponível ou vazia no banco de dados.");
        }
    }
}
