package br.edu.checkpoint.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.widget.BaseAdapter
import android.widget.ImageButton
import br.edu.checkpoint.CRUDActivity
import br.edu.checkpoint.LocationsActivity
import br.edu.checkpoint.R
import br.edu.checkpoint.entity.Cadastro
import com.google.android.material.button.MaterialButton
import androidx.core.net.toUri

class ListaAdapter (private val activity: LocationsActivity, val cursor : Cursor) : BaseAdapter(){

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
            cursor.getString(5),
        )
        return cadastro
    }

    override fun getItemId(id: Int): Long {
        cursor.moveToPosition(id)
        return cursor.getLong(0)
    }

    override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup?): android.view.View {
        val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = inflater.inflate( R.layout.item_ponto_turistico, null)

        val tvNome = v.findViewById<android.widget.TextView>(R.id.tvNome)
        val tvDescricao = v.findViewById<android.widget.TextView>(R.id.tvDescricao)
        val ivImagem = v.findViewById<android.widget.ImageView>(R.id.ivImagem)
        val btEditar = v.findViewById<ImageButton>(R.id.btEditar)
        val btDeletar = v.findViewById<ImageButton>(R.id.btDeletar)

        cursor.moveToPosition(position)

        tvNome.text = cursor.getString(1)
        tvDescricao.text = cursor.getString(2)

        val imagemUriString = cursor.getString(5)
        if (!imagemUriString.isNullOrEmpty()) {
            ivImagem.setImageURI(android.net.Uri.parse(imagemUriString))
        }

        // AÇÃO DO BOTÃO DE EDITAR (sem alterações)
        btEditar.setOnClickListener {
            cursor.moveToPosition(position)
            val intent = Intent(activity, CRUDActivity::class.java)
            intent.putExtra("cod", cursor.getInt(0))
            intent.putExtra("nome", cursor.getString(1))
            intent.putExtra("descricao", cursor.getString(2))
            intent.putExtra("latitude", cursor.getDouble(3))
            intent.putExtra("longitude", cursor.getDouble(4))
            intent.putExtra("imagem", cursor.getString(5))
            activity.startActivity(intent)
        }

        // AÇÃO DO BOTÃO DE DELETAR (sem alterações)
        btDeletar.setOnClickListener {
            cursor.moveToPosition(position)
            val idParaDeletar = cursor.getInt(0)
            activity.deletePontoTuristico(idParaDeletar)
        }

        // ADICIONE ESTE NOVO LISTENER PARA O CLIQUE NO ITEM INTEIRO
        v.setOnClickListener {
            cursor.moveToPosition(position)

            val latitude = cursor.getDouble(3)
            val longitude = cursor.getDouble(4)

            val intent = Intent()
            intent.putExtra("latitude", latitude)
            intent.putExtra("longitude", longitude)
            activity.setResult(Activity.RESULT_OK, intent)
            activity.finish() // Fecha a tela da lista e volta para o mapa
        }

        return v
    }
}