package com.telemetria.model;

import java.util.Random;

public class SensorGeografico extends Sensor {
    private double latitude;
    private double longitude;
    private static final Random random = new Random();
    
    // Construtor 1: Inicializa com as coordenadas padrão
    public SensorGeografico(String nome) {
        super("Telemetria", nome, "Coordenadas", "Graus Decimais"); 
        
        this.setId(); 
        
        // Coordenadas iniciais para fim de exemplo 
        this.latitude = -16.67744721562849;
        this.longitude = -49.24250895082047;
    }
    
    // Construtor 2: Permite passar as coordenadas logo na criação
    public SensorGeografico(String nome, double latitude, double longitude) {
        super("Telemetria", nome, "Coordenadas", "Graus Decimais");
        this.setId();
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void simularDeslocamentoAleatorio() {
        // 1. Sorteia uma distância de 0 a 200 metros
        double distanciaMetros = random.nextDouble() * 200.0;
        
        // 2. Sorteia uma direção (ângulo de 0 a 360 graus em radianos)
        double angulo = random.nextDouble() * 2 * Math.PI;

        // 3. Converte a distância em metros para graus geográficos (1 grau ≈ 111.32 km)
        double deslocamentoEmGraus = distanciaMetros / 111320.0;

        // 4. Decompõe a distância percorrida nos eixos X e Y
        double deltaLat = deslocamentoEmGraus * Math.cos(angulo);
        double deltaLon = deslocamentoEmGraus * Math.sin(angulo);

        // 5. Aplica o movimento nas coordenadas
        this.latitude += deltaLat;
        this.longitude += deltaLon;
        
        // Salva a última distância percorrida no valorAtual da classe mãe
        this.setValorAtual(distanciaMetros);
    }
    
    public double[] getValores() {
        return new double[] { this.latitude, this.longitude };
    }
    
    public String getLatLong() {
        return "latlong = " + latitude + ", " + longitude;
    }
    
    public void atualizarCoordenadas(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}