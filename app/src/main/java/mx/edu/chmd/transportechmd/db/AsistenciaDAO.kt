package mx.edu.chmd.transportechmd.db

import androidx.room.*

@Entity(tableName = AsistenciaDAO.TABLE_NAME)
class AsistenciaDAO(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "idRuta")  val  idRuta:String,
    @ColumnInfo(name = "tarjeta")  val  tarjeta:String,
    @ColumnInfo(name = "idAlumno")  val  idAlumno:String,
    @ColumnInfo(name = "nombreAlumno")  val  nombreAlumno:String,
    @ColumnInfo(name = "domicilio")  val  domicilio:String,
    @ColumnInfo(name = "horaManana")  val  horaManana:String,
    @ColumnInfo(name = "horaRegreso")  val  horaRegreso:String,
    @ColumnInfo(name = "ascenso")  val  ascenso:String,
    @ColumnInfo(name = "descenso")  val  descenso:String,
    @ColumnInfo(name = "domicilio_s")  val  domicilio_s:String,
    @ColumnInfo(name = "grupo")  val  grupo:String,
    @ColumnInfo(name = "grado")  val  grado:String,
    @ColumnInfo(name = "nivel")  val  nivel:String,
    @ColumnInfo(name = "foto")  val  foto:String,
    @ColumnInfo(name = "selected")  val  selected:Boolean,
    @ColumnInfo(name = "selectedNA")  val  selectedNA:Boolean,
    @ColumnInfo(name = "ascenso_t")  val  ascenso_t:String,
    @ColumnInfo(name = "descenso_t")  val  descenso_t:String,
    @ColumnInfo(name = "salida")  val  salida:String,
    @ColumnInfo(name = "ordenIn")  val  ordenIn:String,
    @ColumnInfo(name = "ordenOut")  val  ordenOut:String,
    @ColumnInfo(name = "inasist")  val  inasist:Boolean,
    @ColumnInfo(name = "inasistTarde")  val  inasistTarde:Boolean,
    @ColumnInfo(name = "procesado")  val  procesado:Int,
    @ColumnInfo(name = "asistencia")  val  asistencia:String,) {
    companion object {
        const val TABLE_NAME = "Asistencia"
    }

    @Dao
    interface IAsistenciaDAO {

        @Query("SELECT * FROM $TABLE_NAME WHERE tarjeta=:tarjeta AND idRuta=:idRuta")
        fun getAlumnoByTarjeta(tarjeta: String,idRuta:String):List<AsistenciaDAO>

        @Query("SELECT * FROM $TABLE_NAME WHERE idRuta=:idRuta ORDER BY CAST(ascenso as INTEGER), CAST(ordenIn as INTEGER)")
        fun getAsistencia(idRuta: String):List<AsistenciaDAO>

        @Query("SELECT COUNT(*) FROM $TABLE_NAME WHERE idRuta=:idRuta AND asistencia='1'")
        fun getTotalidadAsistenciaEnBus(idRuta: String):Int

        @Query("SELECT COUNT(*) FROM $TABLE_NAME WHERE idRuta=:idRuta AND asistencia='1' AND ascenso='1' AND descenso='0'")
        fun getTotalidadAsistenciaEnBusBajada(idRuta: String):Int

        @Query("SELECT COUNT(*) FROM $TABLE_NAME WHERE idRuta=:idRuta AND asistencia='1' AND ascenso_t='1' AND descenso_t='0'")
        fun getTotalidadAsistenciaEnBusBajadaTarde(idRuta: String):Int

        @Query("SELECT * FROM $TABLE_NAME WHERE idRuta=:idRuta AND nombreAlumno LIKE '%' || :nombre || '%' ORDER BY CAST(ascenso as INTEGER), CAST(ordenIn as INTEGER)")
        fun buscarAlumnos(idRuta: String,nombre:String):List<AsistenciaDAO>


        @Query("SELECT * FROM $TABLE_NAME WHERE idRuta=:idRuta AND ascenso<2 and descenso<2 ORDER BY CAST(descenso as INTEGER), CAST(ordenIn as INTEGER)")
        fun getAlumnosBajar(idRuta: String):List<AsistenciaDAO>

        @Query("SELECT * FROM $TABLE_NAME WHERE idRuta=:idRuta AND ascenso_t<2 and descenso_t<2 ORDER BY CAST(descenso_t as INTEGER), CAST(ordenIn as INTEGER)")
        fun getAlumnosBajarTarde(idRuta: String):List<AsistenciaDAO>

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun guardaAsistencia(asistenciaDAO: AsistenciaDAO)


        @Query("SELECT COUNT(*) FROM $TABLE_NAME WHERE idRuta=:idRuta AND tarjeta=:tarjeta")
        fun existeTarjeta(tarjeta: String,idRuta: String):Int



        //turno mañana
        @Query("UPDATE $TABLE_NAME SET ascenso=1 , descenso=0,procesado=:procesado WHERE idRuta=:idRuta AND idAlumno=:idAlumno AND ascenso=0 AND descenso=0")
        fun asisteTurnoMan(idRuta: String, idAlumno: String,procesado:Int)

        @Query("UPDATE $TABLE_NAME SET ascenso=1 , descenso=0,procesado=:procesado WHERE idRuta=:idRuta AND tarjeta=:tarjeta AND ascenso=0 AND descenso=0")
        fun asisteTurnoManNFC(idRuta: String, tarjeta: String,procesado:Int)

        @Query("UPDATE $TABLE_NAME SET ascenso=1 , descenso=0,procesado=:procesado WHERE idRuta=:idRuta AND ascenso<2 AND descenso<2")
        fun asistenTodosMan(idRuta: String,procesado:Int)

        @Query("UPDATE $TABLE_NAME SET descenso=1,procesado=:procesado WHERE idRuta=:idRuta AND ascenso<2 AND descenso<2")
        fun bajanTodosMan(idRuta: String,procesado:Int)

        @Query("UPDATE $TABLE_NAME SET ascenso=0 , descenso=0,procesado=:procesado WHERE idRuta=:idRuta AND idAlumno=:idAlumno")
        fun reiniciaAsistenciaMan(idRuta: String, idAlumno: String,procesado: Int)

        @Query("UPDATE $TABLE_NAME SET ascenso=1 , descenso=0,procesado=:procesado WHERE idRuta=:idRuta AND idAlumno=:idAlumno")
        fun reiniciaSubidaMan(idRuta: String, idAlumno: String, procesado: Int)

        @Query("UPDATE $TABLE_NAME SET ascenso=0 , descenso=0,procesado=:procesado WHERE idRuta=:idRuta")
        fun reiniciaAsistenciaRutaMan(idRuta: String,procesado: Int)

        @Query("UPDATE $TABLE_NAME SET ascenso=2, descenso=2,procesado=:procesado WHERE idRuta=:idRuta AND idAlumno=:idAlumno")
        fun noAsisteTurnoMan(idRuta: String, idAlumno: String,procesado: Int)

        @Query("UPDATE $TABLE_NAME SET ascenso=1 , descenso=1,procesado=:procesado WHERE idRuta=:idRuta AND idAlumno=:idAlumno")
        fun bajaTurnoMan(idRuta: String, idAlumno: String,procesado: Int)

        @Query("UPDATE $TABLE_NAME SET ascenso=1 , descenso=1,procesado=:procesado WHERE idRuta=:idRuta AND tarjeta=:tarjeta")
        fun bajaTurnoManNFC(idRuta: String, tarjeta: String,procesado: Int)

        //turno tarde
        @Query("UPDATE $TABLE_NAME SET ascenso_t=1 , descenso_t=0,procesado=:procesado WHERE idRuta=:idRuta AND idAlumno=:idAlumno")
        fun asisteTurnoTar(idRuta: String, idAlumno: String,procesado: Int)

        @Query("UPDATE $TABLE_NAME SET ascenso_t=1 , descenso_t=0,procesado=:procesado WHERE idRuta=:idRuta AND tarjeta=:tarjeta")
        fun asisteTurnoTarNFC(idRuta: String, tarjeta: String,procesado: Int)

        @Query("UPDATE $TABLE_NAME SET ascenso_t=1 , descenso_t=0,procesado=:procesado WHERE idRuta=:idRuta AND ascenso_t<2 AND descenso_t<2")
        fun asistenTodosTar(idRuta: String,procesado: Int)

        @Query("UPDATE $TABLE_NAME SET descenso_t=1,procesado=:procesado WHERE idRuta=:idRuta AND ascenso_t<2 AND descenso_t<2")
        fun bajanTodosTar(idRuta: String,procesado: Int)

        @Query("UPDATE $TABLE_NAME SET ascenso_t=0 , descenso_t=0,procesado=:procesado WHERE idRuta=:idRuta AND idAlumno=:idAlumno")
        fun reiniciaAsistenciaTar(idRuta: String, idAlumno: String,procesado: Int)

        @Query("UPDATE $TABLE_NAME SET ascenso_t=1 , descenso_t=0, salida=0,procesado=:procesado WHERE idRuta=:idRuta AND idAlumno=:idAlumno")
        fun reiniciaBajadaTar(idRuta: String, idAlumno: String,procesado: Int)

        @Query("UPDATE $TABLE_NAME SET ascenso_t=2, descenso_t=2,procesado=:procesado WHERE idRuta=:idRuta AND idAlumno=:idAlumno")
        fun noAsisteTurnoTar(idRuta: String, idAlumno: String,procesado: Int)

        @Query("UPDATE $TABLE_NAME SET ascenso_t=1 , descenso_t=1,procesado=:procesado WHERE idRuta=:idRuta AND idAlumno=:idAlumno")
        fun bajaTurnoTar(idRuta: String, idAlumno: String,procesado: Int)

        @Query("UPDATE $TABLE_NAME SET ascenso_t=1 , descenso_t=1,procesado=:procesado WHERE idRuta=:idRuta AND tarjeta=:tarjeta")
        fun bajaTurnoTarNFC(idRuta: String, tarjeta: String,procesado: Int)

        //elimina asistencia
        @Query("DELETE FROM $TABLE_NAME WHERE idRuta=:idRuta")
        fun eliminaAsistencia(idRuta:String)

        @Query("DELETE FROM $TABLE_NAME")
        fun eliminaAsistenciaCompleta()

        //Procesamiento offline
        @Query("SELECT * FROM $TABLE_NAME WHERE procesado=-1")
        fun getAsistenciaSP():List<AsistenciaDAO>
        @Query("UPDATE $TABLE_NAME SET procesado=1 WHERE idAlumno=:idAlumno and idRuta=:idRuta")
        fun actualizaProcesados(idRuta: String, idAlumno: String)

    }

}