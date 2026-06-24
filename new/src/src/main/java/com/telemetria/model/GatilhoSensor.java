 package com.telemetria.model;

public class GatilhoSensor {
    
    private Sensor sensor;
    private int indiceValor;
    private double limiteMaximo; 

    public GatilhoSensor(Sensor sensor, int indiceValor, double limiteMaximo) {
        this.sensor = sensor;
        this.indiceValor = indiceValor;
        this.limiteMaximo = limiteMaximo;
    }

    // =========================================================================
    // GETTERS & SETTERS
    // =========================================================================
    public Sensor getSensor() { 
        return this.sensor; 
    }
    
    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public int getIndiceValor() { 
        return this.indiceValor; 
    }
    
    public void setIndiceValor(int indiceValor) {
        this.indiceValor = indiceValor;
    }

    public double getLimiteMaximo() { 
        return this.limiteMaximo; 
    }
    
    public void setLimiteMaximo(double limiteMaximo) {
        this.limiteMaximo = limiteMaximo;
    }

    // =========================================================================
    // MÉTODOS UTILITÁRIOS
    // =========================================================================
    @Override
    public String toString() {
        String nomeSensor = (sensor != null) ? sensor.getNome() : "Desconhecido";
        return String.format("Gatilho: Alarme se [%s] passar de %.2f", nomeSensor, limiteMaximo);
    }
}