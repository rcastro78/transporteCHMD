package mx.edu.chmd.transportechmd.networking

import mx.edu.chmd.transportechmd.model.Asistencia
import mx.edu.chmd.transportechmd.model.ComentarioItem
import mx.edu.chmd.transportechmd.model.Ruta
import mx.edu.chmd.transportechmd.model.Usuario
import retrofit2.Call
import retrofit2.http.*

interface ITransporte {
    @GET("getRutaTransporte.php")
    fun getRutaTransporte(@Query("aux_id") aux_id: String?): Call<List<Ruta>>

    @GET("validarSesion.php")
    fun iniciarSesion(
        @Query("usuario") email: String?,
        @Query("clave") clave: String?
    ): Call<List<Usuario>>




    //Alumnos en ruta
    @GET("getAlumnosRutaMat.php")
    fun getAsistenciaMan(@Query("ruta_id") idRuta: String?):Call<List<Asistencia>>
    @GET("getAlumnosRutaTar.php")
    fun getAsistenciaTar(@Query("ruta_id") idRuta: String?):Call<List<Asistencia>>

    //Marcar asistencia
    @GET("asistenciaAlumno.php")
    fun asistenciaAlumnoMan(@Query("id_alumno") idAlumno: String?,@Query("id_ruta") idRuta: String?):Call<String>

    //Marcar bajada
//Marcar asistencia
    @GET("descensoAlumno.php")
    fun descensoAlumnoMan(@Query("id_alumno") idAlumno: String?,@Query("id_ruta") idRuta: String?):Call<String>

    //Marcar asistencia
    @GET("ascensoTodosMan.php")
    fun ascensoTodosAlumnos(@Query("id_ruta") idRuta: String?):Call<String>

    //Enviar comentario
    @GET("registraComentario.php")
    fun enviarComentario(@Query("id_ruta") idRuta: String?,@Query("comentario") comentario: String?):Call<String>
    @GET("getComentario.php")
    fun getComentario(@Query("id_ruta") idRuta: String?):Call<ComentarioItem>



    @GET("asistenciaAlumnoTarde.php")
    fun asistenciaAlumnoTar(@Query("id_alumno") idAlumno: String?,@Query("id_ruta") idRuta: String?):Call<String>
    @GET("descensoAlumnoTarde.php")
    fun descensoAlumnoTar(@Query("id_alumno") idAlumno: String?,@Query("id_ruta") idRuta: String?):Call<String>

    //Marcar inasistencia
    @GET("noAsistenciaAlumno.php")
    fun inasistenciaAlumnoMan(@Query("id_alumno") idAlumno: String?,@Query("id_ruta") idRuta: String?):Call<String>
    @GET("noAsistenciaAlumnoTarde.php")
    fun inasistenciaAlumnoTar(@Query("id_alumno") idAlumno: String?,@Query("id_ruta") idRuta: String?):Call<String>

    //reinicio de asistencia
    @GET("reiniciaAsistenciaAlumno.php")
    fun reiniciaAsistenciaAlumnoMan(@Query("id_alumno") idAlumno: String?,@Query("id_ruta") idRuta: String?):Call<String>

    @GET("reiniciaAsistenciaAlumnoTarde.php")
    fun reiniciaAsistenciaAlumnoTar(@Query("id_alumno") idAlumno: String?,@Query("id_ruta") idRuta: String?):Call<String>

    //Marcar asistencia
    @GET("descensoTodosTar.php")
    fun descensoTodosAlumnoTar(@Query("id_ruta") idRuta: String?):Call<String>

    @GET("reiniciarBajada.php")
    fun reiniciarBajada(@Query("id_alumno") idAlumno: String?,@Query("id_ruta") idRuta: String?):Call<String>

    //Cerrar ruta

    @GET("cerrarRuta.php")
    fun cerrarRuta(
        @Query("id_ruta") id_ruta: String?,
        @Query("estatus") estatus: String?
    ): Call<Ruta>


    //Cerrar ruta
    @FormUrlEncoded
    @POST("cerrarRutaTarde2.php")
    fun cerrarRutaTarde(
        @Field("id_ruta") id_ruta: String?,
        @Field("estatus") estatus: String?
    ): Call<Ruta?>?

    //Postear recorrido
    //0->no es emergencia
    //1->es emergencia
    @FormUrlEncoded
    @POST("enviaRuta.php")
    fun enviarRuta(
        @Field("id_ruta") idRuta: String?, @Field("aux_id") aux_id: String?,
        @Field("latitud") latitud: String?, @Field("longitud") longitud: String?,
        @Field("es_emergencia") emergencia: String?
    ): Call<String?>?




    @FormUrlEncoded
    @POST("crearSesion.php")
    fun crearSesion(
        @Field("usuario_id") usuario_id: String?,
        @Field("token") token: String?
    ): Call<String?>?

}