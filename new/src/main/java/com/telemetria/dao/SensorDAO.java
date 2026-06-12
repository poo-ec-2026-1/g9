package com.telemetria.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class SensorDAO {

    public static boolean cadastrarSensorNoBanco(Sensor s, String identificadorVeiculo) {

        String sql = "INSERT INTO sensores (veiculo_id, categoria, nome, und_medida, tipo_dado, valor_atual, limite_maximo) "
                + "VALUES ((SELECT id FROM veiculos WHERE identificador = ?), ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, identificadorVeiculo);
            stmt.setString(2, s.getCategoria());
            stmt.setString(3, s.getNome());
            stmt.setString(4, s.getUndMedida());
            stmt.setString(5, s.getTipoDado());
            stmt.setDouble(6, s.getValor());
            stmt.setDouble(7, s.getLimiteMaximo());

            int linhas = stmt.executeUpdate();
            return linhas > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao cadastrar sensor no banco: " + e.getMessage());
            return false;
        }
    }

    // Lista sensores de um veículo específico para que o Operador saiba qual ID editar
    public static void listarSensoresPorVeiculo(String placa) {
        String sql = "SELECT s.id, s.nome, s.categoria, s.und_medida, s.tipo_dado, s.valor_atual, s.limite_maximo "
                + "FROM sensores s JOIN veiculos v ON s.veiculo_id = v.id "
                + "WHERE v.identificador = ?";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, placa);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\nSensores instalados no veículo " + placa + ":");
            while (rs.next()) {
                System.out.printf("ID: %d | [%s] %s | Unidade: %s | Tipo: %s | Valor: %.2f | Limite: %.2f\n",
                        rs.getInt("id"), rs.getString("categoria"), rs.getString("nome"),
                        rs.getString("und_medida"), rs.getString("tipo_dado"),
                        rs.getDouble("valor_atual"), rs.getDouble("limite_maximo"));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar sensores: " + e.getMessage());
        }
    }

    // Método para atualização completa de todos os campos da tabela sensores
    public static boolean atualizarSensorCompleto(int id, String categoria, String nome, String undMedida, String tipoDado, double limiteMaximo) {
        
        String sql = "UPDATE sensores SET categoria = ?, nome = ?, und_medida = ?, tipo_dado = ?,  limite_maximo = ?, atualizado_em = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categoria);
            stmt.setString(2, nome);
            stmt.setString(3, undMedida);
            stmt.setString(4, tipoDado);
            stmt.setDouble(5, limiteMaximo);
            stmt.setInt(6, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // Método para excluir um sensor permanentemente
    public static boolean excluirSensorNoBanco(int idSensor) {
        String sql = "DELETE FROM sensores WHERE id = ?";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idSensor);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

}