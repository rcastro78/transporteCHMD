package mx.edu.chmd.transportechmd.db

import androidx.room.*

@Entity(tableName = RutaDAO.TABLE_NAME)
class RutaDAO(@PrimaryKey(autoGenerate = true) val uid: Int,
              @ColumnInfo(name = "idRuta")  val idRuta: String,
              @ColumnInfo(name = "nombre")  val nombre: String,
              @ColumnInfo(name = "camion")  val camion: String,
              @ColumnInfo(name = "turno")  val turno: String,
              @ColumnInfo(name = "tipoRuta")  val tipoRuta: String,
              @ColumnInfo(name = "estatus")  val estatus: String){
    companion object {
        const val TABLE_NAME = "RutaTransporte"
    }

    @Dao
    interface IRutaDAO{
        @Query("SELECT * FROM $TABLE_NAME")
        fun getRutas():List<RutaDAO>
        @Query("SELECT * FROM $TABLE_NAME WHERE estatus<2")
        fun getRutasActivas():List<RutaDAO>
        @Insert
        fun guardaRutas(ruta:RutaDAO)
        @Query("DELETE FROM $TABLE_NAME")
        fun delete()
        @Query("UPDATE $TABLE_NAME SET estatus=:estatus WHERE idRuta=:idRuta")
        fun cambiaEstatusRuta(estatus: String, idRuta: String)


        @Query("SELECT COUNT(*) FROM $TABLE_NAME WHERE turno='1' AND estatus<>'2'")
        fun getTotalRutasAbiertasMan():Int

        @Query("SELECT COUNT(*) FROM $TABLE_NAME WHERE turno='2' AND estatus<>'2'")
        fun getTotalRutasAbiertasTar():Int

    }



}