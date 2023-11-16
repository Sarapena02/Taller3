package com.icm.taller3

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity

class UsuarioActivos : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usuario_activos)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_usuario, menu)
        return true
    }
}