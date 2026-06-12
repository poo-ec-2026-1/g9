package com.telemetria.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.telemetria.model.Usuario;

public class LogDAO {
    private static void salvarLog(Usuario u, String descricao) {
        String sql = "INSERT INTO logs (usuario_email, acao, data_hora) VALUES (?, ?, ?)";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, u.getLogin()); 
            stmt.setString(2, descricao);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

}
}