package com.example.controlecirs;

import java.io.Serializable;
public class Armazenagem implements Serializable {
    public int id_residuo;
    public String tipo;
    public int quantidade;
    public String prioridade;

    public Armazenagem(int id_residuo, String tipo, int quantidade, String prioridade) {
        this.id_residuo = id_residuo;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.prioridade = prioridade;
    }
}
