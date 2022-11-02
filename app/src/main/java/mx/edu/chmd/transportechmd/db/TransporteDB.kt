package mx.edu.chmd.transportechmd.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = arrayOf(AsistenciaDAO::class,UsuarioDAO::class,
    RutaDAO::class), version = 1)
abstract class TransporteDB : RoomDatabase() {

    companion object {
        private var INSTANCE: TransporteDB? = null
        fun getInstance(context: Context): TransporteDB {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context,
                    TransporteDB::class.java,
                    "transporte1.db"
                )
                    .build()
            }
            return INSTANCE as TransporteDB
        }
    }

    abstract val iAsistenciaDAO: AsistenciaDAO.IAsistenciaDAO
    abstract val iUsuarioDAO: UsuarioDAO.IUsuarioDAO
    abstract val iRutaDAO: RutaDAO.IRutaDAO
}