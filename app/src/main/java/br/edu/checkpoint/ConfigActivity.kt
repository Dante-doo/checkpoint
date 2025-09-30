package br.edu.checkpoint

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.edit

class ConfigActivity : AppCompatActivity() {

    private val tiposMapa = arrayOf("Normal", "Satélite", "Terreno", "Híbrido")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Configurações"
        }

        val prefs = getSharedPreferences("config", Context.MODE_PRIVATE)
        val seekBar = findViewById<SeekBar>(R.id.seekBar)
        val spinner = findViewById<Spinner>(R.id.spinnerTipoMapa)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tiposMapa)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        seekBar.progress = prefs.getInt("zoom", 10)
        spinner.setSelection(prefs.getInt("mapTypeIndex", 0))

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                prefs.edit { putInt("zoom", progress) }
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: android.view.View, position: Int, id: Long) {
                prefs.edit { putInt("mapTypeIndex", position) }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}