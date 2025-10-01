package br.edu.checkpoint

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import br.edu.checkpoint.database.DatabaseHandler

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import br.edu.checkpoint.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var banco : DatabaseHandler
    private var marcadorTemp: Marker? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initDatabase()

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = "Checkpoint"


        val btnSettings = findViewById<ImageButton>(R.id.ibConfig)
        btnSettings.setOnClickListener {
            val intent = Intent(this, ConfigActivity::class.java)
            startActivity(intent)
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
        val prefs = getSharedPreferences("config", Context.MODE_PRIVATE)
        val mapTypeIndex = prefs.getInt("mapTypeIndex", 0)
        mMap.mapType = getMapTypeFromIndex(mapTypeIndex)

        updateMap()

        mMap.setOnMapClickListener { latLng ->
            val latitude = latLng.latitude
            val longitude = latLng.longitude

            marcadorTemp?.remove()
            marcadorTemp = mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))

            val apiKey = getApiKeyFromManifest()
            val url = "https://maps.googleapis.com/maps/api/geocode/xml?latlng=$latitude,$longitude&key=$apiKey"

            Thread {
                try {
                    val connection = URL(url).openConnection()
                    val input = BufferedReader(InputStreamReader(connection.getInputStream()))
                    val response = StringBuilder()

                    var linha = input.readLine()
                    while (linha != null) {
                        response.append(linha)
                        linha = input.readLine()
                    }
                    input.close()

                    val resposta = response.toString()
                    val endereco: String = if ("<formatted_address>" in resposta) {
                        resposta.substringAfter("<formatted_address>").substringBefore("</formatted_address>")
                    } else {
                        "Endereço não encontrado."
                    }

                    runOnUiThread {
                        val dialog = AlertDialog.Builder(this)
                            .setTitle("Endereço")
                            .setMessage(endereco)
                            .setPositiveButton("Cadastrar Ponto") { _, _ ->
                                val intent = Intent(this, CRUDActivity::class.java)
                                intent.putExtra("descricao", endereco)
                                intent.putExtra("latitude", latLng.latitude)
                                intent.putExtra("longitude", latLng.longitude)
                                startActivity(intent)
                            }
                            .setNegativeButton("Cancelar") {_, _ ->
                                marcadorTemp?.remove()
                                marcadorTemp = null
                            }
                            .create()
                        dialog.show()
                    }

                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this, "Erro ao buscar endereço", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }

        mMap.setOnMapClickListener { latLng ->
            val latitude = latLng.latitude
            val longitude = latLng.longitude

            marcadorTemp?.remove()
            marcadorTemp = mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))

            val apiKey = getApiKeyFromManifest()
            val url = "https://maps.googleapis.com/maps/api/geocode/xml?latlng=$latitude,$longitude&key=$apiKey"

            Thread {
                try {
                    val connection = URL(url).openConnection()
                    val input = BufferedReader(InputStreamReader(connection.getInputStream()))
                    val response = StringBuilder()

                    var linha = input.readLine()
                    while (linha != null) {
                        response.append(linha)
                        linha = input.readLine()
                    }
                    input.close()

                    val resposta = response.toString()
                    val endereco: String = if ("<formatted_address>" in resposta) {
                        resposta.substringAfter("<formatted_address>").substringBefore("</formatted_address>")
                    } else {
                        "" // Deixa a string vazia se não encontrar
                    }

                    runOnUiThread {
                        val dialog = AlertDialog.Builder(this)
                            .setTitle("Endereço")
                            .setMessage(if (endereco.isNotEmpty()) endereco else "Nenhum endereço encontrado para este local.")
                            .setPositiveButton("Cadastrar Ponto") { _, _ ->
                                val intent = Intent(this, CRUDActivity::class.java)
                                intent.putExtra("descricao", endereco)
                                intent.putExtra("latitude", latLng.latitude)
                                intent.putExtra("longitude", latLng.longitude)
                                startActivity(intent)
                            }
                            .setNegativeButton("Cancelar") {_, _ ->
                                marcadorTemp?.remove()
                                marcadorTemp = null
                            }
                            .create()
                        dialog.show()
                    }

                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this, "Erro ao buscar endereço", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }


        val registros = banco.selectAll()
        for (cadastro in registros) {
            val latLng = LatLng(cadastro.latitude, cadastro.longitude)
            val marker = MarkerOptions()
                .position(latLng)
                .title(cadastro.nome)
                .snippet(cadastro.descricao)
            mMap.addMarker(marker)
        }
    }

    override fun onResume() {
        super.onResume()

        if (::mMap.isInitialized) {
            updateMap()
        }
    }

    private fun updateMap() {
        val prefs = getSharedPreferences("config", Context.MODE_PRIVATE)
        val zoom = prefs.getInt("zoom", 10)
        val mapTypeIndex = prefs.getInt("mapTypeIndex", 0)

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-26.2295, -52.6716), zoom.toFloat()))
        mMap.mapType = getMapTypeFromIndex(mapTypeIndex)
        mMap.clear()

        val registros = banco.selectAll()

        for (cadastro in registros) {
            val latLng = LatLng(cadastro.latitude, cadastro.longitude)
            val marker = MarkerOptions()
                .position(latLng)
                .title(cadastro.nome)
                .snippet(cadastro.descricao)
            mMap.addMarker(marker)
        }
    }

    private fun getMapTypeFromIndex(index: Int): Int {
        return when (index) {
            1 -> GoogleMap.MAP_TYPE_SATELLITE
            2 -> GoogleMap.MAP_TYPE_TERRAIN
            3 -> GoogleMap.MAP_TYPE_HYBRID
            else -> GoogleMap.MAP_TYPE_NORMAL
        }
    }

    private fun initDatabase() {

        banco = DatabaseHandler( this )

    }

    fun btLocationsOnClick(view: View) {
        val i = Intent(this, LocationsActivity::class.java)
        startActivity( i )
    }

    fun getApiKeyFromManifest(): String? {
        val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        return appInfo.metaData.getString("com.google.android.geo.API_KEY")
    }
}
