package br.edu.checkpoint.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import br.edu.checkpoint.entity.Cadastro

class DatabaseHandler( context : Context) : SQLiteOpenHelper( context, DATABASE_NAME, null, DATABASE_VERSION ) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL( "CREATE TABLE IF NOT EXISTS ${TABLE_NAME} (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nome TEXT, descricao TEXT, " +
                "latitude REAL,\n" +
                "longitude REAL,\n" +
                "imagem TEXT )")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL( "DROP TABLE IF EXISTS ${TABLE_NAME} " )
        onCreate( db )
    }

    fun insert( cadastro : Cadastro) {
        val registro = ContentValues()
        registro.put( "nome", cadastro.nome )
        registro.put( "descricao", cadastro.descricao )
        registro.put( "latitude", cadastro.latitude )
        registro.put( "longitude", cadastro.longitude )
        registro.put( "imagem", cadastro.imagem )

        val banco = this.writableDatabase

        banco.insert( TABLE_NAME, null, registro )
    }

    fun update( cadastro : Cadastro ) {
        val registro = ContentValues()
        registro.put( "nome", cadastro.nome )
        registro.put( "descricao", cadastro.descricao )
        registro.put( "latitude", cadastro.latitude )
        registro.put( "longitude", cadastro.longitude )
        registro.put( "imagem", cadastro.imagem )

        val banco = this.writableDatabase

        banco.update(
            TABLE_NAME,
            registro,
            "_id = ?",
            arrayOf(cadastro._id.toString())
        )
    }

    fun delete( _id : Int ) {
        val banco = this.writableDatabase
        banco.delete( TABLE_NAME, "_id=${_id}", null)
    }

    fun find( _id : Int ) : Cadastro? {
        val banco = this.writableDatabase

        val registro = banco.query(
            TABLE_NAME,
            null,
            "_id=${_id}",
            null,
            null,
            null,
            null
        )

        if ( registro.count > 0 ) {
            registro.moveToFirst()
            return Cadastro(
                registro.getInt( 0 ),
                registro.getString( 1 ),
                registro.getString( 2 ),
                registro.getDouble( 3 ),
                registro.getDouble( 4 ),
                registro.getString(5)

            )
        }
        return null
    }

    fun list() : Cursor {
        val banco = this.writableDatabase

        return banco.query(
            TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            null
        )
    }

    fun selectAll(): List<Cadastro> {
        val cadastros = mutableListOf<Cadastro>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        if (cursor.moveToFirst()) {
            do {
                val cadastro = Cadastro(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getDouble(3),
                    cursor.getDouble(4),
                    cursor.getString(5)
                )
                cadastros.add(cadastro)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return cadastros
    }

    companion object {
        private const val DATABASE_NAME = "dbfile.sqlite"
        private const val DATABASE_VERSION = 3
        private const val TABLE_NAME = "cadastro"
    }
}