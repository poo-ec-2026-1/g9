 package com.telemetria.model;
 
import java.time.Instant;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

import com.telemetria.db.ConexaoBanco;


// GPS básico, leitura de localização por meio da lat e long, junto de uma leitura de velocidade.
public record Localizacao(
    double latitude,
    double longitude,
    double velocidadeKmh,
    Instant timestamp // para gravar a hora e a data.
) {
    public String toGoogleMapsUrl() {
        // Correção do formato da URL do Google Maps
        return "https://www.google.com/maps?q=" + latitude + "," + longitude;
    }
}

// Conexão do GPS com o banco de dados
class LocalizacaoDAO {
    
    public void salvarLocalizacao(long dispositivoId, Localizacao loc) {
        String sql = "INSERT INTO localizacao (dispositivo_id, latitude, longitude, velocidade, data_hora) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = ConexaoBanco.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, dispositivoId);
            stmt.setDouble(2, loc.latitude());
            stmt.setDouble(3, loc.longitude());
            stmt.setDouble(4, loc.velocidadeKmh()); 
            stmt.setTimestamp(5, Timestamp.from(loc.timestamp()));
            
            stmt.executeUpdate();
            System.out.println("GPS persistido no MySQL com sucesso.");
            
        } catch (Exception e) {
            // print para casos de erro.
            System.err.println("Erro ao persistir localização: " + e.getMessage());
        }
    }
} 