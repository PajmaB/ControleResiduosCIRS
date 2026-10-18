package com.example.controlecirs;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class ItemResiduo implements Serializable {
    public String nome;
    public List<Armazenagem> armazenagens;

    public ItemResiduo(String nome) {
        this.nome = nome;
        this.armazenagens = new ArrayList<>();
    }
}