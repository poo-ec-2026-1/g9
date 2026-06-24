package com.telemetria.model;

import java.util.ArrayList;
import java.util.List;

public class Monitoramento {
    
    private String identificador;
    private Central central;
    private Veiculo veiculo;
    private List<GatilhoSensor> regrasAtivas = new ArrayList<>();
    
    public Monitoramento(String identificador, Veiculo veiculo, Central central) {
        this.identificador = identificador;
        this.veiculo = veiculo;
        this.central = central;
    }
    
    public void adicionarRegra(GatilhoSensor gatilho) {
        this.regrasAtivas.add(gatilho);
    }
    
    public void processarNovaLeitura(Sensor sensorLido, double valorRecebido) {
        sensorLido.setValorAtual(valorRecebido);
        
        for (GatilhoSensor regra : regrasAtivas) {
            if (regra.getSensor().equals(sensorLido)) {
                
                // Avalia o gatilho usando a lógica direta de limite máximo
                if (avaliarGatilho(regra, valorRecebido)) {
                    dispararAlarme(regra, valorRecebido);
                }
            }
        }
    }
    
    public boolean avaliarGatilho(GatilhoSensor regra, double valorRecebido) {
        return valorRecebido > regra.getLimiteMaximo();
    }
    
    private void dispararAlarme(GatilhoSensor regraQuebrada, double valorDetectado) {
        String nomeSensor = regraQuebrada.getSensor().getNome();
        double limite = regraQuebrada.getLimiteMaximo();
        
        String mensagem = String.format("ALERTA CRÍTICO: Sensor [%s] detectou %.2f (Limite máximo de %.2f violado)", 
                                        nomeSensor, valorDetectado, limite);
        
        if (central != null) {
            central.receberAlerta(mensagem, this.veiculo);
        }
        if (veiculo != null) {
            veiculo.exibirAviso(mensagem);
        }
    }
    
    public void exibirStatusRegras() {
        System.out.println("\t--- Painel de Monitoramento do Veículo: " + this.identificador + " ---");
        if(regrasAtivas.isEmpty()) {
            System.out.println("\t\t⚠️ Nenhuma regra de segurança configurada.");
        } else {
            System.out.println("\t\tRegras ativas no sistema de bordo:");
            for (GatilhoSensor r : regrasAtivas) {
                System.out.println("\t\t- " + r.getSensor().getNome() + " | Alarme dispara se passar de: " + r.getLimiteMaximo());
            }
        }
    }

    public List<GatilhoSensor> getRegrasAtivas() { 
        return regrasAtivas; 
    }
    public Veiculo getVeiculo() { 
        return veiculo;
    }
}