package br.edu.checkpoint

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import br.edu.checkpoint.database.DatabaseHandler
import br.edu.checkpoint.entity.Cadastro
import java.io.ByteArrayOutputStream
import java.io.InputStream

class CRUDActivity : AppCompatActivity() {

    private lateinit var etNome: EditText
    private lateinit var etDescricao: EditText
    private lateinit var etLatitude: EditText
    private lateinit var etLongitude: EditText
    private lateinit var ivImagem: ImageView
    private lateinit var btnSalvar: Button
    private lateinit var btnExcluir: Button
    private lateinit var banco : DatabaseHandler
    private var imagemSelecionadaUri: Uri? = null
    private var imagemBytes: ByteArray? = null

    private var id: Int = 0



    private val selecionarImagem =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                imagemSelecionadaUri = uri
                ivImagem.setImageURI(uri)
                imagemBytes = uriToByteArray(uri)
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crudactivity)

        initDatabase()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Cadastro de pontos turísticos"
        }

        etNome = findViewById(R.id.etNome)
        etDescricao = findViewById(R.id.etDescricao)
        etLatitude = findViewById(R.id.etLatitude)
        etLongitude = findViewById(R.id.etLongitude)
        ivImagem = findViewById(R.id.ivImagem)
        val btnSelecionarImagem = findViewById<Button>(R.id.btnSelecionarImagem)
        btnSalvar = findViewById(R.id.btnSalvar)
        btnExcluir = findViewById(R.id.btnExcluir)

        id = intent.getIntExtra("cod", 0)

        if (id != 0) {
            etNome.setText(intent.getStringExtra("nome"))
            etDescricao.setText(intent.getStringExtra("descricao"))
            etLatitude.setText(intent.getDoubleExtra("latitude", 0.0).toString())
            etLongitude.setText(intent.getDoubleExtra("longitude", 0.0).toString())
            imagemBytes = intent.getByteArrayExtra("imagem")
            imagemBytes?.let {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                ivImagem.setImageBitmap(bitmap)
            }

        } else {
            btnExcluir.visibility = Button.GONE

            intent.getStringExtra("descricao")?.let { etDescricao.setText(it) }
            val latitude = intent.getDoubleExtra("latitude", 0.0)
            val longitude = intent.getDoubleExtra("longitude", 0.0)
            if (latitude != 0.0 && longitude != 0.0) {
                etLatitude.setText(latitude.toString())
                etLongitude.setText(longitude.toString())
            }
        }

        btnSalvar.setOnClickListener {
            btnSalvarOnClick()
        }

        btnExcluir.setOnClickListener {
            btnExcluirOnClick()
        }

        btnSelecionarImagem.setOnClickListener {
            selecionarImagem.launch("image/*")
        }
    }


    private fun btnExcluirOnClick() {
        if( id != 0 ) {
            banco.delete( id )
            Toast.makeText( this, "Excluído com sucesso!", Toast.LENGTH_SHORT ).show()
            finish()
        } else {
            Toast.makeText( this, "Nenhum registro para excluir!", Toast.LENGTH_SHORT ).show()
        }
    }

    private fun btnSalvarOnClick() {
        if( etNome.text.isNotEmpty() && etDescricao.text.isNotEmpty() && etLatitude.text.isNotEmpty() && etLongitude.text.isNotEmpty() ) {
            val cadastro = Cadastro(
                id,
                etNome.text.toString(),
                etDescricao.text.toString(),
                etLatitude.text.toString().toDouble(),
                etLongitude.text.toString().toDouble(),
                imagemBytes
            )
            Log.d("CRUD", "ID recebido no cadastro: ${cadastro._id}")

            if( id != 0)  {
                banco.update( cadastro )
                Toast.makeText( this, "Alterado com sucesso!", Toast.LENGTH_SHORT ).show()
            } else {
                banco.insert( cadastro )
                Toast.makeText( this, "Cadastrado com sucesso!", Toast.LENGTH_SHORT ).show()
            }
            finish()
        } else {
            Toast.makeText( this, "Preencha todos os campos!", Toast.LENGTH_SHORT ).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun uriToByteArray(uri: Uri): ByteArray? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun initDatabase() {
        banco = DatabaseHandler( this )
    }
}

