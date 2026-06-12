package com.telemetria.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class UsuarioDAO {

    public static boolean cadastrarUsuario(String nome, String email, String login, String senha, PerfilAcesso Nivel) {
        String sql = "INSERT INTO usuario (nome, email, login, senha, nivel_acesso) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nome);
            stmt.setString(2, email);
            stmt.setString(3, login);
            stmt.setString(4, senha);
            stmt.setInt(5, Nivel.getNivel());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro Usuario: " + e.getMessage());
            return false;
        }
    }

    public static Usuario autenticarUsuario(String loginOuEmail, String senha) {
        String sql = "SELECT id, nome, email, login, senha, nivel_acesso FROM usuario WHERE (login = ? OR email = ?) AND senha = ?";
        
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, loginOuEmail);
            stmt.setString(2, loginOuEmail);
            stmt.setString(3, senha);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String nome = rs.getString("nome");
                    String emailDb = rs.getString("email");
                    String loginDb = rs.getString("login");
                    String senhaDb = rs.getString("senha");
                    int nivel = rs.getInt("nivel_acesso");
                
                    PerfilAcesso perfil = PerfilAcesso.porNivel(nivel);

                    switch (perfil) {
                        case ADMIN: 
                            return new Equipe(loginDb, senhaDb, nome, emailDb, perfil);
                        case OPERADOR: 
                            return new Operador(loginDb, senhaDb, nome, emailDb, perfil); 
                        case FROTISTA:
                            return new Gestor(loginDb, senhaDb, nome, emailDb, perfil);
                        default: 
                            return new Cliente(loginDb, senhaDb, nome, emailDb, perfil);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro na autenticação: " + e.getMessage());
        }
        return null;
    }

    public static boolean atualizarUsuario(Usuario autor, String emailAlvo, String novoNome, String novaSenha, PerfilAcesso nivel) {
        boolean isAdmin = autor.getPerfil() == PerfilAcesso.ADMIN;
        boolean isProprioUsuario = autor.getEmail().equals(emailAlvo);

        if (!isAdmin && !isProprioUsuario) {
            System.out.println("ACESSO NEGADO: Você não tem permissão para alterar os dados de outra pessoa.");
            return false;
        }

        String sql = "UPDATE usuario SET nome = ?, senha = ?, nivel_acesso = ? WHERE email = ?";

        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, novoNome);
            stmt.setString(2, novaSenha);
            stmt.setInt(3, nivel.getNivel());
            stmt.setString(4, emailAlvo);
            
            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas > 0) {
                if (isAdmin && !isProprioUsuario) {
                    salvarLog(autor, "Admin alterou os dados do usuário: " + emailAlvo);
                } else {
                    salvarLog(autor, "Atualizou o próprio perfil.");
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar usuário: " + e.getMessage());
        }
        return false;
    }
    
    public static boolean excluirUsuario(Usuario autor, String emailAlvo){
        boolean isAdmin = autor.getPerfil() == PerfilAcesso.ADMIN;
        boolean isProprioUsuario = autor.getEmail().equals(emailAlvo);
        
        if (!isAdmin && !isProprioUsuario){
            System.err.println("ACESSO NEGADO: Sem permissão necessária!");
            return false;
        }
        
        String sql = "DELETE FROM usuario WHERE email = ?";
        
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){
            
            stmt.setString(1, emailAlvo);
            int linhasAfetadas = stmt.executeUpdate();
            
            if (linhasAfetadas > 0){
                if (isAdmin && !isProprioUsuario){
                    salvarLog(autor, "ADMIN Excluiu a conta: " + emailAlvo);
                    System.out.println("Sua Conta do Usuario " + emailAlvo + " excluida pelo Administrador");
                } else {
                    salvarLog(autor, "Excluiu a própria conta");
                    System.out.println("Sua Conta de Usuario foi excluída com sucesso");
                }
                return true;
            } else {
                System.out.println("Conta de usuário não encontrada");
            }
            
        } catch (SQLException e){
            System.err.println("Erro ao tentar excluir a conta: " + e.getMessage());
        }
        return false;
    }
        
    public static List<Usuario> listarUsuarios(PerfilAcesso perfilSolicitante) {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuario";
        
        if(perfilSolicitante == PerfilAcesso.OPERADOR){
            sql += " WHERE nivel_acesso = 0"; 
        }

        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String login = rs.getString("login");
                String senha = rs.getString("senha");
                String nome = rs.getString("nome");
                String email = rs.getString("email");
                int nivel = rs.getInt("nivel_acesso"); 
            
                PerfilAcesso perfil = PerfilAcesso.porNivel(nivel);
            
                Usuario u;
                if (perfil == PerfilAcesso.ADMIN || perfil == PerfilAcesso.OPERADOR) {
                    u = new Equipe(login, senha, nome, email, perfil);
                } else {
                    u = new Cliente(login, senha, nome, email, perfil);
                }
            
                usuarios.add(u);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar usuários: " + e.getMessage());
        }
        return usuarios;
    }

    private static void salvarLog(Usuario autor, String acao) {
        System.out.println("LOG [" + autor.getEmail() + "]: " + acao);
        
        String sql = "INSERT INTO logs (usuario_email, acao) VALUES (?, ?)";
        
        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, autor.getEmail());
            stmt.setString(2, acao);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao salvar log: " + e.getMessage());
        }
    }
}