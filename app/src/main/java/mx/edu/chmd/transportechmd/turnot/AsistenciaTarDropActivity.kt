package mx.edu.chmd.transportechmd.turnot

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Typeface
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_asistencia_tar_drop.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.edu.chmd.transportechmd.R
import mx.edu.chmd.transportechmd.SeleccionRutaActivity
import mx.edu.chmd.transportechmd.adapter.AsistenciaBajarItemTarAdapter
import mx.edu.chmd.transportechmd.db.AsistenciaDAO
import mx.edu.chmd.transportechmd.db.TransporteDB
import mx.edu.chmd.transportechmd.model.Asistencia
import mx.edu.chmd.transportechmd.model.Comentario
import mx.edu.chmd.transportechmd.networking.ITransporte
import mx.edu.chmd.transportechmd.networking.TransporteAPI
import mx.edu.chmd.transportechmd.servicios.NetworkChangeReceiver
import mx.edu.chmd.transportechmd.viewmodel.AsistenciaViewModel
import mx.edu.chmd.transportechmd.viewmodel.RutaViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//RUTA 4: Final, dejar a los niños en sus casas
class AsistenciaTarDropActivity : AppCompatActivity() {
    var lstAsistencia:ArrayList<Asistencia> = ArrayList()
    var adapter: AsistenciaBajarItemTarAdapter = AsistenciaBajarItemTarAdapter()
    private lateinit var asistenciaViewModel: AsistenciaViewModel
    private lateinit var rutaViewModel: RutaViewModel
    private var sharedPreferences: SharedPreferences? = null
    var id_ruta:String=""
    lateinit var iTransporte: ITransporte
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
    public override fun onResume() {
        super.onResume()
        getAsistencia(id_ruta)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asistencia_tar_drop)
        val SHARED:String=getString(R.string.spref)
        val db = TransporteDB.getInstance(this.application)
        sharedPreferences = getSharedPreferences(SHARED, 0)
        val toolbar =
            findViewById<Toolbar>(R.id.tool_bar) // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar)
        iTransporte = TransporteAPI.getCHMDService()!!
        asistenciaViewModel = ViewModelProvider(this)[AsistenciaViewModel::class.java]
        rutaViewModel = ViewModelProvider(this)[RutaViewModel::class.java]
        val tf = Typeface.createFromAsset(getAssets(),"fonts/Nunito-Bold.ttf")
        id_ruta = sharedPreferences!!.getString("idRuta","0").toString()
        val nomRuta = sharedPreferences!!.getString("nomRuta","--").toString()
        lblRuta.setText(nomRuta)
        lblRuta.typeface = tf
        btnCerrarRegistro.typeface = tf
        lblAscDesc.typeface = tf
        lblTotales.typeface = tf

        //lstAsistencia.clear()
        btnCerrarRegistro.setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle("CHMD - Transporte")
                .setMessage(
                    "¿Desea efectuar el cierre de la ruta (bajada de alumnos)?"
                )
                .setPositiveButton("Aceptar") { _, _ ->

                    //val db = TransporteDB.getInstance(this.application)
                    //cerrar registro en la base, ponerle estatus=1
                    CoroutineScope(Dispatchers.IO).launch {
                        db.iRutaDAO.cambiaEstatusRuta("2",id_ruta)
                    }
                    //enviar cierre de la ruta al server
                    cerrarRuta("2",id_ruta)
                    //recrear()
                    val intent = Intent(this@AsistenciaTarDropActivity, SeleccionRutaActivity::class.java)
                    startActivity(intent)
                }

                .setNegativeButton("Cancelar"){dialog,_->
                    dialog.dismiss()

                }

