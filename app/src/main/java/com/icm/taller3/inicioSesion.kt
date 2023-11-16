package com.icm.taller3

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.icm.taller3.databinding.ActivityInicioSesionBinding

class inicioSesion : AppCompatActivity() {

    private lateinit var binding: ActivityInicioSesionBinding
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInicioSesionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        val iniciarSesion = binding.button
        val registrar = binding.button2

        iniciarSesion.setOnClickListener{
            iniciarSesion(binding.editTextText.text.toString(),binding.editTextTextPassword.text.toString())
        }

        registrar.setOnClickListener{
            val registro = Intent(this,RegisterUser::class.java)
            startActivity(registro)
        }
    }
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }
    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            val userName = currentUser.displayName
            if (userName != null) {
                // User is a regular user, navigate to User Dashboard
                val intent = Intent(this, MapaUsuario::class.java)
                intent.putExtra("user", currentUser.email)
                intent.putExtra("username", currentUser.displayName)
                startActivity(intent)
            }
        } else {
            binding.editTextText.setText("")
            binding.editTextTextPassword.setText("")
        }
    }

    private fun validateForm(): Boolean {
        var valid = true
        val email = binding.editTextText.text.toString()
        if (TextUtils.isEmpty(email)) {
            binding.editTextText.error = "Required."
            valid = false
        } else {
            binding.editTextText.error = null
        }
        val password = binding.editTextTextPassword.text.toString()
        if (TextUtils.isEmpty(password)) {
            binding.editTextTextPassword.error = "Required."
            valid = false
        } else {
            binding.editTextTextPassword.error = null
        }
        return valid
    }

    private fun isEmailValid(email: String): Boolean {
        if (!email.contains("@") ||
            !email.contains(".") ||
            email.length < 5)
            return false
        return true
    }

    private fun iniciarSesion(email: String, password: String){
        if(validateForm() && isEmailValid(email)){
            auth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI
                        val user = auth.currentUser
                        updateUI(auth.currentUser)
                    } else {
                        Toast.makeText(this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }
                }
        }
    }

    companion object{
        private const val  TAG = "EmailPassword"
    }
}