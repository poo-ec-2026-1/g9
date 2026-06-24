package com.telemetria.model;

import java.util.ArrayList;
import java.util.List; 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.telemetria.db.ConexaoBanco;
import com.telemetria.model.Veiculo;


public class Central {
    
    private String nomeUnidade;
    private List<Veiculo> veiculosEmEmergencia;
    
    public Central (String nomeUnidade){
        this.nomeUnidade = nomeUnidade;
        this.veiculosEmEmergencia = new ArrayList<>();
    }
        
    // =========================================================================
    // GERENCIAMENTO DE EMERGÊNCIAS
    // =========================================================================

    public void receberAlerta(String msg, Veiculo v) {
        System.out.println("\n🚨 ALERTA RECEBIDO NA CENTRAL [" + nomeUnidade + "]: " + msg);
        
        if (!veiculosEmEmergencia.contains(v)){
            veiculosEmEmergencia.add(v);
        }
        
        // Proteção contra NullPointerException caso o veículo ainda não tenha gerado GPS
        if (v.getLocalizacao() != null) {
            System.out.println("📍 Localização do veículo: Lat " + v.getLatitude() + " | Lon " + v.getLongitude());
        } else {
            System.out.println("📍 Localização do veículo: Aguardando sinal de GPS...");
        }
    }

    public void listarEmergenciasAtivas(){
        System.out.println("\n--- 🔴 EMERGÊNCIAS ATIVAS ---");
        if (veiculosEmEmergencia.isEmpty()) {
            System.out.println("Nenhum veículo em estado de emergência no momento.");
            return;
        }
        
        for (Veiculo v : veiculosEmEmergencia) {
            System.out.println("- Veículo [" + v.getTipoIdentificador() + "]: " + v.getIdentificador());
        }
    }

    // =========================================================================
    // COMUNICAÇÃO COM FROTISTAS
    // =========================================================================

    /**
     * Acessa o banco de dados e busca todas as mensagens enviadas pelos Frotistas (Clientes).
     * Funciona como uma "Caixa de Entrada" para a equipe de Operadores/Admin.
     */
    public void lerMensagensFrotistas() {
        System.out.println("\n--- 📥 CAIXA DE ENTRADA: MENSAGENS DE FROTISTAS ---");
        
        // Busca apenas os logs que começam com a tag que definimos na classe Cliente
        String sql = "SELECT usuario_email, acao, data_hora FROM logs " +
                     "WHERE acao LIKE '[MENSAGEM GESTOR]%' ORDER BY data_hora DESC";
                     
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
             
            boolean temMensagem = false;
            
            while (rs.next()) {
                temMensagem = true;
                String email = rs.getString("usuario_email");
                String mensagemCompleta = rs.getString("acao");
                String dataHora = rs.getString("data_hora");
                
                // Limpa a tag visual "[MENSAGEM GESTOR] " para a leitura ficar mais elegante
                String mensagemLimpa = mensagemCompleta.replace("[MENSAGEM GESTOR] ", "");
                
                System.out.println("📅 " + dataHora);
                System.out.println("👤 De: " + email);
                System.out.println("💬 Mensagem: " + mensagemLimpa);
                System.out.println("---------------------------------------------------");
            }
            
            if (!temMensagem) {
                System.out.println("Nenhuma mensagem recebida dos frotistas até o momento.");
            }
             
        } catch (SQLException e) {
            System.out.println("❌ Erro ao acessar a caixa de entrada: " + e.getMessage());
        }
    }
}