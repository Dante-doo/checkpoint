package br.edu.checkpoint

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import br.edu.checkpoint.database.DatabaseHandler
import br.edu.checkpoint.entity.Cadastro
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import androidx.core.net.toUri

class CRUDActivity : AppCompatActivity() {

    private lateinit var etNome: EditText
    private lateinit var etDescricao: EditText
    private lateinit var etLatitude: EditText
    private lateinit var etLongitude: EditText
    private lateinit var ivImagem: ImageView
    private lateinit var btnSalvar: Button
    private lateinit var btnExcluir: Button
    private lateinit var banco : DatabaseHandler
    private var imagemUriString: String? = null
    private var id: Int = 0
    private val STORAGE_PERMISSION_CODE = 101

    private val selecionarImagem =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                // Copia a imagem para o armazenamento interno e salva o novo URI
                imagemUriString = saveImageToInternalStorage(uri)?.toString()
                imagemUriString?.let {
                    ivImagem.setImageURI(it.toUri())
                }
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
            imagemUriString = intent.getStringExtra("imagem")
            imagemUriString?.let {
                ivImagem.setImageURI(Uri.parse(it))
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
            checkPermissionAndOpenGallery()
        }
    }

    private fun checkPermissionAndOpenGallery() {
        // Define a permissão com base na versão do Android
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), STORAGE_PERMISSION_CODE)
        } else {
            selecionarImagem.launch("image/*")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selecionarImagem.launch("image/*")
            } else {
                Toast.makeText(this, "Permissão para acessar a galeria foi negada.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uriToByteArray(uri: Uri): ByteArray? {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
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
                imagemUriString // <-- Salva o URI como String
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

    private fun saveImageToInternalStorage(uri: Uri): Uri? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            // Cria um nome de arquivo único
            val fileName = "${UUID.randomUUID()}.jpg"
            val file = File(filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

//    private fun uriToByteArray(uri: Uri): ByteArray? {
//        return try {
//            val inputStream: InputStream? = contentResolver.openInputStream(uri)
//            val bitmap = BitmapFactory.decodeStream(inputStream)
//            val stream = ByteArrayOutputStream()
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
//            stream.toByteArray()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }


    private fun initDatabase() {
        banco = DatabaseHandler( this )
    }
}

