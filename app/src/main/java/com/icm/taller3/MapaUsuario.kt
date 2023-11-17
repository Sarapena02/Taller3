package com.icm.taller3

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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
        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)

        // Añadir marcador de ubicación actual del usuario
        addMyLocationOverlay()

        // Añadir marcadores de puntos de interés
        addPointsOfInterestMarkers()

        // Centrar la vista en la ubicación del usuario
        centerMapToUserLocation()
    }

    private fun addMyLocationOverlay() {
        val locationProvider = GpsMyLocationProvider(this)
        mLocationOverlay = MyLocationNewOverlay(locationProvider, mapView)
        mLocationOverlay.enableMyLocation()

        // Agregar el overlay después de habilitar la ubicación
        mapView.overlays.add(mLocationOverlay)
    }

    private fun addPointsOfInterestMarkers() {
        try {
            // Leer el archivo JSON
            val inputStream: InputStream = assets.open("locations.json")
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val json = String(buffer, charset("UTF-8"))

            // Parsear el JSON
            val jsonObject = JSONObject(json)
            val locationsArray: JSONArray = jsonObject.getJSONObject("locations").getJSONArray("locationsArray")

            // Crear una lista de marcadores para los puntos de interés
            for (i in 0 until locationsArray.length()) {
                val locationObject = locationsArray.getJSONObject(i)
                val latitude = locationObject.getDouble("latitude")
                val longitude = locationObject.getDouble("longitude")
                val name = locationObject.getString("name")

                // Crear un marcador en la ubicación del punto de interés
                val point = GeoPoint(latitude, longitude)
                val marker = Marker(mapView)
                marker.position = point
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.title = name

                // Añadir el marcador al mapa
                mapView.overlays.add(marker)
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
                val intent = Intent(this, RegisterUser::class.java)
                startActivity(intent)
            }
            R.id.item2 -> {
                showStatusDialog()
            }
            else -> {
                //val intent1 = Intent(this, UsuarioActivos::class.java)
                //startActivity(intent1)
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
