package br.edu.checkpoint.adapter

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.widget.BaseAdapter
import br.edu.checkpoint.CRUDActivity
import br.edu.checkpoint.R
import br.edu.checkpoint.entity.Cadastro
import com.google.android.material.button.MaterialButton

class ListaAdapter (val context: Context, val cursor : Cursor) : BaseAdapter(){

    override fun getCount(): Int {
        return cursor.count
    }

    override fun getItem(id: Int): Any {
        cursor.moveToPosition(id)
        val cadastro = Cadastro(
            cursor.getInt(0),
            cursor.getString(1),
            cursor.getString(2),
            cursor.getDouble(3),
            cursor.getDouble(4),
            cursor.getBlob(5),
        )
        return cadastro
    }

    override fun getItemId(id: Int): Long {
        cursor.moveToPosition(id)
        return cursor.getLong(0)
    }

    override fun getView(id: Int, p1: android.view.View?, p2: android.view.ViewGroup?): android.view.View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = inflater.inflate( R.layout.item_ponto_turistico, null)

        val tvNome = v.findViewById<android.widget.TextView>(R.id.tvNome)
        val tvDescricao = v.findViewById<android.widget.TextView>(R.id.tvDescricao)
        val tvLatitude = v.findViewById<android.widget.TextView>(R.id.tvLatitude)
        val tvLongitude = v.findViewById<android.widget.TextView>(R.id.tvLongitude)
        val ivImagem = v.findViewById<android.widget.ImageView>(R.id.ivImagem)
        val btEditar = v.findViewById<MaterialButton>(R.id.btEditar)

        cursor.moveToPosition( id )
        tvNome.text = cursor.getString(1)
        tvDescricao.text = cursor.getString(2)
        tvLatitude.text = cursor.getString(3)
        tvLongitude.text = cursor.getString(4)

        val imagemBytes = cursor.getBlob(5)
        if (imagemBytes != null && imagemBytes.isNotEmpty()) {
            val bitmap = BitmapFactory.decodeByteArray(imagemBytes, 0, imagemBytes.size)
            ivImagem.setImageBitmap(bitmap)
        }


        btEditar.setOnClickListener{
            cursor.moveToPosition(id)

            val intent = Intent(context, CRUDActivity::class.java)
            intent.putExtra("cod", cursor.getInt(0))
            intent.putExtra("nome", cursor.getString(1))
            intent.putExtra("descricao", cursor.getString(2))
            intent.putExtra("latitude", cursor.getDouble(3))
            intent.putExtra("longitude", cursor.getDouble(4))
            intent.putExtra("imagem", cursor.getBlob(5))

            context.startActivity(intent)
        }

        return v
    }
}