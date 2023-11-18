package com.icm.taller3

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset

data class Location(val latitude: Double, val longitude: Double, val name: String)
class MapaUsuario : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var mLocationOverlay: MyLocationNewOverlay

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)

        // Solicitar permisos de ubicación en tiempo de ejecución
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        }

        // Configuración de OpenStreetMap
        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE))

        // Configuración de la barra de herramientas
        val toolbar: Toolbar = findViewById(R.id.toolbarMenu)
        setSupportActionBar(toolbar)

        // Inicialización del mapa
        mapView = findViewById(R.id.mapView)
        mapView.tileProvider.tileSource = TileSourceFactory.MAPNIK
        mapView.setMultiTouchControls(true)

        mLocationOverlay = addMyLocationOverlay()

        // Añadir marcador de ubicación actual del usuario
        addMyLocationOverlay()

        val locationsArray = readJsonFromAssets(this, "locations.json")

        Log.d("lo","$locationsArray")
        if (locationsArray != null) {
            // Puedes acceder a los elementos del arreglo así:
            for (location in locationsArray) {
                Log.d("punto","Nombre: ${location.name}, Latitud: ${location.latitude}, Longitud: ${location.longitude}")
            }
        }

        // Añadir marcadores de puntos de interés
        addPointsOfInterestMarkers(locationsArray)

        // Centrar la vista en la ubicación del usuario
        centerMapToUserLocation()
    }

    private fun readJsonFromAssets(context: Context, fileName: String): Array<Location>? {
        var json: String? = null
        try {
            val inputStream: InputStream = context.assets.open(fileName)
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, Charset.defaultCharset())
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        val gson = Gson()
        val locations = gson.fromJson(json, Map::class.java)["locations"] as Map<String, Map<String, Any>>

        return locations.map { (_, value) ->
            gson.fromJson(gson.toJsonTree(value), Location::class.java)
        }.toTypedArray()
    }

    private fun addMyLocationOverlay(): MyLocationNewOverlay {
        val locationProvider = GpsMyLocationProvider(this)
        val overlay = MyLocationNewOverlay(locationProvider, mapView)
        overlay.enableMyLocation()
        overlay.enableFollowLocation() // Habilita el seguimiento de la ubicación
        mapView.overlays.add(overlay)
        mapView.controller.setZoom(13.0)
        return overlay
    }

    private fun addPointsOfInterestMarkers(locations: Array<Location>?) {
        
        try {

            if (locations != null) {
                for (location in locations) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val name = location.name

                    // Crear un marcador en la ubicación del punto de interés
                    val point = GeoPoint(latitude, longitude)
                    val marker = Marker(mapView)
                    marker.position = point
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = name

                    // Añadir el marcador al mapa
                    mapView.overlays.add(marker)
                }
            }

            // Forzar la actualización del mapa
            mapView.invalidate()

        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }


    private fun centerMapToUserLocation() {
        // Asegurar que la ubicación del usuario no sea nula
        val userLocation: GeoPoint? = mLocationOverlay.myLocation
        if (userLocation != null) {
            val mapController: IMapController = mapView.controller
            mapController.setCenter(userLocation)
            mapController.animateTo(userLocation)
            mapController.setZoom(15.0)
        }
    }

    //Menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_usuario, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item1 -> {
                val intent = Intent(this, inicioSesion::class.java)
                startActivity(intent)
            }
            R.id.item2 -> {
                showStatusDialog()
            }
            else -> {
                val intent1 = Intent(this, UsuarioActivos::class.java)
                startActivity(intent1)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun showStatusDialog() {
        val statusOptions = arrayOf("Disponible", "Desconectado")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona tu estado")
            .setItems(statusOptions, DialogInterface.OnClickListener { _, which ->
                // 'which' indica la posición del elemento seleccionado en el array
                when (which) {
                    0 -> {
                        // Opción: Disponible
                        // Aquí puedes poner la lógica para cuando el usuario está disponible
                    }
                    1 -> {
                        // Opción: Desconectado
                        // Aquí puedes poner la lógica para cuando el usuario está desconectado
                    }
                }
            })

        builder.create().show()
    }
}
