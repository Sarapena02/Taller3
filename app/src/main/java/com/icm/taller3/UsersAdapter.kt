package com.icm.taller3

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso

class UsersAdapter(context: Context, resource: Int, objects: List<Users>) :
    ArrayAdapter<Users>(context, resource, objects) {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.users_items, parent, false)

        val nombre = view.findViewById<TextView>(R.id.textView10)
        val boton = view.findViewById<TextView>(R.id.textView11)
        val foto = view.findViewById<ImageView>(R.id.doctorImageView)
        val currentUser = getItem(position)
        // Obtén la URL de la foto
        val photoUrl = currentUser?.foto ?: "URL_POR_DEFECTO_SI_LA_URL_ES_NULA"

        Picasso.get()
            .load(photoUrl)
            .rotate(90f)
            .into(foto)

        nombre.text = currentUser?.firstName
        boton.text = currentUser?.lastName

        return view
    }
}