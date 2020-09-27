package com.polarbear.map

import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.io.InputStream
import java.lang.reflect.Type

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var polyline: Polyline
    private lateinit var startCircle: Circle
    private lateinit var endCircle: Circle
    private var isDrawn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val btnImport: Button = findViewById(R.id.btn_import)
        btnImport.setOnClickListener {
            val routes = applicationContext.assets.list("")?.dropLast(2)?.toTypedArray()
            var selectedRoute = ""

            val builder: AlertDialog.Builder = AlertDialog.Builder(this@MapsActivity)
            builder.setTitle("Choose a route")
            builder.setCancelable(false)

            builder.setSingleChoiceItems(
                routes, // array
                -1 // initial selection (-1 none)
            ){ _, i ->
                selectedRoute = routes?.get(i) ?: ""
            }

            builder.setPositiveButton("Ok"){ _, _ ->
                if (selectedRoute != ""){
                    // Get path from json structure
                    val features = getRoute(selectedRoute)[0].features
                    var path: Any? = null
                    for (i in 0..2) {
                        if (features?.get(i)?.geometry?.type == "LineString") {
                            path = features[i].geometry?.coordinates
                            break
                        }
                    }
                    val pathArray: ArrayList<ArrayList<Double>> = path as ArrayList<ArrayList<Double>>

                    drawRoute(pathArray)
                }
            }
            builder.setNegativeButton("Cancel",null)

            val dialog = builder.create()
            dialog.show()
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    private fun getJsonFromAssets(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            val inputStream: InputStream = context.assets.open(fileName)
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            jsonString = String(buffer, charset("UTF-8"))
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return jsonString
    }

    private fun getRoute(fileName: String): ArrayList<Route> {
        var jsonFileString: String? = getJsonFromAssets(applicationContext, fileName)

        val gson = Gson()
        val listRouteType: Type = object : TypeToken<ArrayList<Route?>?>() {}.type

        // Add "[", "]" to jsonFileString
        jsonFileString = "[$jsonFileString]"

        return gson.fromJson(jsonFileString, listRouteType)
    }

    private fun drawRoute(path: ArrayList<ArrayList<Double>>) {
        // Remove previous drawings
        if (isDrawn) {
            polyline.remove()
            startCircle.remove()
            endCircle.remove()
        }

        // Add polylines to the map.
        val polylineOptions = PolylineOptions()
        for (i in 0 until path.size) {
            polylineOptions.add(LatLng(path[i][1], path[i][0]))
        }
        polyline = mMap.addPolyline(polylineOptions)

        // Draw a circle at start and end point
        startCircle = mMap.addCircle(CircleOptions()
            .center(LatLng(path[0][1], path[0][0]))
            .radius(100.0))

        endCircle = mMap.addCircle(CircleOptions()
            .center(LatLng(path[path.size - 1][1], path[path.size - 1][0]))
            .radius(100.0))

        // Style the polyline
        polyline.startCap = RoundCap()
        polyline.endCap = RoundCap()
        polyline.width = 6f
        polyline.color = R.color.polyline_color
        polyline.jointType = JointType.ROUND

        startCircle.strokeColor = R.color.start_end_color
        endCircle.strokeColor = R.color.start_end_color

        // Position the map's camera at the center of the current route,
        // and set the zoom factor so most of route shows on the screen.
        var sumX = 0.0
        var sumY = 0.0
        for (i in 0 until path.size) {
            sumX += path[i][1]
            sumY += path[i][0]
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(sumX/path.size, sumY/path.size), 12f))

        isDrawn = true
    }

}
