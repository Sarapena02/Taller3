package com.icm.taller3

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import kotlin.math.log


class MapaUsuario : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var mapController: IMapController
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private val MY_PERMISSIONS_REQUEST_LOCATION = 1
    private lateinit var auth : FirebaseAuth


    data class Ubicacion(val latitude: Double, val longitude: Double, val name: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)

        auth = Firebase.auth

        val toolbar: Toolbar = findViewById(R.id.toolbarMenu)
        setSupportActionBar(toolbar)

        // Configuración de OSM
        Configuration.getInstance().load(applicationContext, getPreferences(MODE_PRIVATE))

        // Configuración del mapa
        mapView = findViewById(R.id.mapView)
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)

        mapController = mapView.controller

        // Verificación y solicitud de permisos de ubicación
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Si ya se tienen permisos, configurar la capa de ubicación
            setupMyLocationOverlay()
        } else {
            // Si no se tienen permisos, solicitarlos al usuario
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), MY_PERMISSIONS_REQUEST_LOCATION)
        }

        // Cargar ubicaciones desde el archivo JSON
        val ubicaciones = loadLocationsFromJson()

        // Mostrar marcadores en el mapa para cada ubicación
        for (ubicacion in ubicaciones) {
            val geoPoint = GeoPoint(ubicacion.latitude, ubicacion.longitude)

            // Crear y configurar el marcador
            val marker = Marker(mapView)
            marker.position = geoPoint
            marker.title = ubicacion.name
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

            // Agregar el marcador al mapa
            mapView.overlays.add(marker)
        }

        // Centrar el mapa en la primer ubicación (ajusta según tus necesidades)
        if (ubicaciones.isNotEmpty()) {
            mapController.setCenter(GeoPoint(ubicaciones[0].latitude, ubicaciones[0].longitude))
            mapController.setZoom(15.0)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_usuario, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item1 -> {
                auth.signOut()
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




    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permiso concedido, configurar la capa de ubicación
                    setupMyLocationOverlay()
                } else {
                    // Permiso denegado, puedes manejar esto según tus necesidades
                }
                return
            }
            else -> {
                // Manejar otros casos si es necesario
            }
        }
    }

    private fun setupMyLocationOverlay() {
        // Configurar la capa de la ubicación actual del usuario
        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mapView)
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.enableFollowLocation()

        // Centrar el mapa en la ubicación del usuario si está disponible
        if (myLocationOverlay.lastFix != null) {
            mapController.setCenter(GeoPoint(myLocationOverlay.lastFix.latitude, myLocationOverlay.lastFix.longitude))
            mapController.setZoom(15.0)
        }

        mapView.overlays.add(myLocationOverlay)
    }

    private fun loadLocationsFromJson(): List<Ubicacion> {
        val json: String = loadJsonFromAsset("locations.json") ?: return emptyList()

        val listType = object : TypeToken<Map<String, Ubicacion>>() {}.type
        val locationsMap: Map<String, Ubicacion> = Gson().fromJson(json, listType)

        // Obtener la lista de ubicaciones desde el mapa
        return locationsMap.values.toList()
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
