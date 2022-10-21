package mx.edu.chmd.transportechmd.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import mx.edu.chmd.transportechmd.model.Ruta
import mx.edu.chmd.transportechmd.networking.ITransporte
import mx.edu.chmd.transportechmd.networking.TransporteAPI
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RutaViewModel:ViewModel(){
    private var rutaResultData: MutableLiveData<List<Ruta>>
    private var rutaCierreResultData: MutableLiveData<Ruta>
    private var iTransporteService: ITransporte
    init {
        rutaResultData = MutableLiveData()
        rutaCierreResultData = MutableLiveData()
        iTransporteService = TransporteAPI.getCHMDService()!!
    }

    fun getUserDataResultObserver(): MutableLiveData<List<Ruta>> {
        return rutaResultData
    }
    fun getCierreResultObserver(): MutableLiveData<Ruta> {
        return rutaCierreResultData
    }

    fun getRuta(aux_id:String){
        val call = iTransporteService.getRutaTransporte(aux_id)
        call.enqueue(object: Callback<List<Ruta>> {
            override fun onResponse(call: Call<List<Ruta>>, response: Response<List<Ruta>>) {
                if(response!=null){
                    rutaResultData.postValue(response.body())
                }

            }

            override fun onFailure(call: Call<List<Ruta>>, t: Throwable) {
                rutaResultData.postValue(null)
                Log.d("ERROR-DSG",t.localizedMessage)
            }

        })
    }

    fun cerrarRuta(estatus:String,id_ruta:String){
        val call = iTransporteService.cerrarRuta(id_ruta, estatus)
        call.enqueue(object: Callback<Ruta> {
            override fun onResponse(call: Call<Ruta>, response: Response<Ruta>) {
                if(response!=null){
                    rutaCierreResultData.postValue(response.body())
                }

            }

            override fun onFailure(call: Call<Ruta>, t: Throwable) {
                rutaCierreResultData.postValue(null)
                Log.d("ERROR-DSG",t.localizedMessage)
            }

        })
    }


}