package br.edu.checkpoint

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import br.edu.checkpoint.database.DatabaseHandler

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import br.edu.checkpoint.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var banco: DatabaseHandler
    private var marcadorTemp: Marker? = null

    // NOVO: Cliente para obter a localização do dispositivo
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // NOVO: Launcher para pedir permissão de localização
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permissão concedida, tenta focar na localização do usuário
                focusOnUserLocation()
            } else {
                // Permissão negada, foca na localização padrão
                focusOnDefaultLocation()
                Toast.makeText(this, "Permissão de localização negada.", Toast.LENGTH_LONG).show()
            }
        }

    private val locationsActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.let {
                val latitude = it.getDoubleExtra("latitude", 0.0)
                val longitude = it.getDoubleExtra("longitude", 0.0)

                if (latitude != 0.0 && longitude != 0.0) {
                    val location = LatLng(latitude, longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 17f))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDatabase()
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // NOVO: Inicializa o cliente de localização
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val btnSettings = findViewById<ImageButton>(R.id.ibConfig)
        btnSettings.setOnClickListener {
            val intent = Intent(this, ConfigActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // NOVO: Inicia o processo de obter a localização
        checkLocationPermission()
        mMap = googleMap
        val prefs = getSharedPreferences("config", Context.MODE_PRIVATE)
        val mapTypeIndex = prefs.getInt("mapTypeIndex", 0)
        mMap.mapType = getMapTypeFromIndex(mapTypeIndex)

        // A função updateMap() já desenha os marcadores. O código duplicado foi removido.
        updateMap()

        // Listener para cliques em qualquer lugar do mapa (Funcionalidade existente)
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
                        ""
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

        // Listener para cliques em Pontos de Interesse do Google (Funcionalidade corrigida)
        mMap.setOnPoiClickListener { poi ->
            marcadorTemp?.remove()
            marcadorTemp = mMap.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(poi.latLng, 16f))

            val latitude = poi.latLng.latitude
            val longitude = poi.latLng.longitude
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
                        ""
                    }

                    runOnUiThread {
                        val dialog = AlertDialog.Builder(this)
                            .setTitle("Ponto de Interesse")
                            .setMessage("Deseja cadastrar o ponto: ${poi.name}?")
                            .setPositiveButton("Cadastrar") { _, _ ->
                                val intent = Intent(this, CRUDActivity::class.java)
                                intent.putExtra("nome", poi.name)
                                intent.putExtra("descricao", endereco)
                                intent.putExtra("latitude", poi.latLng.latitude)
                                intent.putExtra("longitude", poi.latLng.longitude)
                                startActivity(intent)
                            }
                            .setNegativeButton("Cancelar") { _, _ ->
                                marcadorTemp?.remove()
                                marcadorTemp = null
                            }
                            .create()
                        dialog.show()
                    }

                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this, "Erro ao buscar endereço do ponto", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }

        // [BÔNUS] Listener para cliques nos seus marcadores salvos
        mMap.setOnMarkerClickListener { marker ->
            // Verifica se o marcador clicado não é o marcador temporário azul
            if (marker != marcadorTemp) {
                // O comportamento padrão é mostrar a janela de informações,
                // então retornamos 'false' para permitir que isso aconteça.
                return@setOnMarkerClickListener false
            }
            // Se for o marcador temporário, não fazemos nada (consumimos o evento)
            true
        }
    }

    override fun onResume() {
        super.onResume()

        if (::mMap.isInitialized) {
            updateMap()
        }
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permissão já concedida
                focusOnUserLocation()
            }
            else -> {
                // Pede a permissão
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    // NOVO: Função para focar na localização do usuário
    @SuppressLint("MissingPermission") // A permissão é checada antes de chamar esta função
    private fun focusOnUserLocation() {
        mMap.isMyLocationEnabled = true // Ativa o ponto azul da localização do usuário
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val userLatLng = LatLng(location.latitude, location.longitude)
                val prefs = getSharedPreferences("config", Context.MODE_PRIVATE)
                val zoom = prefs.getInt("zoom", 15) // Zoom inicial um pouco mais próximo
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, zoom.toFloat()))
            } else {
                // Se não conseguir a localização, usa a padrão
                focusOnDefaultLocation()
            }
            updateMap() // Carrega os marcadores salvos
        }
    }

    // NOVO: Função para focar na localização padrão
    private fun focusOnDefaultLocation() {
        val prefs = getSharedPreferences("config", Context.MODE_PRIVATE)
        val zoom = prefs.getInt("zoom", 10)
        val patoBranco = LatLng(-26.2295, -52.6716)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(patoBranco, zoom.toFloat()))
        updateMap() // Carrega os marcadores salvos
    }

    private fun updateMap() {
        val prefs = getSharedPreferences("config", Context.MODE_PRIVATE)
        val mapTypeIndex = prefs.getInt("mapTypeIndex", 0)

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
        locationsActivityLauncher.launch(i)
    }

    fun getApiKeyFromManifest(): String? {
        val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        return appInfo.metaData.getString("com.google.android.geo.API_KEY")
    }
}
