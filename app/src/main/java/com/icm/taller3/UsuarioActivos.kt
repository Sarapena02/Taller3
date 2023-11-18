package com.icm.taller3

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UsuarioActivos : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private lateinit var myRef: DatabaseReference
    private lateinit var adapter: UsersAdapter
    companion object{
        lateinit var userTodo:Users
    }
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usuario_activos)

        //Notificación
        startService(Intent(this, ServiceUser::class.java))


        val usersList = mutableListOf<Users>()
        adapter = UsersAdapter(this, R.layout.users_items, usersList)

        val listView: ListView = findViewById(R.id.listaDoctores)
        listView.adapter = adapter

        obtenerIdentificacionUsuarioActual()

        listView.setOnItemClickListener { _, _, position, _ ->
            // Get the clicked doctor's information
            Log.d("errorenposicion", "Item at position $position clicked.")
            val selectedUser = usersList[position]
            userTodo = selectedUser
            // Create an intent to open SolicitarCitaActivity
            val intent = Intent(this, UbicacionUsuario::class.java)

            // Pass any relevant data to the new activity
            intent.putExtra("identificador",selectedUser.identificationNumber)

            startActivity(intent)
        }

    }

    private fun obtenerUsers(id: String) {
        myRef = database.getReference("users")

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val doctorsList = mutableListOf<Users>()

                if (snapshot.exists()) {
                    for (empSnap in snapshot.children) {
                        val empData = empSnap.getValue(Users::class.java)
                        if (empData != null) {
                            if(empData.estado == "Disponible" && empData.identificationNumber != id)
                                doctorsList.add(empData)
                        }
                    }

                    // Actualizar el adaptador con la nueva lista de doctores
                    adapter.clear()
                    adapter.addAll(doctorsList)
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("conseguirDoctores", "Error al obtener doctores", error.toException())
            }
        })
    }

    private fun obtenerIdentificacionUsuarioActual() {
        val user = FirebaseAuth.getInstance().currentUser

        // Verificar si el usuario está autenticado
        if (user != null) {
            val userId = user.uid

            // Obtener la referencia del usuario actual en la base de datos
            val userRef = database.getReference("users").child(userId)

            // Escuchar cambios en los datos del usuario actual
            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userData = snapshot.getValue(Users::class.java)

                    // Verificar si se obtuvieron datos del usuario
                    if (userData != null) {
                        val currentUserIdentificationNumber = userData.identificationNumber
                        // Luego, puedes utilizar currentUserIdentificationNumber como necesites
                        obtenerUsers(currentUserIdentificationNumber)
                    } else {
                        // No se pudieron obtener datos del usuario
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("obtenerIdentificacion", "Error al obtener la identificación del usuario", error.toException())
                }
            })
        } else {
            // El usuario no está autenticado
        }
    }

}