package br.edu.checkpoint

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import br.edu.checkpoint.adapter.ListaAdapter
import br.edu.checkpoint.database.DatabaseHandler

class LocationsActivity : AppCompatActivity() {

    private lateinit var banco : DatabaseHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_locations)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Lista de pontos turísticos"
        }

        banco = DatabaseHandler(this )
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        val cursor = banco.list()
        val adapter = ListaAdapter( this, cursor )
        findViewById<ListView>(R.id.lvPrincipal).adapter = adapter
    }

    fun btCRUDLocationsOnClick(view: View) {
        val intent = Intent(this, CRUDActivity::class.java)
        startActivity(intent)
    }
}