                .show()






        }


        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(text: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(text: String?): Boolean {
                if(text!!.isNotEmpty()){
                    lstAsistencia.clear()
                    buscarAlumno(id_ruta,text)
                }else{
                    lstAsistencia.clear()
                    getAsistencia(id_ruta)
                }
                return true
            }

        })


        swpContainer.setOnRefreshListener {
            swpContainer.isRefreshing=true
            //borrar data
            lstAsistencia.clear()
            rvAsistencia.adapter = null
            //borrar de la base por idRuta y descargar de nuevo
            val a = CoroutineScope(Dispatchers.IO.limitedParallelism(1)).launch {
                db.iAsistenciaDAO.eliminaAsistencia(id_ruta)
                Log.d("TAREA A","llamada")
                getAsistenciaRutaTar(id_ruta)
                Thread.sleep(3500)
                Log.d("TAREA B","llamada")
                getAsistencia(id_ruta)
                Thread.sleep(2000)
                Log.d("TAREA C","llamada")
                swpContainer.isRefreshing=false
            }

        }

    }


    //Funciones para ser llamadas desde el recyclerview

    fun recrear(){
        finish()
        startActivity(intent)
    }

    suspend fun getAsistenciaRutaTar(idRuta:String){
        val db = TransporteDB.getInstance(this.application)
        val call = iTransporte.getAsistenciaTar(idRuta)
        CoroutineScope(Dispatchers.IO).launch {
            db.iAsistenciaDAO.eliminaAsistencia(idRuta)
        }
        call.enqueue(object: Callback<List<Asistencia>> {
            override fun onResponse(
                call: Call<List<Asistencia>>,
                response: Response<List<Asistencia>>
            ) {
                if(response!=null){
                    val asistencia = response.body()
                    asistencia!!.forEach { alumno->
                        val a = AsistenciaDAO(0,idRuta,"",alumno.id_alumno,
                            alumno.nombre,alumno.domicilio,alumno.hora_manana,alumno.hora_regreso,
                            alumno.ascenso,alumno.descenso,alumno.domicilio_s,alumno.grupo,alumno.grado,
                            alumno.nivel,alumno.foto,false,false,alumno.ascenso_t,alumno.descenso_t,
                            alumno.salida,alumno.orden_in,alumno.orden_out,false,false,0,alumno.asistencia)
                        CoroutineScope(Dispatchers.IO).launch {
                            db.iAsistenciaDAO.guardaAsistencia(a)
                        }

                    }


                }


            }

            override fun onFailure(call: Call<List<Asistencia>>, t: Throwable) {

            }

        })

    }

    fun cerrarRuta(estatus:String, id_ruta: String){
        rutaViewModel.getCierreResultObserver().observe(this){result->

        }
        rutaViewModel.cerrarRuta(estatus,id_ruta)
    }


    fun enviarBajada(id_alumno:String, id_ruta: String){
        asistenciaViewModel.getAlumnoAsistenciaResultObserver().observe(this){result->

        }
        asistenciaViewModel.enviaDescensoAlumnoTar(id_alumno, id_ruta)
    }

    fun reiniciarBajada(id_alumno:String, id_ruta: String){
        asistenciaViewModel.getAlumnoReiniciaAsistenciaResultObserver().observe(this){result->

        }
        asistenciaViewModel.enviaReinicioBajadaAlumnoTar(id_alumno, id_ruta)
    }

    fun enviarComentario(id_ruta: String,c:String){
        asistenciaViewModel.getComentarioResultObserver().observe(this){r->

        }
        asistenciaViewModel.enviaComentario(id_ruta, c)
    }

    /*fun enviarAsistencia(id_alumno:String, id_ruta: String){
        asistenciaViewModel.getAlumnoAsistenciaResultObserver().observe(this){result->

        }
        asistenciaViewModel.enviaAsistenciaAlumnoMan(id_alumno, id_ruta)
    }

    fun enviarAsistenciaCompleta(id_ruta: String){
        asistenciaViewModel.getSubidaCompletaResultObserver().observe(this){result->

        }
        asistenciaViewModel.enviaAsistenciaCompletaMan(id_ruta)
    }

    fun enviarInasistencia(id_alumno:String, id_ruta: String){
        asistenciaViewModel.getAlumnoInasistenciaResultObserver().observe(this){result->

        }
        asistenciaViewModel.enviaInasistenciaAlumnoMan(id_alumno, id_ruta)
    }*/

    //fin

    fun getAsistencia(id_ruta:String)/*:ArrayList<Asistencia>*/{
        val db = TransporteDB.getInstance(this.application)
        var bajan:Int=0
        var novan:Int=0
        var totalAlumnos:Int=0
        //var lst:ArrayList<Asistencia> = ArrayList()
        CoroutineScope(Dispatchers.IO).launch {
            val asistentes = db.iAsistenciaDAO.getAlumnosBajarTarde(id_ruta)
            totalAlumnos = asistentes.size
            asistentes.forEach{ alumno->
                if(alumno.descenso_t=="1")
                    bajan++

                if(alumno.ascenso_t=="1" && !alumno.salida.equals("0"))
                    novan++

                lstAsistencia.add(
                    Asistencia(alumno.ascenso, alumno.ascenso_t,
                        "",alumno.descenso,alumno.descenso_t,alumno.domicilio,
                        alumno.domicilio_s,"","",alumno.foto,alumno.grado,
                        alumno.grupo,alumno.horaManana,alumno.horaRegreso,alumno.idAlumno,
                        alumno.idRuta,alumno.idRuta,alumno.nivel,alumno.nombreAlumno,alumno.ordenIn,
                        alumno.ordenOut,alumno.salida,"")
                )
            }

            //lst = lstAsistencia
            //adapter.notifyDataSetChanged()

        }

        CoroutineScope(Dispatchers.Main).launch{
            //adapter.clear()
            //adapter.addAll(lstAsistencia)

            if(bajan == totalAlumnos - novan){
                btnCerrarRegistro.setBackgroundResource(R.drawable.boton_redondeado)
                btnCerrarRegistro.isEnabled = true
                btnCerrarRegistro.text="Cerrar registro"
            }

            lblTotales.setText("$bajan/${lstAsistencia.size}")

            adapter = AsistenciaBajarItemTarAdapter(lstAsistencia,this@AsistenciaTarDropActivity)
            rvAsistencia.layoutManager = LinearLayoutManager(this@AsistenciaTarDropActivity)
            rvAsistencia.adapter = adapter
            //adapter.actualizarDatos(lstAsistencia)
        }
        //return lst
    }
    fun buscarAlumno(id_ruta:String,nombre:String)/*:ArrayList<Asistencia>*/{
        val db = TransporteDB.getInstance(this.application)
        var bajan:Int=0
        var novan:Int=0
        var totalAlumnos:Int=0
        //var lst:ArrayList<Asistencia> = ArrayList()
        CoroutineScope(Dispatchers.IO).launch {
            val asistentes = db.iAsistenciaDAO.buscarAlumnos(id_ruta,nombre)
            totalAlumnos = asistentes.size
            asistentes.forEach{ alumno->
                lstAsistencia.add(
                    Asistencia(alumno.ascenso, alumno.ascenso_t,
                        "",alumno.descenso,alumno.descenso_t,alumno.domicilio,
                        alumno.domicilio_s,"","",alumno.foto,alumno.grado,
                        alumno.grupo,alumno.horaManana,alumno.horaRegreso,alumno.idAlumno,
                        alumno.idRuta,alumno.idRuta,alumno.nivel,alumno.nombreAlumno,alumno.ordenIn,
                        alumno.ordenOut,alumno.salida,"")
                )
            }

            //lst = lstAsistencia
            //adapter.notifyDataSetChanged()

        }

        CoroutineScope(Dispatchers.Main).launch{
            //adapter.clear()
            //adapter.addAll(lstAsistencia)

            if(bajan == totalAlumnos - novan){
                btnCerrarRegistro.setBackgroundResource(R.drawable.boton_redondeado)
                btnCerrarRegistro.isEnabled = true
                btnCerrarRegistro.text="Cerrar registro"
            }

            lblTotales.setText("$bajan/${lstAsistencia.size}")

            adapter = AsistenciaBajarItemTarAdapter(lstAsistencia,this@AsistenciaTarDropActivity)
            rvAsistencia.layoutManager = LinearLayoutManager(this@AsistenciaTarDropActivity)
            rvAsistencia.adapter = adapter
            //adapter.actualizarDatos(lstAsistencia)
        }
        //return lst
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_asistencia_bajar, menu)

        // return true so that the menu pop up is opened
        return true
    }



    override fun onOptionsItemSelected(item: MenuItem):Boolean
    {
        if (item.itemId == R.id.action_back) {
            onBackPressed()
        }

        if (item.itemId == R.id.action_msj) {
            val btmComentarios = BottomSheetDialog(this@AsistenciaTarDropActivity)
            btmComentarios.setContentView(R.layout.view_enviar_comentario)
            val txtComentario: TextView? = btmComentarios.findViewById(R.id.txtComentario)
            val btnComment: Button? = btmComentarios.findViewById(R.id.btnComment)
            var c = ""
            //Recuperar el comentario de la ruta
            CoroutineScope(Dispatchers.IO).launch {
                getComentario(id_ruta,txtComentario!!)

            }

            CoroutineScope(Dispatchers.Main).launch{
                txtComentario!!.text = c
            }

            btnComment!!.setOnClickListener {
                //Enviar comentario de la ruta
                if(txtComentario!!.text.isNotEmpty()){
                    val c = txtComentario.text.toString()
                    enviarComentario(id_ruta,c)

                }
                btmComentarios.dismiss()
            }
            btmComentarios.show()
        }

        if (item.itemId == R.id.action_subir) {
            AlertDialog.Builder(this)
                .setTitle("CHMD - Transporte")
                .setMessage(
                    "¿Desea efectuar la bajada de todos los alumnos de la ruta?"
                )
                .setPositiveButton("Aceptar") { _, _ ->
                    //enviar reinicio de la asistencia
                    //enviarAsistenciaCompleta(id_ruta)
                    val db = TransporteDB.getInstance(this.application)
                    CoroutineScope(Dispatchers.IO).launch {
                        db.iAsistenciaDAO.bajanTodosTar(id_ruta,getProcesable())
                    }
                    recrear()
                }

                .setNegativeButton("Cancelar"){dialog,_->
                    dialog.dismiss()

                }

                .show()

        }
        return true
    }

    fun getProcesable(): Int {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return if(netInfo != null && netInfo.isConnectedOrConnecting)
            1
        else
            0

    }

    fun getComentario(id_ruta: String,txt:TextView){
        var comentario:String=""
        iTransporte.getComentario(id_ruta)
            .enqueue(object : Callback<List<Comentario>> {
                override fun onResponse(call: Call<List<Comentario>>, response: Response<List<Comentario>>) {

                    if (response != null) {
                        response.body()!!.forEach { c->
                            txt.setText(c.comentario)
                        }
                    }

                }

                override fun onFailure(call: Call<List<Comentario>>, t: Throwable) {
                    comentario=t.message!!
                }

            })

    }

}