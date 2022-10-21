package mx.edu.chmd.transportechmd.db

import androidx.room.*

@Entity(tableName = UsuarioDAO.TABLE_NAME)
class UsuarioDAO(@PrimaryKey(autoGenerate = true) val uid: Int,
                 @ColumnInfo(name = "id")  val id:String,
                 @ColumnInfo(name = "id_usuario")  val id_usuario: String,
                 @ColumnInfo(name = "correo")  val correo: String,
                 @ColumnInfo(name = "clave")  val clave:String){
    companion object{
        const val TABLE_NAME = "Usuario"
    }

    @Dao
    interface IUsuarioDAO{
        @Query("SELECT * FROM $TABLE_NAME WHERE correo=:correo AND clave=:clave")
        fun validarUsuario(correo: String,clave: String):UsuarioDAO

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun guardaUser(u:UsuarioDAO)

        @Delete
        fun elimina(u: UsuarioDAO)

    }
}