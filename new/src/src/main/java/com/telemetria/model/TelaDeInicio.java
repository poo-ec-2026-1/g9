 package com.telemetria.model;

import com.telemetria.repository.GeralDAO;
import com.telemetria.model.SensorGeografico;
import com.telemetria.db.InicializadorBanco;
import com.telemetria.model.Login;


public class TelaDeInicio {
        public TelaDeInicio(){
            System.out.println("Bem-vindo a Telemetria!");
        
            try {
                InicializadorBanco.main(new String[0]);
            } catch (Exception e) {
                System.err.println("Erro ao inicializar o banco: " + e.getMessage());
            }
            
            Login telaLogin = new Login();
        
        }
    
        public class Main {
            public static void main(String[] args) {
            TelaDeInicio inicio = new TelaDeInicio();
            
            //Atualiza os dados geograficos a cada 10 segundos
            SensorGeografico gpsSensor = new SensorGeografico("GPS Frontal", -16.6774, -49.2425);
            LocalizacaoDAO dao = new LocalizacaoDAO();
            
            long idVeiculo = 1001L;
                ProcessadorTelemetria processador = new ProcessadorTelemetria(idVeiculo, gpsSensor, dao);
    
            System.out.println("Iniciando monitoramento de telemetria...");
            for (int i = 0; i < 5; i++) {
                processador.processarCiclo();
                
                try {
                    Thread.sleep(10000); // Aguarda 10 segundos até a próxima leitura
                } catch (InterruptedException e) {
                    System.err.println("Erro na thread de monitoramento.");
                    }
                }
            }
        }   
    
}