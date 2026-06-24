package com.telemetria.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class InicializadorBanco {

    public static void main(String[] args) {
        
        String sql = """
            CREATE TABLE IF NOT EXISTS usuario (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                email TEXT UNIQUE NOT NULL,
                login TEXT UNIQUE NOT NULL,
                senha TEXT NOT NULL,
                nivel_acesso INTEGER NOT NULL
            );

            CREATE TABLE IF NOT EXISTS veiculos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                usuario_id INTEGER NOT NULL,
                motorista_id INTEGER, -- Adicionado para vínculo de motoristas
                identificador TEXT NOT NULL, -- Placa ou Chassi
                tipo_identificador TEXT,
                tipo_veiculo TEXT,
                ativo INTEGER DEFAULT 1,
                
                -- Cria o relacionamento: um veículo pertence a um gestor
                FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
                -- E pode ser dirigido por um motorista
                FOREIGN KEY (motorista_id) REFERENCES usuario(id) ON DELETE SET NULL
            );

            CREATE TABLE IF NOT EXISTS sensores (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                veiculo_id INTEGER NOT NULL,
                categoria TEXT,
                nome TEXT NOT NULL,
                und_medida TEXT,
                tipo_dado TEXT,
                valor_atual REAL,
                limite_maximo REAL,
                atualizado_em DATETIME DEFAULT CURRENT_TIMESTAMP,
                
                -- Cria o relacionamento: um sensor pertence a um veículo
                FOREIGN KEY (veiculo_id) REFERENCES veiculos(id) ON DELETE CASCADE
            );

            CREATE TABLE IF NOT EXISTS logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                usuario_email TEXT,
                acao TEXT NOT NULL,
                data_hora DATETIME DEFAULT CURRENT_TIMESTAMP
            );
            
            CREATE TABLE IF NOT EXISTS localizacao (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                dispositivo_id INTEGER NOT NULL, -- Referência ao ID do Veículo/Equipamento
                latitude REAL NOT NULL,
                longitude REAL NOT NULL,
                velocidade REAL,
                data_hora DATETIME,
                
                -- Cria o relacionamento: o histórico de GPS pertence a um veículo
                FOREIGN KEY (dispositivo_id) REFERENCES veiculos(id) ON DELETE CASCADE
            );
            
            -- NOVA TABELA: Caixa de mensagens entre usuários e a Central
            CREATE TABLE IF NOT EXISTS mensagens (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                remetente_email TEXT NOT NULL,
                conteudo TEXT NOT NULL,
                data_hora DATETIME DEFAULT CURRENT_TIMESTAMP
            );
        """;
        
        try (Connection conn = ConexaoBanco.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(sql);
            System.out.println("✅ Banco de dados e tabelas criados com sucesso no SQLite!");
            System.out.println("O arquivo 'jhctelemetria.db' já deve estar na pasta do seu projeto.");

        } catch (SQLException e) {
            System.err.println("❌ Erro ao criar o banco de dados: " + e.getMessage());
        }
    }
}