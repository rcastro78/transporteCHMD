package mx.edu.chmd.transportechmd.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import mx.edu.chmd.transportechmd.model.Comentario

import mx.edu.chmd.transportechmd.networking.ITransporte
import mx.edu.chmd.transportechmd.networking.TransporteAPI
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AsistenciaViewModel: ViewModel() {
    var alumnoAsistenciaResultData: MutableLiveData<String>
    var alumnoBajaResultData: MutableLiveData<String>
    var alumnoProcesadoManResultData: MutableLiveData<String>
    var alumnoProcesadoTarResultData: MutableLiveData<String>
    var alumnoInasistenciaResultData: MutableLiveData<String>
    var alumnoReiniciaAsistenciaResultData: MutableLiveData<String>
    var asistenciaCompletaResultData: MutableLiveData<String>
    var comentarioResultData: MutableLiveData<Comentario>
    private var iTransporteService: ITransporte

    init {
        alumnoAsistenciaResultData = MutableLiveData()
        alumnoInasistenciaResultData = MutableLiveData()
        alumnoReiniciaAsistenciaResultData = MutableLiveData()
        asistenciaCompletaResultData = MutableLiveData()
        alumnoProcesadoManResultData = MutableLiveData()
        alumnoProcesadoTarResultData = MutableLiveData()
        alumnoBajaResultData = MutableLiveData()
        comentarioResultData = MutableLiveData()
        iTransporteService = TransporteAPI.getCHMDService()!!
    }

    fun getAlumnoAsistenciaResultObserver(): MutableLiveData<String> {
        return alumnoAsistenciaResultData
    }

    fun getSubidaCompletaResultObserver(): MutableLiveData<String> {
        return asistenciaCompletaResultData
    }

    fun getAlumnoReiniciaAsistenciaResultObserver(): MutableLiveData<String> {
        return alumnoReiniciaAsistenciaResultData
    }

    fun getAlumnoInasistenciaResultObserver(): MutableLiveData<String> {
        return alumnoInasistenciaResultData
    }

    fun getComentarioResultObserver(): MutableLiveData<Comentario> {
        return comentarioResultData
    }

    fun enviaAsistenciaAlumnoMan(id_alumno: String, id_ruta: String) {
        val call = iTransporteService.asistenciaAlumnoMan(id_alumno, id_ruta)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response != null) {
                    alumnoAsistenciaResultData.postValue(response.body())
                }

            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                alumnoAsistenciaResultData.postValue(null)
                Log.d("ERROR-DSG", t.localizedMessage)
            }

        })
    }



    fun enviaAsistenciaAlumnoTar(id_alumno: String, id_ruta: String) {
        val call = iTransporteService.asistenciaAlumnoTar(id_alumno, id_ruta)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response != null) {
                    alumnoAsistenciaResultData.postValue(response.body())
                }

            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                alumnoAsistenciaResultData.postValue(null)
                Log.d("ERROR-DSG", t.localizedMessage)
            }

        })
    }

    fun enviaAsistenciaCompletaMan(id_ruta: String) {
        val call = iTransporteService.ascensoTodosAlumnos(id_ruta)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response != null) {
                    asistenciaCompletaResultData.postValue(response.body())
                }

            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                asistenciaCompletaResultData.postValue(null)
                Log.d("ERROR-DSG", t.localizedMessage)
            }

        })
    }

    fun enviaAsistenciaCompletaTar(id_ruta: String) {
        val call = iTransporteService.ascensoTodosAlumnos(id_ruta)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response != null) {
                    asistenciaCompletaResultData.postValue(response.body())
                }

            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                asistenciaCompletaResultData.postValue(null)
                Log.d("ERROR-DSG", t.localizedMessage)
            }

        })
    }




    fun enviaReiniciarAsistenciaAlumnoMan(id_alumno: String, id_ruta: String) {
        val call = iTransporteService.reiniciaAsistenciaAlumnoMan(id_alumno, id_ruta)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response != null) {
                    alumnoReiniciaAsistenciaResultData.postValue(response.body())
                }

            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                alumnoReiniciaAsistenciaResultData.postValue(null)
                Log.d("ERROR-DSG", t.localizedMessage)
            }

        })


        }

    fun enviaReiniciarAsistenciaAlumnoTar(id_alumno: String, id_ruta: String) {
        val call = iTransporteService.reiniciaAsistenciaAlumnoTar(id_alumno, id_ruta)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response != null) {
                    alumnoReiniciaAsistenciaResultData.postValue(response.body())
                }

            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                alumnoReiniciaAsistenciaResultData.postValue(null)
                Log.d("ERROR-DSG", t.localizedMessage)
            }

        })


    }


        fun enviaInasistenciaAlumnoMan(id_alumno: String, id_ruta: String) {
            val call = iTransporteService.inasistenciaAlumnoMan(id_alumno, id_ruta)
            call.enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response != null) {
                        alumnoInasistenciaResultData.postValue(response.body())
                    }

                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    alumnoInasistenciaResultData.postValue(null)
                    Log.d("ERROR-DSG", t.localizedMessage)
                }

            })
        }

    fun enviaInasistenciaAlumnoTar(id_alumno: String, id_ruta: String) {
        val call = iTransporteService.inasistenciaAlumnoTar(id_alumno, id_ruta)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response != null) {
                    alumnoInasistenciaResultData.postValue(response.body())
                }

            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                alumnoInasistenciaResultData.postValue(null)
                Log.d("ERROR-DSG", t.localizedMessage)
            }

        })
    }


    fun enviaDescensoAlumnoMan(id_alumno: String, id_ruta: String) {
        val call = iTransporteService.descensoAlumnoMan(id_alumno, id_ruta)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response != null) {
                    alumnoBajaResultData.postValue(response.body())
                }

            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                alumnoBajaResultData.postValue(null)
                Log.d("ERROR-DSG", t.localizedMessage)
            }

        })

    }

    fun enviaDescensoAlumnoTar(id_alumno: String, id_ruta: String) {
        val call = iTransporteService.descensoAlumnoTar(id_alumno, id_ruta)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response != null) {
                    alumnoBajaResultData.postValue(response.body())
                }

            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                alumnoBajaResultData.postValue(null)
                Log.d("ERROR-DSG", t.localizedMessage)
            }

        })

    }

    fun enviaComentario(id_ruta: String,c:String) {
        val call = iTransporteService.enviarComentario(id_ruta,c)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response != null) {
                    alumnoBajaResultData.postValue(response.body())
                }

            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                alumnoBajaResultData.postValue(null)
                Log.d("ERROR-DSG", t.localizedMessage)
            }

        })

    }


    fun enviaReinicioBajadaAlumnoTar(id_alumno: String, id_ruta: String) {
        val call = iTransporteService.reiniciarBajada(id_alumno, id_ruta)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response != null) {
                    alumnoBajaResultData.postValue(response.body())
                }

            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                alumnoBajaResultData.postValue(null)
                Log.d("ERROR-DSG", t.localizedMessage)
            }

        })

    }





    }



