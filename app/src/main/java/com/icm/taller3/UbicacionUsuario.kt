package com.icm.taller3


import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.type.LatLng
import org.mapsforge.map.android.layers.MyLocationOverlay
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class UbicacionUsuario : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private val database = FirebaseDatabase.getInstance()
    private lateinit var myRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ubicacion_usuario)



        myRef = database.getReference("users")

        val identificationNumber = intent.getStringExtra("identificador")



        mapView = findViewById(R.id.map)
        mapView.tileProvider.tileSource = TileSourceFactory.MAPNIK
        mapView.setMultiTouchControls(true)

        myLocationOverlay = addMyLocationOverlay()


        val userLocationReference = myRef.orderByChild("identificationNumber").equalTo(identificationNumber)

        // Escuchar cambios en la ubicación del usuario
        userLocationReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (usuarioSnapshot in dataSnapshot.children) {
                    val usuario = usuarioSnapshot.getValue(User::class.java)
                    // Actualizar la ubicación en el mapa
                    updateMapLocation(usuario?.latitud, usuario?.longitud)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar errores si es necesario
                Log.e("Error", "Error al obtener la ubicación del usuario: ${databaseError.message}")
            }
        })

        // Configuración de OpenStreetMap
        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE))

    }

    private fun updateMapLocation(latitud: String?, longitud: String?) {
        mapView.overlays.clear()
        if (latitud != null && longitud != null) {
            val latitude = latitud.toDouble()
            val longitude = longitud.toDouble()

            val userLocation = GeoPoint(latitude, longitude)

            myLocationOverlay.runOnFirstFix {
                // The location is now available
                val myLocation = myLocationOverlay.myLocation
                val latitude = myLocation.latitude
                val longitude = myLocation.longitude

                val currentLocation = GeoPoint(latitude, longitude)

                if (latitude != null && longitude != null) {
                    nuevaUbi(userLocation,currentLocation)
                }
            }
        }
    }

    private fun nuevaUbi(userLocation: GeoPoint,currentLocation: GeoPoint){
            // Crear un marcador en la ubicación del usuario
            val marker = Marker(mapView)
            marker.position = userLocation
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            mapView.overlays.add(marker)

            //marcador ubicacion actual
            val marker2 = Marker(mapView)
            marker2.position = currentLocation
            marker2.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            mapView.overlays.add(marker2)

            // Crear una línea entre los dos marcadores
            val line = Polyline(mapView)
            line.addPoint(marker2?.position)
            line.addPoint(userLocation)
            mapView.overlays.add(line)

           // Calcular la distancia entre los dos puntos (en metros)
            val distance = marker2?.position?.distanceToAsDouble(userLocation)

            runOnUiThread {
                // Convertir la distancia a kilómetros con dos decimales
                val distanceInKilometers = distance?.div(1000)?.let { "%.2f".format(it) }

                // Mostrar la distancia en el TextView
                val distancia: TextView = findViewById(R.id.textView)
                distancia.text = "Distancia: $distanceInKilometers km"



                // Centrar el mapa en la ubicación del usuario
                val mapController: IMapController = mapView.controller
                mapController.setCenter(userLocation)
                mapController.animateTo(userLocation)
                mapController.setZoom(15.0)
            }



    }

    private fun addMyLocationOverlay(): MyLocationNewOverlay {
        val locationProvider = GpsMyLocationProvider(this)
        val overlay = MyLocationNewOverlay(locationProvider, mapView)
        overlay.enableMyLocation()
        mapView.overlays.add(overlay)
        return overlay
    }

}