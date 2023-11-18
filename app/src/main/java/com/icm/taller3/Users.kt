package com.icm.taller3

data class Users(
    val firstName: String,
    val lastName: String,
    val identificationNumber: String,
    val longitud: String,
    val latitud: String,
    val foto: String,
    val estado: String
) {

    // Constructor sin argumentos necesario para Firebase


    constructor() : this("", "", "", "","","","")


    // Constructor que selecciona una imagen aleatoria


}