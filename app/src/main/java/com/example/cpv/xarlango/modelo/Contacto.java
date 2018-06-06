package com.example.cpv.xarlango.modelo;

/**
 * Clase de tipo modelo. Contendra las caracteristicas de un contacto telefonico en el servidor
 * de la aplicacion. Sera usado como apoyo para rellenar los arrayList necesarios en las listas
 * de contactos
 */
public class Contacto {
    String nombre; //nombre del usuario
    String numero; //numero del usuario
    String estado; //estado del usuario

    /**
     * Devulve el estado del usuario
     * @return String del estado del usuario
     */
    public String getEstado() {
        return estado;
    }

    /**
     * Establece el estado del usuario que es pasado por parametros
     * @param estado String del estado del usuario
     */
    public void setEstado(String estado) {
        this.estado = estado;
    }

    /**
     * Contructor que inicializa las variables globales de la clase
     * @param nombre nombre del usuario
     * @param numero numero de telefono del usuario
     * @param estado estado del usuario
     */
    public Contacto(String nombre, String numero,String estado){
        this.nombre=nombre;
        this.numero=numero;
        this.estado=estado;
    }

    /**
     * Devuelve el nombre del usuario
     * @return String nombre usuario
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre del usuario pasado por parametros
     * @param nombre nombre del usuario
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Devuelve el numero del usuario
     * @return String del numero del usuario
     */
    public String getNumero() {
        return numero;
    }

    /**
     * Establece el numero del usuario que es pasado por parametros
     * @param numero numero del usuario
     */
    public void setNumero(String numero) {
        this.numero = numero;
    }
}
