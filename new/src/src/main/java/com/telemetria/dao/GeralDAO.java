package com.telemetria.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List; 

import com.telemetria.model.Localizacao;
import com.telemetria.model.PerfilAcesso;
import com.telemetria.model.Sensor;
import com.telemetria.model.Usuario;
import com.telemetria.model.Veiculo;
import com.telemetria.db.ConexaoBanco;

public class GeralDAO {

    public List<Veiculo> listarFrotaCompleta() {
        List<Veiculo> lista = new ArrayList<>();
        String sqlVeic = "SELECT * FROM veiculos";
        String sqlSens = "SELECT * FROM sensores WHERE veiculo_id = ?";

        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement stmtV = conn.prepareStatement(sqlVeic);
             ResultSet rsV = stmtV.executeQuery()) {

            while (rsV.next()) {
                int id = rsV.getInt("id");
                Localizacao loc = null;
                
                Veiculo v = new Veiculo(
                    id, 
                    rsV.getString("identificador"), 
                    rsV.getString("tipo_identificador"), 
                    loc
                );

                // Carrega os sensores para este veículo específico
                try (PreparedStatement stmtS = conn.prepareStatement(sqlSens)) {
                    stmtS.setInt(1, id);
                    try (ResultSet rsS = stmtS.executeQuery()) {
                        while (rsS.next()) {
                            v.adicionarSensor(new Sensor(
                                rsS.getString("nome"),
                                rsS.getString("und_medida"),
                                rsS.getString("tipo_dado"),
                                rsS.getDouble("valor_atual"),
                                rsS.getDouble("limite_maximo")
                            ));
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                
                lista.add(v); // Adiciona o veículo após carregar todos os seus sensores
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Método para listar veículos vinculados a um e-mail específico
    public static void listarVeiculosPorDono(String emailDono) {
        String sql = "SELECT v.identificador, v.tipo_veiculo FROM veiculos v " +
                     "JOIN usuario u ON v.usuario_id = u.id WHERE u.email = ?";

        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, emailDono);
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("Veículos encontrados para este cliente:");
                boolean encontrou = false;
                while (rs.next()) {
                    encontrou = true;
                    System.out.println("- Placa: " + rs.getString("identificador") +
                                       " | Tipo: " + rs.getString("tipo_veiculo"));
                }
                if (!encontrou) {
                    System.out.println("Nenhum veículo cadastrado para este e-mail.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar veículos por dono: " + e.getMessage());
        }
    }

    public static void listarClientesEVeiculos() {
        String sql = "SELECT u.nome AS cliente, v.identificador, v.tipo_veiculo, " +
                     "s.nome AS sensor_nome, s.tipo_dado " +
                     "FROM usuario u " +
                     "LEFT JOIN veiculos v ON u.id = v.usuario_id " +
                     "LEFT JOIN sensores s ON v.id = s.veiculo_id " +
                     "WHERE u.nivel_acesso = 0 " +
                     "ORDER BY u.nome, v.identificador";

        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("\n--- RELATÓRIO DE FROTA POR CLIENTE ---");
            System.out.printf("%-20s | %-15s | %-15s\n", "Cliente", "Placa/ID", "Tipo");
            System.out.println("----------------------------------------------------------");

            while (rs.next()) {
                String cliente = rs.getString("cliente");
                String placa = rs.getString("identificador");
                String tipo = rs.getString("tipo_veiculo");

                if (placa == null) {
                    placa = "(Sem veículo)";
                    tipo = "-";
                }

                System.out.printf("%-20s | %-15s | %-15s\n", cliente, placa, tipo);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao gerar relatório: " + e.getMessage());
        }
    }

    //Método para que o usuário autorizado possa ver frotas, criar usuários operadores e possa alterar veículos.
    public static void executarAcao(Usuario autor, String tipoAcao, Object alvo) {
        if (!autor.podeExecutar(tipoAcao)) {
            System.out.println("ACESSO NEGADO: " + autor.getEmail() + " não tem permissão para " + tipoAcao);
            return;
        }

        switch (tipoAcao) {
            case "MANTER_VEICULO":
                salvarLog(autor, "Realizou manutenção em: " + alvo.toString());
                break;
            case "CRIAR_OPERADOR":
                if (autor.getPerfil() == PerfilAcesso.ADMIN) {
                    salvarLog(autor, "Criou novo usuário operador");
                }
                break;
            case "VER_FROTA":
                break;
        }
    }

    public static PerfilAcesso porNivel(int nivel) {
        for (PerfilAcesso perfil : PerfilAcesso.values()) {
            if (perfil.getNivel() == nivel) {
                return perfil;
            }
        }
        throw new IllegalArgumentException("Nível de acesso inválido: " + nivel);
    }

    // Método auxiliar para evitar erro de compilação em executarAcao
    private static void salvarLog(Usuario autor, String mensagem) {
        System.out.println("LOG: " + autor.getNome() + " - " + mensagem);
    }
}