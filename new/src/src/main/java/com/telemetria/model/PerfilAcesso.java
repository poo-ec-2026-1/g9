package com.telemetria.model;
 
public enum PerfilAcesso {
    MOTORISTA(0, "Motorista"),
    FROTISTA(1, "Gestor de Frota"),
    OPERADOR(2, "Operador de Telemetria"),
    ADMIN(3, "Administrador do Sistema");

    private final int nivel;
    private final String descricao;


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
            // Ações exclusivas da Central de Controle 
            case "EXCLUIR_USUARIO":
            case "VER_USUARIO":
            case "EDITAR_USUARIO":
            case "VER_SENSOR":
            case "ADICIONAR_SENSOR":
            case "EXCLUIR_SENSOR":
                return this == ADMIN || this == OPERADOR;
                
            // Ações críticas de Auditoria
            case "VER_LOGS":
            case "LIMPAR_LOGS":
                return this == ADMIN;

            
            case "CRIAR_USUARIO":
            case "MANTER_VEICULO":
                return this == ADMIN || this == OPERADOR || this == FROTISTA;
            
        
            case "VER_FROTA":
            case "VER_DADOS_TELEMETRIA":
                return this.nivel >= 0; 
                
            // Ações exclusivas do FROTISTA
            case "MENSAGEM_CENTRAL":
                return this == FROTISTA;
                
            // Ações exclusivas do aplicativo de bordo do Motorista
            case "INICIAR_VIAGEM":
            case "CALCULAR_ROTA":
                return this == MOTORISTA || this == FROTISTA; 
            
            default:
                return false;
        }
    }
}