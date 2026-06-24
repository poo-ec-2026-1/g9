 package com.telemetria.model;

import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Representa um evento de alerta que aconteceu no histórico do sistema.
 * Registra o momento de início, fim e calcula a duração do alerta.
 */
public class RegistroAlerta {
    
    private GatilhoSensor gatilho; // CORREÇÃO: Usa o novo GatilhoSensor
    private Veiculo veiculo;       // CORREÇÃO: Salva de qual veículo veio o alerta
    private LocalDateTime horarioInicio;
    private LocalDateTime horarioFim;
    private boolean ativo;

    public RegistroAlerta(GatilhoSensor gatilho, Veiculo veiculo) {
        this.gatilho = gatilho;
        this.veiculo = veiculo;
        this.horarioInicio = LocalDateTime.now(); 
        this.ativo = true; 
    }

    public void encerrarAlerta() {
        this.horarioFim = LocalDateTime.now();
        this.ativo = false;
    }

    public String getDuracaoFormatada() {
        LocalDateTime fim = (this.ativo) ? LocalDateTime.now() : this.horarioFim;
        Duration duracao = Duration.between(horarioInicio, fim);
        
        long segundos = duracao.toSeconds();
        if (segundos < 60) {
            return segundos + " segundos";
        }
        return duracao.toMinutes() + " minutos e " + (segundos % 60) + " segundos";
    }

    // GETTERS
    public GatilhoSensor getGatilho() { return gatilho; }
    public Veiculo getVeiculo() { return veiculo; }
    public LocalDateTime getHorarioInicio() { return horarioInicio; }
    public LocalDateTime getHorarioFim() { return horarioFim; }
    public boolean isAtivo() { return ativo; }
}