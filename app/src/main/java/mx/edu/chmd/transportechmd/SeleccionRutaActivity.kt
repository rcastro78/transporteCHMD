package mx.edu.chmd.transportechmd

import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_seleccion_ruta.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.edu.chmd.transportechmd.adapter.SeleccionRutaAdapter
import mx.edu.chmd.transportechmd.db.RutaDAO
import mx.edu.chmd.transportechmd.db.TransporteDB
import mx.edu.chmd.transportechmd.model.Ruta
import mx.edu.chmd.transportechmd.servicios.NetworkChangeReceiver

class SeleccionRutaActivity : AppCompatActivity() {
    var lstRutas:ArrayList<Ruta> = ArrayList()
    private var sharedPreferences: SharedPreferences? = null
    private var networkChangeReceiver: NetworkChangeReceiver = NetworkChangeReceiver()

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(networkChangeReceiver)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccion_ruta)
        val SHARED:String=getString(R.string.spref)
        sharedPreferences = getSharedPreferences(SHARED, 0)
        getRutas()
    }


    fun getRutas(){
        val db = TransporteDB.getInstance(this.application)
        CoroutineScope(Dispatchers.IO).launch {
            val rutasActivas = db.iRutaDAO.getRutasActivas()
            rutasActivas.forEach { ruta->
                lstRutas.add(Ruta(ruta.idRuta,ruta.nombre,ruta.camion,ruta.turno,ruta.tipoRuta,ruta.estatus))
            }

        }
        CoroutineScope(Dispatchers.Main).launch{
            val adapter = SeleccionRutaAdapter(lstRutas,this@SeleccionRutaActivity)
            rvRutas.layoutManager = LinearLayoutManager(this@SeleccionRutaActivity)
            rvRutas.adapter = adapter

        }

    }
}