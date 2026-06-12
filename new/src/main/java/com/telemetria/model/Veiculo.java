 package com.telemetria.model;

import java.util.ArrayList;
import java.util.List;


public class Veiculo {
    private int id;
    private String tipoVeiculo;
    private String identificador; 
    private String tipoIdentificador;
    
    // Utilização da classe Localizacao para uso do GPS.
    private Localizacao localizacao;
    
    
    private List<Sensor> configuracao = new ArrayList<>();
    public static int qtdInstancia = 0;
    
    public void exibirAviso(String msg) {
        System.out.println("[VEÍCULO] Painel aceso: " + msg);
    }
    
    public Veiculo (int id, String identificador, String tipoIdentificador, Localizacao loc){
        this.id = id;
        this.identificador = identificador;
        this.tipoIdentificador = tipoIdentificador;
        this.localizacao = loc;
    }

    public String getIdentificador() { 
        return identificador; 
    }

        public String getTipoIdentificador() { 
        return tipoIdentificador; 
    }

    public String getTipoVeiculo() { 
        return tipoVeiculo;
    }
    // Métodos delegando a responsabilidade para o objeto localizacao
    public double getLatitude()  { 
        return localizacao.latitude(); 
    }
    
    public double getLongitude() { 
        return localizacao.longitude();
    }
    
    public Localizacao getLocalizacao() {
        return localizacao; 
    }

    public void setLocalizacao(Localizacao loc) {
        this.localizacao = loc;
        }

    public int getId() { 
        return id; 
    }
    
    //Comportamento do Sensor.
    public void setSensor(Sensor s) { 
        this.configuracao.add(s);
    }
    
    public List<Sensor> getConfiguracao() {
    return this.configuracao;
    }
    
    public void adicionarSensor(Sensor s) { 
    this.configuracao.add(s);
    }
    
    public void exibirStatusSensores() {
        System.out.println("\t--- Status Atual do Veículo: " + this.identificador + " ---");
        if(configuracao.isEmpty()) System.out.println("\t\tNenhum sensor instalado.");
        for (Sensor s : configuracao) {
            System.out.println("\t\tSensor: " + s.getId() + "-" + s.getNome() + " | Valor: " + s.getValor());
        }
    }


}
