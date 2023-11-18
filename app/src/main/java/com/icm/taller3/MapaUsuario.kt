package com.icm.taller3

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
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
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem
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

        //Notificación
        startService(Intent(this, ServiceUser::class.java))

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
            val locationsArray: JSONArray = jsonObject.getJSONArray("locationsArray")

            // Crear una lista de OverlayItems para los puntos de interés
            val items = ArrayList<OverlayItem>()
            for (i in 0 until locationsArray.length()) {
                val locationObject = locationsArray.getJSONObject(i)
                val latitude = locationObject.getDouble("latitude")
                val longitude = locationObject.getDouble("longitude")
                val name = locationObject.getString("name")

                val point = GeoPoint(latitude, longitude)
                val overlayItem = OverlayItem(name, "", point)
                items.add(overlayItem)
            }

            // Añadir los OverlayItems al mapa
            val itemizedIconOverlay =
                ItemizedIconOverlay<OverlayItem>(items, null, this)
            mapView.overlays.add(itemizedIconOverlay)

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
