package com.icm.taller3;

public class User {
    public String nombre;
    public String apellido;
    public String email;
    public String password;
    public String imagenPerfil;
    public String numeroIdentificacion;
    public double latitud;
    public double longitud;


    public User(String nombre, String apellido, String email, String password,
                String imagenPerfil, String numeroIdentificacion, double latitud, double longitud) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.password = password;
        this.imagenPerfil = imagenPerfil;
        this.numeroIdentificacion = numeroIdentificacion;
        this.latitud = latitud;
        this.longitud = longitud;
    }
}

