package com.telemetria.model;

import java.time.Instant;

public class ProcessadorTelemetria {
    private SensorGeografico sensor;
    private LocalizacaoDAO dao;
    private long dispositivoId;

    // Injeção de dependência: o processador recebe o sensor e o DAO prontos
    public ProcessadorTelemetria(long dispositivoId, SensorGeografico sensor, LocalizacaoDAO dao) {
        this.dispositivoId = dispositivoId;
        this.sensor = sensor;
        this.dao = dao;
    }

    // Método que executa o ciclo de leitura e gravação
    public void processarCiclo() {
        // 1. O sensor lê ou simula a nova posição
        sensor.simularDeslocamentoAleatorio();

        // 2. Extrai as coordenadas atuais do sensor
        double[] coordenadas = sensor.getValores();
        double latitude = coordenadas[0];
        double longitude = coordenadas[1];

        // 3. Define a velocidade (aqui você pode integrar um sensor de velocidade no futuro)
        double velocidadeSimulada = 60.0; 

        // 4. Empacota os dados no record Localizacao com o Timestamp exato deste momento
        Localizacao locAtual = new Localizacao(
            latitude, 
            longitude, 
            velocidadeSimulada, 
            Instant.now()
        );

        // 5. Envia para o banco de dados
        dao.salvarLocalizacao(dispositivoId, locAtual);
    }
}