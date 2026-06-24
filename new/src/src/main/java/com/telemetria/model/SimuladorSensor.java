package com.telemetria.model;

import java.util.Random;

public class SimuladorSensor implements Runnable {
    
    private Sensor sensor;
    private Monitoramento monitor;
    private boolean rodando = true;
    private Random gerador = new Random();

    public SimuladorSensor(Sensor sensor, Monitoramento monitor) {
        this.sensor = sensor;
        this.monitor = monitor;
    }

    public void desligarMotor() {
        this.rodando = false;
    }

    @Override
    public void run() {
        System.out.println("▶️ [SENSOR ATIVADO] " + sensor.getNome() + " transmitindo...");
        
        while (rodando) {
            try {
                int chance = gerador.nextInt(100) + 1; 
                
                if (chance <= 15) { 
                    double pico = sensor.getLimiteMaximo() * 1.5; 
                    if (pico == 0) pico = 150.0; 
                    
                    sensor.setValorAtual(pico);
                    System.out.println("\n⚠️ [ANOMALIA INJETADA] Pico de leitura no sensor " + sensor.getNome() + "!");
                    
                } else if (chance <= 30) {
                    double queda = -5.0; 
                    sensor.setValorAtual(queda);
                    System.out.println("\n⚠️ [ANOMALIA INJETADA] Queda brusca no sensor " + sensor.getNome() + "!");
                    
                } else {
                    sensor.simularLeituraAleatoria();
                }
                
                // Envia a leitura (seja ela normal ou uma anomalia) para o painel avaliar
                monitor.processarNovaLeitura(sensor, sensor.getValor());
                
                // Pausa de 3 segundos antes da próxima leitura
                Thread.sleep(3000); 
                
            } catch (InterruptedException e) {
                System.out.println("❌ Transmissão interrompida para: " + sensor.getNome());
                rodando = false;
            }
        }
        System.out.println("🛑 [SENSOR DESATIVADO] " + sensor.getNome());
    }
}