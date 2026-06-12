 package com.telemetria.model;
 
public class Sensor {
    private static int idSensor = 0;
    private int id;
    private String categoria;
    private String nome;
    private String undMedida;
    private String tipoDado;
    private double valorAtual;
    private double limiteMaximo;

    public Sensor(String nome, String undMedida, String tipoDado, double valorAtual, double limiteMaximo) {
        this.nome = nome;
        this.undMedida = undMedida;
        this.tipoDado = tipoDado;
        this.valorAtual = valorAtual;
        this.limiteMaximo = limiteMaximo;
        this.categoria = "Geral"; // Valor padrão
    }
    
    public Sensor(String categoria, String nome, String tipoDado, String undMedida) {
        this.categoria = categoria;
        this.nome = nome;
        this.tipoDado = tipoDado;
        this.undMedida = undMedida;
        this.valorAtual = 0.0;
    }

    public void setValor(double valor) {
        if (this.tipoDado.equals("1/0")) {
            if (valor == 1 || valor == 0) {
                this.valorAtual = valor;
            } else {
                System.out.println("Erro: Sensor binário aceita apenas 0 ou 1.");
            }
        } else {
            this.valorAtual = valor;
            if (valor > limiteMaximo && limiteMaximo > 0) {
                System.out.println("ALERTA: Limite máximo excedido no sensor " + nome);
            }
        }
    }
    
    // Getters e Setters
    public void setId() {
        this.id = idSensor;
        idSensor++;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
    
    public void setNome(String nome) { 
        this.nome = nome; 
    }
    
    public void setUndMedida(String undMedida) { 
        this.undMedida = undMedida; 
    }
    
    public void setTipoDado(String tipoDado) { 
        this.tipoDado = tipoDado; 
    }
    
     public void setLimiteMaximo(double limiteMaximo) { 
        this.limiteMaximo = limiteMaximo; 
    }
    
    public void setValorAtual(double valorAtual){
        this.valorAtual = valorAtual;
    }
    
    public String getCategoria() { 
        return this.categoria;
    }
    
    public int getId() { 
        return this.id; 
    }
    
    public String getNome() { 
        return this.nome;
    }
    
    public String getTipoDado() { 
        return this.tipoDado;
    }
    
    public double getValor() { 
        return this.valorAtual; 
    }
    
    public String getUndMedida(){
        return undMedida;
    }
    
    public double getLimiteMaximo() { 
        return this.limiteMaximo; 
    }
    
 
}
