import java.util.ArrayList;
import java.util.List; 

public class Monitoramento{
    
    private double limiteMaximo;
    private double valorAtual;
    private String identificador;
    private List<Sensor> configuracao = new ArrayList<>();
    
    private Central central;
    private Veiculo veiculo;
    
    
    public void setLimiteMaximo(double limite){
        this.limiteMaximo = limite;
        }
    
    public boolean temErro(){
        return this.valorAtual > this.limiteMaximo;
    }
    
    
    public void exibirStatusSensores() {
        System.out.println("\t--- Status Atual do Veículo: " + this.identificador + " ---");
        if(configuracao.isEmpty()) System.out.println("\t\tNenhum sensor instalado.");
        for (Sensor s : configuracao) {
            System.out.println("\t\tSensor: " + s.getId() + "-" + s.getNome() + " | Valor: " + s.getValor());
        }
    }
    
    public void Sensor(String nomeS, double valorRecebido){
        this.valorAtual = valorRecebido;
    if(temErro()){
        String mensagem = "Sensor " +nomeS +"acima do limite!";
        
        central.receberAlerta(mensagem, this.veiculo);
        veiculo.exibirAviso(mensagem);
        }
    }
}
