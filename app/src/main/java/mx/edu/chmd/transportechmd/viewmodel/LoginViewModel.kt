package mx.edu.chmd.transportechmd.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import mx.edu.chmd.transportechmd.model.Usuario
import mx.edu.chmd.transportechmd.networking.ITransporte
import mx.edu.chmd.transportechmd.networking.TransporteAPI
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel :ViewModel(){
    var userResultData: MutableLiveData<List<Usuario>>
    private var iTransporteService:ITransporte
    init {
        userResultData = MutableLiveData()
        iTransporteService = TransporteAPI.getCHMDService()!!
    }

    fun getUserDataResultObserver(): MutableLiveData<List<Usuario>> {
        return userResultData
    }

    fun getUserData(usr:String,pwd:String){
        val call = iTransporteService.iniciarSesion(usr,pwd)
        call.enqueue(object: Callback<List<Usuario>> {
            override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>) {
                if(response!=null){
                    userResultData.postValue(response.body())
                }

            }

            override fun onFailure(call: Call<List<Usuario>>, t: Throwable) {
                userResultData.postValue(null)
                Log.d("ERROR-DSG",t.localizedMessage)
            }

        })
    }


}