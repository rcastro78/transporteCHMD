package mx.edu.chmd.transportechmd.servicios

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.edu.chmd.transportechmd.db.TransporteDB
import mx.edu.chmd.transportechmd.networking.ITransporte
import mx.edu.chmd.transportechmd.networking.TransporteAPI
import mx.edu.chmd.transportechmd.viewmodel.AsistenciaViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NetworkChangeReceiver : BroadcastReceiver() {
    lateinit var iTransporteService: ITransporte

    override fun onReceive(context: Context, intent: Intent?) {
        val isConnected = checkInternet(context)
        val db = TransporteDB.getInstance(context)
        iTransporteService = TransporteAPI.getCHMDService()!!
        if(isConnected){

            CoroutineScope(Dispatchers.IO).launch {
                val alumnosProcesadosSinRed = db.iAsistenciaDAO.getAsistenciaSP()
                alumnosProcesadosSinRed.forEach {alumno->


                        if(alumno.ascenso.toInt()>0 || alumno.descenso.toInt()>0){
                            //alumno procesado en la ruta de la mañana
                            //enviar al server
                            CoroutineScope(Dispatchers.IO).launch {
                                //Log.d("ALUMNO PROC MAN", alumno.nombreAlumno)
                                procesarAlumnosMan(db, alumno.idAlumno,alumno.idRuta,alumno.ascenso,alumno.descenso)
                            }
                        }
                        if(alumno.ascenso_t.toInt()>0 || alumno.descenso_t.toInt()>0){
                            //alumno procesado en la ruta de la tarde
                            //enviar al server
                            CoroutineScope(Dispatchers.IO).launch {
                                //Log.d("ALUMNO PROC MAN", alumno.nombreAlumno)
                                procesarAlumnosTar(db,alumno.idAlumno,alumno.idRuta,alumno.ascenso,alumno.descenso)
                            }
                        }
                    }

                }

        }else{
            //Toast.makeText(context,"No está conectado",Toast.LENGTH_LONG).show()
        }
    }


    fun checkInternet(context: Context?): Boolean {
        val serviceManager = ServiceManager(context!!)
        return serviceManager.isNetworkAvailable
    }


    internal class ServiceManager(var context: Context) {
        val isNetworkAvailable: Boolean
            get() {
                val cm =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val networkInfo = cm.activeNetworkInfo
                return networkInfo != null && networkInfo.isConnected
            }

    }



    fun procesarAlumnosMan(db:TransporteDB, id_alumno: String, id_ruta: String,ascenso:String,descenso:String) {
        val call = iTransporteService.procesarMan(id_alumno, id_ruta,ascenso,descenso)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.code()==200) {
                    CoroutineScope(Dispatchers.IO).launch {
                        db.iAsistenciaDAO.actualizaProcesados(id_ruta,id_alumno)
                    }
                }

            }

            override fun onFailure(call: Call<String>, t: Throwable) {

                Log.d("ERROR-DSG", t.localizedMessage)
            }

        })
    }

    fun procesarAlumnosTar(db:TransporteDB,id_alumno: String, id_ruta: String,ascenso:String,descenso:String) {
        val call = iTransporteService.procesarTar(id_alumno, id_ruta,ascenso,descenso)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if(response.code()==200)
                CoroutineScope(Dispatchers.IO).launch {
                    db.iAsistenciaDAO.actualizaProcesados(id_ruta,id_alumno)
                }

            }

            override fun onFailure(call: Call<String>, t: Throwable) {

                Log.d("ERROR-DSG", t.localizedMessage)
            }

        })
    }

}