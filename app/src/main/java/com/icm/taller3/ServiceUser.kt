package com.icm.taller3

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore

class ServiceUser : Service() {
    private lateinit var userRepository: UserRepository

    override fun onCreate() {
        super.onCreate()

        userRepository = UserRepository()

        // Inicia la actualización de la lista de usuarios desde Firestore
        userRepository.updateUsersFromFirestore()

        // Observa cambios en la lista de usuarios
        userRepository.getUserListLiveData().observeForever { userList ->
            userList?.let {
                // Muestra el Toast con el número total de usuarios conectados
                val totalUsers = userList.size
                showTotalUsersToast(totalUsers)
            }
        }
    }

    private fun showTotalUsersToast(totalUsers: Int) {
        Log.d("Toast", "showTotalUsersToast called")
        val message = "Número total de usuarios conectados: $totalUsers"
        showToast(message)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}

class UserRepository {
    private val userListLiveData = MutableLiveData<List<User>>()
    private val userList = mutableListOf<User>()

    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    fun getUserListLiveData(): LiveData<List<User>> {
        return userListLiveData
    }

    fun updateUsersFromFirestore() {
        usersCollection.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                // Manejar el error
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val updatedUsers = snapshot.toObjects(User::class.java)
                userList.clear()
                userList.addAll(updatedUsers)
                userListLiveData.postValue(userList)
            }
        }
    }
}
