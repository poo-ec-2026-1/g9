 package com.telemetria.model;
 
public enum PerfilAcesso {
    CLIENTE(0, "Usuário Cliente"),
    FROTISTA(1, "Gestor de Frota"),
    OPERADOR(2, "Operador de Telemetria"),
    ADMIN(3, "Administrador do Sistema");

    private final int nivel;
    private final String descricao;

    // Construtor privado (padrão de Enums)
    private PerfilAcesso(int nivel, String descricao) {
        this.nivel = nivel;
        this.descricao = descricao;
    }

    // Getters
    public int getNivel() {
        return nivel;
    }

    public String getDescricao() {
        return descricao;
    }

    public static PerfilAcesso porNivel(int nivel) {
        for (PerfilAcesso perfil : PerfilAcesso.values()) {
            if (perfil.getNivel() == nivel) {
                return perfil;
            }
        }
        throw new IllegalArgumentException("Nível de acesso inválido: " + nivel);
    }
   
    public boolean temPermissao(String acao) {
        switch (acao) {
            case "EXCLUIR_USUARIO":
                return this == ADMIN || this == OPERADOR;
            
            case "VER_USUARIO":
                return this == ADMIN || this == OPERADOR;    
                
            case "CRIAR_USUARIO":
                return this == ADMIN || this == OPERADOR;

                
            case "EDITAR_USUARIO":
                return this == ADMIN || this == OPERADOR;

            
            case "MANTER_VEICULO":
                return this == ADMIN || this == OPERADOR || this == FROTISTA;
            
            case "VER_SENSOR":
                return this == ADMIN || this == OPERADOR;
                
            case "ADICIONAR_SENSOR":
                return this == ADMIN || this == OPERADOR;
            
            case "EXCLUIR_SENSOR":
                return this == ADMIN || this == OPERADOR;
            
            case "VER_FROTA":
            case "VER_DADOS_TELEMETRIA":
                return this.nivel >= 1; 
                
            case "VER_LOGS":
                return this == ADMIN;

            case "LIMPAR_LOGS":
                return this == ADMIN;
                
           
            default:
                return false;
        }
    }
}
