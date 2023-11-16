package com.icm.taller3

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
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
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        var lat = ""
        var lon = ""
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // Obtener ubicación actual
                getLastLocation { location ->
                    location?.let { (latitude, longitude) ->
                        lat = latitude.toString()
                        lon = longitude.toString()
                    }
                }
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST
                )
            }
        }


        val registerButton: Button = binding.button
        val profileImage: ImageView = binding.fotoUsuario

        registerButton.setOnClickListener {
            registerUser(lat,lon)
        }

        profileImage.setOnClickListener {
            // Aquí puedes abrir una galería o una actividad para seleccionar una imagen de perfil
            // y luego manejar la respuesta en onActivityResult
        }

    }

    private fun getLastLocation(callback: (Pair<Double, Double>?) -> Unit) {
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                return
            }
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val latitude = it.latitude
                        val longitude = it.longitude
                        callback.invoke(Pair(latitude, longitude))
                    } ?: run {
                        callback.invoke(null)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error al obtener la ubicación: $e")
                    Toast.makeText(
                        this@RegisterUser,
                        "Error al obtener la ubicación actual",
                        Toast.LENGTH_SHORT
                    ).show()
                    callback.invoke(null)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener la ubicación: $e")
            Toast.makeText(
                this@RegisterUser,
                "Error al obtener la ubicación actual",
                Toast.LENGTH_SHORT
            ).show()
            callback.invoke(null)
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 123
    }
    private fun registerUser(latitud: String,longitud: String) {
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
                                saveUserDataToDatabase(userId, firstName,lastName, email, identificationNumber,latitud,longitud)
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

    private fun saveUserDataToDatabase(userId: String?, firstName: String, lastName: String, email: String, identificationNumber: String,latitud: String,longitud: String) {
        val userReference: DatabaseReference = database.child("users").child(userId ?: "")
        val userData = User(firstName, lastName, email, identificationNumber,latitud,longitud)

        userReference.setValue(userData)
            .addOnCompleteListener { databaseTask ->
                if (databaseTask.isSuccessful) {
                    Toast.makeText(
                        this@RegisterUser,
                        "Registro exitoso",
                        Toast.LENGTH_SHORT
                    ).show()
                    val mapa = Intent(this,MapaUsuario::class.java)
                    startActivity(mapa)

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
    val identificationNumber: String = "",
    val longitud: String = "",
    val latitud: String = ""
)

