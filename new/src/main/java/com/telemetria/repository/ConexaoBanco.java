package com.telemetria.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoBanco {

    // URL do PostgreSQL
    private static final String URL =
            "jdbc:postgresql://localhost:5432/jhctelemetria";

    // Usuário do PostgreSQL
    private static final String USUARIO = "postgres";

    // Senha definida na instalação
    private static final String SENHA = "135246Adm";

    public static Connection getConnection() throws SQLException {

        try {

            // Driver PostgreSQL
            Class.forName("org.postgresql.Driver");

            return DriverManager.getConnection(
                    URL,
                    USUARIO,
                    SENHA
            );

        } catch (ClassNotFoundException e) {

            System.err.println(
                    "Driver PostgreSQL não encontrado!"
            );

            throw new SQLException(e);

        } catch (SQLException e) {

            System.err.println(
                    "ERRO CRÍTICO: Não foi possível conectar ao PostgreSQL!"
            );

            throw e;
        }
    }
}