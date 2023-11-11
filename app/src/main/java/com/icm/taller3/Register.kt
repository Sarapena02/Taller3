import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.icm.taller3.R

class Register: AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance()

        val enviarButton: Button = findViewById(R.id.button)
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
                        // Guardar datos adicionales en la base de datos
                        userId?.let { saveUserData(it, nombre, apellido, identificacion) }
                    } else {
                        // Fallo en la creación del usuario
                        // Manejar según sea necesario
                    }
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
