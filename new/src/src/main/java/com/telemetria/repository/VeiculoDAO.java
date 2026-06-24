package com.telemetria.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.telemetria.model.Sensor;
import com.telemetria.model.Veiculo;
import com.telemetria.db.ConexaoBanco;

public class VeiculoDAO {

    public static boolean cadastrarVeiculoNoBanco(Veiculo v, String emailProprietario) {
       
        String sql = "INSERT INTO veiculos " +
                     "(usuario_id, identificador, tipo_identificador, tipo_veiculo, ativo) " +
                     "VALUES ((SELECT id FROM usuario WHERE email = ?), ?, ?, ?, 1)";

        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, emailProprietario);
            stmt.setString(2, v.getIdentificador());
            stmt.setString(3, v.getTipoIdentificador());
            stmt.setString(4, v.getTipoVeiculo()); 

            int linhas = stmt.executeUpdate();
            return linhas > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao cadastrar veículo: " + e.getMessage());
            return false;
        }
    }

    public boolean salvarVeiculoCompleto(Veiculo v, String emailDono) {
        
        String sqlVeic = "INSERT INTO veiculos " +
                         "(usuario_id, identificador, tipo_identificador, tipo_veiculo, ativo) " +
                         "VALUES ((SELECT id FROM usuario WHERE email = ?), ?, ?, ?, 1)";

        String sqlSens = "INSERT INTO sensores (veiculo_id, categoria, nome, und_medida, tipo_dado, valor_atual, limite_maximo) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;

        try {
            conn = ConexaoBanco.getConnection();
            
            conn.setAutoCommit(false);

            int idGerado = -1;
            // Salva o veículo e pede ao banco para retornar o ID gerado automaticamente
            try (PreparedStatement stV = conn.prepareStatement(sqlVeic, Statement.RETURN_GENERATED_KEYS)) {
                stV.setString(1, emailDono);
                stV.setString(2, v.getIdentificador());
                stV.setString(3, v.getTipoIdentificador());
                stV.setString(4, v.getTipoVeiculo());

                stV.executeUpdate();

                // Recupera o ID gerado para o veículo
                try (ResultSet rs = stV.getGeneratedKeys()) {
                    if (rs.next()) idGerado = rs.getInt(1);
                }
            }

            // Se o veículo foi salvo com sucesso, salva os sensores vinculados a ele
            if (idGerado != -1) {
                try (PreparedStatement stS = conn.prepareStatement(sqlSens)) {
                    for (Sensor s : v.getConfiguracao()) {
                        stS.setInt(1, idGerado);
                        stS.setString(2, "Geral");
                        stS.setString(3, s.getNome());
                        stS.setString(4, s.getUndMedida());
                        stS.setString(5, s.getTipoDado());
                        stS.setDouble(6, s.getValor());
                        stS.setDouble(7, s.getLimiteMaximo());
                        stS.addBatch(); // Adiciona à fila de execução em lote
                    }
                    stS.executeBatch(); 
                }
            }

            // Confirma a transação (salva tudo de vez)
            conn.commit();
            return true;

        } catch (SQLException e) {
            // Em caso de erro, desfaz tudo o que foi feito nesta transação (Rollback)
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            System.err.println("Erro na Transação: " + e.getMessage());
            return false;
        } finally {
            // Fecha a conexão independentemente de sucesso ou falha
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public static boolean atualizarVeiculoNoBanco(String placaAntiga, String novaPlaca, String novoTipo, String emailDono) {
        String sql = "UPDATE veiculos SET identificador = ?, tipo_veiculo = ? " +
                     "WHERE identificador = ? AND usuario_id = (SELECT id FROM usuario WHERE email = ?)";

        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, novaPlaca);
            stmt.setString(2, novoTipo);
            stmt.setString(3, placaAntiga);
            stmt.setString(4, emailDono);

            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar veículo: " + e.getMessage());
            return false;
        }
    }
}
