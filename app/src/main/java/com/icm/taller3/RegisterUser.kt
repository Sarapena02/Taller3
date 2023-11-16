package com.icm.taller3

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.icm.taller3.databinding.ActivityRegisterBinding

class RegisterUser : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance()

        val registerButton: Button = binding.button
        val profileImage: ImageView = binding.fotoUsuario

        registerButton.setOnClickListener {
            registerUser()
        }

        profileImage.setOnClickListener {
            // Aquí puedes abrir una galería o una actividad para seleccionar una imagen de perfil
            // y luego manejar la respuesta en onActivityResult
        }

    }
    private fun registerUser() {
        val email = binding.email.text.toString()
        val password = binding.password.text.toString()
        val firstName = binding.name.text.toString()
        val lastName = binding.lastname.text.toString()
        val identificationNumber = binding.identificacion.text.toString()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid

                    // Actualizar el perfil del usuario con el nombre
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName("$firstName $lastName")
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileUpdateTask ->
                            if (profileUpdateTask.isSuccessful) {
                                // Registro exitoso, ahora guarda los datos adicionales en la base de datos
                                saveUserDataToDatabase(userId, firstName,lastName, email, identificationNumber)
                            } else {
                                Toast.makeText(
                                    this@RegisterUser,
                                    "Error al actualizar el perfil del usuario",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Log.e(TAG, "Error al registrar: ${task.exception}")
                    Toast.makeText(
                        this@RegisterUser,
                        "Error al registrar el usuario",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun saveUserDataToDatabase(userId: String?, firstName: String, lastName: String, email: String, identificationNumber: String) {
        val userReference: DatabaseReference = database.child("users").child(userId ?: "")
        val userData = User(firstName, lastName, email, identificationNumber)

        userReference.setValue(userData)
            .addOnCompleteListener { databaseTask ->
                if (databaseTask.isSuccessful) {
                    Toast.makeText(
                        this@RegisterUser,
                        "Registro exitoso",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Aquí puedes redirigir a la actividad principal u otra actividad
                } else {
                    Log.e(TAG, "Error al guardar datos: ${databaseTask.exception}")
                    Toast.makeText(
                        this@RegisterUser,
                        "Error al guardar los datos del usuario",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}
data class User(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val identificationNumber: String = ""
)

