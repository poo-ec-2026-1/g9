package com.telemetria.model;

import java.util.Scanner;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

import com.telemetria.db.ConexaoBanco;
import com.telemetria.repository.GeralDAO;
import com.telemetria.repository.SensorDAO;
import com.telemetria.model.GatilhoSensor;
import com.telemetria.model.Monitoramento;
import com.telemetria.model.Sensor;
import com.telemetria.model.Localizacao;


// Renomeado de Cliente para Motorista para refletir o papel no sistema
public class Motorista extends Usuario implements Autenticavel {
    
    public Motorista(String login, String senha, String nome, String email, PerfilAcesso perfil) {
        // Trava o perfil de segurança como OPERADOR ou MOTORISTA (dependendo do seu Enum)
        super(login, senha, nome, email, perfil); 
    }

    @Override
    public void acessarSistema() {
        Scanner leitor = new Scanner(System.in);
        int opcao;

        do {
            System.out.println("\n--- APLICATIVO DO MOTORISTA: " + this.getNome().toUpperCase() + " ---");
            System.out.println("1. Visualizar Veículo Vinculado");
            System.out.println("2. Ver Sensores do meu Veículo");
            System.out.println("3. Calcular Rota para Destino");
            System.out.println("0. Sair");
            System.out.print("Escolha uma opção: ");
            
            opcao = leitor.nextInt();
            leitor.nextLine(); // Limpar buffer

            switch (opcao) {
                case 1:
                    System.out.println("\n--- MEU VEÍCULO ---");
                    GeralDAO.listarVeiculosPorDono(this.getEmail());
                    break;
                case 2:
                    verTelemetriaDaFrotaPrivada(leitor);
                    break;
                case 3:
                    calcularRota(leitor);
                    break;
                case 4:
                    iniciarViagem(leitor);
                    break;
                case 0:
                    System.out.println("Encerrando aplicativo do motorista...");
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        } while (opcao != 0);
    }
    
    // --- NOVA FUNÇÃO: CÁLCULO DE ROTA ---
    private void calcularRota(Scanner leitor) {
        System.out.print("\nDigite a rua ou endereço de destino: ");
        String destino = leitor.nextLine();

        System.out.println("Calculando rota partindo da base... para: " + destino);
        
        double distanciaEstimada = 5.0 + (Math.random() * 35.0);
        int tempoEstimado = (int) (distanciaEstimada * 2.2);

        System.out.printf("🛣️ Rota traçada! Distância: %.1f km | Tempo: %d min\n", distanciaEstimada, tempoEstimado);
        
        System.out.println("\nComandos Disponíveis:");
        System.out.println("1. INICIAR CORRIDA para este destino");
        System.out.println("2. Cancelar e voltar ao menu");
        System.out.print("Escolha: ");
        
        int escolha = leitor.nextInt();
        leitor.nextLine();

        if (escolha == 1) {
            System.out.println("✅ Corrida iniciada! Direcione-se para " + destino);
            iniciarViagem(leitor);
            
            String sqlStatus = "UPDATE veiculos SET status_viagem = 'EM_CURSO' WHERE id = ?";   
        } else {
            System.out.println("Rota cancelada.");
        }
    }    // ------------------------------------

    // --- NOVA FUNÇÃO: GERADOR E TRANSMISSOR DE GPS ---
   private void iniciarViagem(Scanner leitor) {
        System.out.print("\nDigite a Placa/Serial do veículo que você está conduzindo: ");
        String placa = leitor.nextLine();
        
        System.out.print("Digite o ID Numérico deste veículo (para salvar o GPS no banco): ");
        long idVeiculo = leitor.nextLong();
        leitor.nextLine(); // limpar buffer

        System.out.println("\nLigando motor... Configurando painel de telemetria.");
        
        // 1. Busca os sensores instalados no carro
        List<Sensor> sensoresDoCarro = SensorDAO.buscarSensoresPorVeiculo(placa);
        
        // 2. Configura o Monitoramento
        Veiculo veiculoRodando = new Veiculo(); // Objeto virtual apenas para amarrar os dados
        veiculoRodando.setIdentificador(placa);
        Monitoramento monitor = new Monitoramento(placa, veiculoRodando, null);
        
        // Lista para guardar os simuladores criados para podermos desligá-los no final
        List<SimuladorSensor> simuladoresAtivos = new ArrayList<>();
        
        // 3. Para cada sensor, cria uma regra e inicia uma Thread separada
        for (Sensor s : sensoresDoCarro) {
            // Se o limite máximo for 0, usamos um limite alto fictício. Se houver limite, usamos ele.
            double limiteSeguranca = (s.getLimiteMaximo() > 0) ? s.getLimiteMaximo() : 999.0;
            monitor.adicionarRegra(new GatilhoSensor(s, 0, limiteSeguranca));
            
            SimuladorSensor sim = new SimuladorSensor(s, monitor);
            simuladoresAtivos.add(sim);
            
            Thread t = new Thread(sim);
            t.start(); // Dá a partida no sensor (roda solto em background)
        }

        System.out.println("\nIniciando transmissão de GPS (Simulando 5 leituras)...");

        SensorGeografico gpsApp = new SensorGeografico("GPS Celular Motorista", -16.8225, -49.2671); 
        LocalizacaoDAO daoLocalizacao = new LocalizacaoDAO();

        // 4. Loop do GPS (Roda na Thread principal, segurando a tela)
        for (int i = 1; i <= 5; i++) {
            gpsApp.simularDeslocamentoAleatorio();
            double[] coordenadas = gpsApp.getValores();
            
            double velocidadeSimulada = 40.0 + (Math.random() * 30.0);
            Localizacao locAtual = new Localizacao(coordenadas[0], coordenadas[1], velocidadeSimulada, Instant.now());
            
            daoLocalizacao.salvarLocalizacao(idVeiculo, locAtual);
            System.out.printf("📍 [%d/5] GPS enviado | Vel: %.1f km/h\n", i, velocidadeSimulada);
            
            try {
                Thread.sleep(3000); 
            } catch (InterruptedException e) {
                System.err.println("Erro no rastreamento.");
            }
        }
        
        // 5. Viagem encerrada: Desliga todos os sensores em background
        System.out.println("\nChegando ao destino... Desligando motores e sensores.");
        for (SimuladorSensor sim : simuladoresAtivos) {
            sim.desligarMotor();
        }
        
        System.out.println("🏁 Viagem concluída. Transmissão encerrada.");
    }
    
    private void verTelemetriaDaFrotaPrivada(Scanner leitor) {
        System.out.print("\nDigite a Placa/Serial do SEU veículo para ver os sensores: ");
        String placa = leitor.nextLine();

        String sql = "SELECT s.nome, s.und_medida, s.valor_atual " +
                     "FROM sensores s " +
                     "JOIN veiculos v ON s.veiculo_id = v.id " +
                     "JOIN usuario u ON v.usuario_id = u.id " +
                     "WHERE v.identificador = ? AND u.email = ?";

        try (Connection conn = ConexaoBanco.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, placa);
            stmt.setString(2, this.getEmail()); 

            try (ResultSet rs = stmt.executeQuery()) {
                boolean encontrou = false;
                System.out.println("\n--- SENSORES DO VEÍCULO [" + placa.toUpperCase() + "] ---");
                
                while (rs.next()) {
                    encontrou = true;
                    String nomeSensor = rs.getString("nome");
                    double valor = rs.getDouble("valor_atual");
                    String und = rs.getString("und_medida");
                    
                    System.out.println("↳ " + nomeSensor + ": " + valor + " " + (und != null ? und : ""));
                }
                
                if (!encontrou) {
                    System.out.println("Nenhum sensor encontrado. Verifique se a placa está correta e se o veículo pertence a você.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao carregar telemetria: " + e.getMessage());
        }
    }
    
    @Override
    public int getQuantidadeFatores() {
        return 1;
    }
    
    @Override
    public boolean validarFator(String senhaDigitada) {
        return super.getSenha().equals(senhaDigitada); 
    }
}