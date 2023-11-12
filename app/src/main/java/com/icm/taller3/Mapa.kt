package com.icm.taller3

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset

data class Ubicacion(val latitud: Double, val longitud: Double, val nombre: String)

class Mapa : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var mapController: IMapController
    private lateinit var menuLayout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)

        // Configuración de OSM
        Configuration.getInstance().load(applicationContext, getPreferences(MODE_PRIVATE))

        // Configuración del mapa
        mapView = findViewById(R.id.mapView)
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)

        mapController = mapView.controller

        // Cargar ubicaciones desde el archivo JSON
        val ubicaciones = loadLocationsFromJson()

        // Mostrar marcadores en el mapa para cada ubicación
        for (ubicacion in ubicaciones) {
            val geoPoint = GeoPoint(ubicacion.latitud, ubicacion.longitud)
            val marker = Marker(mapView)
            marker.position = geoPoint
            marker.title = ubicacion.nombre
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            mapView.overlays.add(marker)
        }

        // Centrar el mapa en la primera ubicación (ajusta según tus necesidades)
        if (ubicaciones.isNotEmpty()) {
            mapController.setCenter(GeoPoint(ubicaciones[0].latitud, ubicaciones[0].longitud))
            mapController.setZoom(15.0)
        }

        menuLayout = findViewById(R.id.menuLayout)
        val imageViewMenu = findViewById<ImageView>(R.id.mapView)

        imageViewMenu.setOnClickListener {
            toggleMenu()
        }
    }

    private fun toggleMenu() {
        if (menuLayout.visibility == View.VISIBLE) {
            menuLayout.visibility = View.GONE
        } else {
            menuLayout.visibility = View.VISIBLE
        }
    }
/*
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_sign_out -> {
                // Acción al hacer clic en "Cerrar Sesión"
                FirebaseAuth.getInstance().signOut()
                toggleMenu()
                // Puedes redirigir a la pantalla de inicio de sesión u otra acción necesaria
                return true
            }
            R.id.menu_set_available -> {
                // Acción al hacer clic en "Establecer Disponible"
                // Ejemplo: setAvailability(true)
                toggleMenu()
                return true
            }
            R.id.menu_set_disconnected -> {
                // Acción al hacer clic en "Establecer Desconectado"
                // Ejemplo: setAvailability(false)
                toggleMenu()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }*/

    private fun loadLocationsFromJson(): List<Ubicacion> {
        val json: String = loadJsonFromAsset("ubicaciones.json") ?: return emptyList()

        val listType = object : TypeToken<List<Ubicacion>>() {}.type
        return Gson().fromJson(json, listType)
    }

    private fun loadJsonFromAsset(fileName: String): String? {
        var json: String? = null
        try {
            val inputStream: InputStream = assets.open(fileName)
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, Charset.defaultCharset())
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return json
    }
}
