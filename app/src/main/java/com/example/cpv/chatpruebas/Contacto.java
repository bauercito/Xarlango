package com.example.cpv.chatpruebas;

public class Contacto {
    String nombre;
    String numero;
    public Contacto(String nombre,String numero){
        this.nombre=nombre;
        this.numero=numero;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }
}
