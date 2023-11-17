package com.icm.taller3

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.icm.taller3.databinding.ActivityRegisterBinding
import kotlin.random.Random
import androidx.appcompat.app.AlertDialog
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date


class RegisterUser : AppCompatActivity() {

    private val GALLERY_REQUEST_CODE = 1
    private val CAMERA_REQUEST_CODE = 2
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var camerapath: Uri
    private lateinit var URLimagen: String




    private val cameraRequest = registerForActivityResult(ActivityResultContracts.TakePicture()
    ) {
            succes:Boolean-> if (succes){
                loadImage(camerapath)
            }
      }

    private val GalleryRequest = registerForActivityResult(ActivityResultContracts.GetContent()
    ) { uri:Uri? -> if (uri!= null){
            loadImage(uri)
        }
      }

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

        //permiso de ubicacion
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
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
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST
                )
            }
        }

        //permiso de storage
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Si no, solicitar al usuario
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                GALLERY_REQUEST_CODE
            )
        }

        val openGalleryButton = findViewById<Button>(R.id.galeria)
        openGalleryButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                GalleryRequest.launch("image/*") // Launch GalleryRequest
            } else {
                // Request permission if not granted
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    GALLERY_REQUEST_CODE
                )
            }
        }

        // Verifica si tienes permiso para acceder a la cámara
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Si no, solicitar al usuario
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE
            )
        }

        val openCameraButton = findViewById<Button>(R.id.camara)
        openCameraButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                initializeFile()
                cameraRequest.launch(camerapath)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PermissionRequestCodes.CAMERA)
            }

        }



        val registerButton: Button = binding.button
        registerButton.setOnClickListener {
            registerUser(lat,lon,URLimagen)
        }

    }

    fun loadImage(imagepath:Uri?){
        val imagestream = contentResolver.openInputStream(imagepath!!)
        val image = BitmapFactory.decodeStream(imagestream)
        binding.fotoUsuario.setImageBitmap(image)

        uploadImageToFirebaseStorage(imagepath) { urlString ->
            // La URL de descarga está disponible aquí como una cadena (puede ser nula en caso de fallo)
            if (urlString != null) {
                URLimagen = urlString
                // Realiza cualquier acción adicional con la URL de descarga como cadena
            } else {
                Log.e("Upload", "Error uploading image")
                URLimagen = ""
            }
        }

    }

    fun initializeFile() {

        val imageFileName: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        try {
            val imageFile = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
            )

            // Save the file path for use with ACTION_VIEW intents
            camerapath = FileProvider.getUriForFile(
                this,
                applicationContext.packageName + ".fileprovider",
                imageFile
            )
            Log.d("inicialize","${camerapath}");
        } catch (e: IOException) {
            e.printStackTrace()
            // Handle the error, show a toast, or log the exception
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
    private fun registerUser(latitud: String,longitud: String,imagen: String) {
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
                                saveUserDataToDatabase(userId, firstName,lastName, email, identificationNumber,latitud,longitud,imagen)
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

    private fun uploadImageToFirebaseStorage(imageUri: Uri, callback: (String?) -> Unit) {

        var storageReference = storage.reference
        val storageRef = storageReference.child("image1"+ Random.nextInt(1, 101))

        storageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                // Image uploaded successfully, you can get the download URL
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    // Now you can use 'uri' to store the download URL in your database or perform any other action
                    Log.d("Upload", "Image uploaded successfully. Download URL: $uri")
                    callback(uri.toString())
                }
            }
            .addOnFailureListener { exception ->
                // Handle unsuccessful uploads
                Log.e("Upload", "Error uploading image", exception)
                callback(null)
            }
    }

    private fun saveUserDataToDatabase(userId: String?, firstName: String, lastName: String, email: String, identificationNumber: String,latitud: String,longitud: String,foto: String) {
        val userReference: DatabaseReference = database.child("users").child(userId ?: "")
        val userData = User(firstName, lastName, email, identificationNumber,latitud,longitud,foto)

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
    val firstName: String? = "",
    val lastName: String? = "",
    val email: String? = "",
    val identificationNumber: String? = "",
    val longitud: String? = "",
    val latitud: String? = "",
    val foto: String? = "",
    val estado: String? = "Disponible"
)

