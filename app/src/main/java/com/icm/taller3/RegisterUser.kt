package com.icm.taller3

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class RegisterUser : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance()

        val enviarButton: Button = findViewById(R.id.button)
        val galeriaButton: Button = findViewById(R.id.galeria)
        val fotoImageView: ImageView = findViewById(R.id.fotoUsuario)

        enviarButton.setOnClickListener {
            // Obtener valores de los campos
            val nombre = findViewById<EditText>(R.id.name).text.toString()
            val apellido = findViewById<EditText>(R.id.lastname).text.toString()
            val correo = findViewById<EditText>(R.id.email).text.toString()
            val password = findViewById<EditText>(R.id.password).text.toString()
            val identificacion = findViewById<EditText>(R.id.identificacion).text.toString()

            // Crear usuario en Firebase Authentication
            auth.createUserWithEmailAndPassword(correo, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Usuario creado exitosamente
                        val userId = auth.currentUser?.uid

                        // Configurar listener para el botón de la foto
                        fotoImageView.setOnClickListener {
                            // Abrir la galería para seleccionar una imagen
                            val intent = Intent(Intent.ACTION_PICK)
                            intent.type = "image/*"
                            startActivityForResult(intent, PICK_IMAGE_REQUEST)
                        }

                        // Guardar datos adicionales en la base de datos
                        userId?.let { saveUserData(it, nombre, apellido, identificacion) }
                    } else {
                        // Fallo en la creación del usuario
                        // Manejar según sea necesario
                    }
                }
        }

        galeriaButton.setOnClickListener {
            // Abrir la galería para seleccionar una imagen
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
    }

    // Manejar el resultado de la selección de imagen
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val imageUri: Uri = data.data!!
            uploadImageToFirebaseStorage(imageUri)
        }
    }

    // Subir la imagen a Firebase Storage
    private fun uploadImageToFirebaseStorage(imageUri: Uri) {
        val storageReference = storage.reference
        val imageRef = storageReference.child("images/${auth.currentUser?.uid}.jpg")

        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                // Imagen subida exitosamente
                // Aquí puedes obtener la URL de la imagen almacenada y guardarla en la base de datos si es necesario
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    // Ahora puedes guardar imageUrl en la base de datos junto con otros datos del usuario
                    auth.currentUser?.uid?.let { saveUserImage(it, imageUrl) }
                }
            }
            .addOnFailureListener { e ->
                // Fallo al subir la imagen
                // Manejar según sea necesario
            }
    }

    // Guardar la URL de la imagen en la base de datos
    private fun saveUserImage(userId: String, imageUrl: String) {
        database.child("users").child(userId).child("imagen").setValue(imageUrl)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Imagen guardada", Toast.LENGTH_SHORT).show()
                } else {
                    // Fallo al guardar la URL de la imagen
                    Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserData(userId: String, nombre: String, apellido: String, identificacion: String) {
        val userMap = HashMap<String, Any>()
        userMap["nombre"] = nombre
        userMap["apellido"] = apellido
        userMap["identificacion"] = identificacion

        // Guardar datos adicionales en la base de datos
        database.child("users").child(userId).setValue(userMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Datos adicionales guardados exitosamente
                    // Aquí puedes manejar la subida de la imagen al almacenamiento, si es necesario
                } else {
                    // Fallo en la subida de datos adicionales
                    // Manejar según sea necesario
                }
            }
    }
}
