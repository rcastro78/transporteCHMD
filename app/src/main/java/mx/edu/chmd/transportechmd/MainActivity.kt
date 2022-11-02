package mx.edu.chmd.transportechmd

import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.edu.chmd.transportechmd.db.AsistenciaDAO
import mx.edu.chmd.transportechmd.db.RutaDAO
import mx.edu.chmd.transportechmd.db.TransporteDB
import mx.edu.chmd.transportechmd.model.Asistencia
import mx.edu.chmd.transportechmd.model.Ruta
import mx.edu.chmd.transportechmd.model.Usuario
import mx.edu.chmd.transportechmd.networking.ITransporte
import mx.edu.chmd.transportechmd.networking.TransporteAPI
import mx.edu.chmd.transportechmd.servicios.NetworkChangeReceiver
import mx.edu.chmd.transportechmd.viewmodel.LoginViewModel
import mx.edu.chmd.transportechmd.viewmodel.RutaViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    var email:String=""
    var clave:String=""
    private lateinit var loginViewModel:LoginViewModel
    private lateinit var rutaViewModel: RutaViewModel
    private var sharedPreferences: SharedPreferences? = null
    private lateinit var iTransporteService: ITransporte
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
        setContentView(R.layout.activity_main)
        val SHARED:String=getString(R.string.spref)
        sharedPreferences = getSharedPreferences(SHARED, 0)
        iTransporteService = TransporteAPI.getCHMDService()!!
        val tf = Typeface.createFromAsset(assets,"fonts/Nunito-Bold.ttf")
        txtEmail.typeface = tf
        txtPassword.typeface = tf
        lblHeader.typeface = tf
        btnLogin.typeface = tf
        lblEstado.typeface = tf
        txtEmail.setText("lnabor@chmd.edu.mx")
        txtPassword.setText("auxiliar2015")
        //txtEmail.setText("ocana@chmd.edu.mx")
        //txtPassword.setText("auxiliar1997")

        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        rutaViewModel = ViewModelProvider(this)[RutaViewModel::class.java]
        //asistenciaViewModel = ViewModelProvider(this)[AsistenciaViewModel::class.java]
        btnLogin.setOnClickListener{
            btnLogin.isEnabled = false
            email = txtEmail.text.toString()
            clave = txtPassword.text.toString()
            if(email.isNotEmpty() && clave.isNotEmpty()){
              getUserData(email,clave)
            }else{
                Toast.makeText(applicationContext,"Ambos campos son obligatorios",Toast.LENGTH_LONG).show()
            }
        }

    }


    fun getUserData(usr:String,pwd:String){
        val call = iTransporteService.iniciarSesion(usr,pwd)
        call.enqueue(object: Callback<List<Usuario>> {
            override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>) {
                if(response.code()==200) {
                    response.body()!!.forEach { usuario ->

                        CoroutineScope(Dispatchers.IO).launch {
                            getRutasAsignadas(usuario.id_usuario)
                        }
                    }

                    if (response.body()!!.size<=5) {
                        lblEstado.visibility=View.VISIBLE
                        lblEstado.text="Usuario no reconocido"
                        btnLogin.isEnabled=true
                    }

                }

            }

            override fun onFailure(call: Call<List<Usuario>>, t: Throwable) {

                Log.d("ERROR-DSG",t.localizedMessage)
            }

        })
    }





    suspend fun getAsistenciaRutaMan(idRuta:String){
        val db = TransporteDB.getInstance(this.application)
        val call = iTransporteService.getAsistenciaMan(idRuta)
        CoroutineScope(Dispatchers.IO).launch {
            db.iAsistenciaDAO.eliminaAsistencia(idRuta)
        }
        call.enqueue(object:Callback<List<Asistencia>>{
            override fun onResponse(
                call: Call<List<Asistencia>>,
                response: Response<List<Asistencia>>
            ) {
                if(response!=null){
                    val asistencia = response.body()
                    asistencia!!.forEach { alumno->
                        val a = AsistenciaDAO(0,idRuta,alumno.tarjeta,alumno.id_alumno,
                        alumno.nombre,alumno.domicilio,alumno.hora_manana,"",
                        alumno.ascenso,alumno.descenso,alumno.domicilio_s,alumno.grupo,alumno.grado,
                        alumno.nivel,alumno.foto,false,false,alumno.ascenso_t,alumno.descenso_t,
                        alumno.salida,alumno.orden_in,"",false,false,0,alumno.asistencia)

                        Log.d("ALUMNOS",alumno.nombre)

                        CoroutineScope(Dispatchers.IO).launch {
                            db.iAsistenciaDAO.guardaAsistencia(a)
                        }

                    }

                    if(asistencia.size<=0){
                        lblEstado.visibility = View.VISIBLE
                        lblEstado.setText("No se puede descargar el listado de alumnos. Intente más tarde.")
                    }




                }

                CoroutineScope(Dispatchers.Main).launch {
                    lblEstado.visibility = View.VISIBLE
                    lblEstado.setText("Descargando alumnos...")

                }
            }

            override fun onFailure(call: Call<List<Asistencia>>, t: Throwable) {
                Log.d("ALUMNOS",t.message!!)
            }

        })

    }
    suspend fun getAsistenciaRutaTar(idRuta:String){
        val db = TransporteDB.getInstance(this.application)
        val call = iTransporteService.getAsistenciaTar(idRuta)
        CoroutineScope(Dispatchers.IO).launch {
            db.iAsistenciaDAO.eliminaAsistencia(idRuta)
        }
        call.enqueue(object:Callback<List<Asistencia>>{
            override fun onResponse(
                call: Call<List<Asistencia>>,
                response: Response<List<Asistencia>>
            ) {
                if(response!=null){
                    val asistencia = response.body()
                    asistencia!!.forEach { alumno->
                        var horaReg=""
                        if(alumno.hora_regreso == null){
                            horaReg = ""
                        }else{
                            horaReg = alumno.hora_regreso
                        }


                        var tarjeta=""
                        if(alumno.tarjeta == null){
                            tarjeta = ""
                        }else{
                            tarjeta = alumno.tarjeta
                        }


                        var orden_out=""
                        if(alumno.orden_out == null){
                            tarjeta = ""
                        }else{
                            orden_out = alumno.orden_out
                        }


                    val a = AsistenciaDAO(0,idRuta,tarjeta,alumno.id_alumno,
                            alumno.nombre,alumno.domicilio,alumno.hora_manana,horaReg,
                            alumno.ascenso,alumno.descenso,alumno.domicilio_s,alumno.grupo,alumno.grado,
                            alumno.nivel,alumno.foto,false,false,alumno.ascenso_t,alumno.descenso_t,
                            alumno.salida,alumno.orden_in,orden_out,false,false,0,alumno.asistencia)
                        CoroutineScope(Dispatchers.IO).launch {
                            db.iAsistenciaDAO.guardaAsistencia(a)
                        }
                    }
                }

                CoroutineScope(Dispatchers.Main).launch {

                    //lblEstado.visibility = View.VISIBLE
                    //lblEstado.setText("Descargando alumnos...")
                    //lblEstado.animate().translationYBy(0.2f)

                }
                val intent = Intent(this@MainActivity,SeleccionRutaActivity::class.java)
                startActivity(intent)
            }

            override fun onFailure(call: Call<List<Asistencia>>, t: Throwable) {
                Log.d("ALUMNOS",t.message!!)
            }

        })

    }

    suspend fun getRutasAsignadas(aux_id:String){
        val db = TransporteDB.getInstance(this.application)
        val call = iTransporteService.getRutaTransporte(aux_id)
        CoroutineScope(Dispatchers.IO).launch {
            db.iRutaDAO.delete()
        }
        call.enqueue(object: Callback<List<Ruta>> {
            override fun onResponse(call: Call<List<Ruta>>, response: Response<List<Ruta>>) {
                if(response!=null){
                    val rutas = response.body()
                    rutas!!.forEach { ruta->
                        val r = RutaDAO(0,ruta.idRutaH,ruta.nombreRuta,ruta.camion,ruta.turno,ruta.tipoRuta,ruta.estatus)
                        Log.d("RUTAS",ruta.nombreRuta)

                        if(ruta.turno.equals("1"))
                            CoroutineScope(Dispatchers.IO).launch {
                                getAsistenciaRutaMan(ruta.idRutaH)
                            }
                        if(ruta.turno.equals("2"))
                            CoroutineScope(Dispatchers.IO).launch {
                                getAsistenciaRutaTar(ruta.idRutaH)
                            }

                        CoroutineScope(Dispatchers.IO).launch {
                            db.iRutaDAO.guardaRutas(r)
                        }


                        }
                    CoroutineScope(Dispatchers.Main).launch {
                        lblEstado.visibility = View.VISIBLE
                        lblEstado.setText("Descargando rutas...")
                    }

                    if(rutas.size<=0){
                        CoroutineScope(Dispatchers.Main).launch {
                            lblEstado.visibility = View.VISIBLE
                            lblEstado.setText("No hay rutas para descargar. Intente más tarde")
                        }
                    }

                }

            }

            override fun onFailure(call: Call<List<Ruta>>, t: Throwable) {

                Log.d("ERROR-DSG",t.localizedMessage)
            }

        })


    /*rutaViewModel.getUserDataResultObserver().observe(this){rutas->
            rutas.forEach { ruta->
                Log.d("RUTAS",ruta.nombreRuta)
            }
        }
        rutaViewModel.getRuta(aux_id)*/
    }





}