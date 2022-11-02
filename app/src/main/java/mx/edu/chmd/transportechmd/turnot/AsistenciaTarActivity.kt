package mx.edu.chmd.transportechmd.turnot

import android.app.AlertDialog
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.nfc.*
import android.nfc.tech.*
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.widget.Button
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_asistencia_man.*
import kotlinx.android.synthetic.main.view_enviar_comentario.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.edu.chmd.transportechmd.R
import mx.edu.chmd.transportechmd.SeleccionRutaActivity
import mx.edu.chmd.transportechmd.adapter.AsistenciaItemTarAdapter
import mx.edu.chmd.transportechmd.db.AsistenciaDAO
import mx.edu.chmd.transportechmd.db.TransporteDB
import mx.edu.chmd.transportechmd.model.Asistencia
import mx.edu.chmd.transportechmd.model.Comentario
import mx.edu.chmd.transportechmd.networking.ITransporte
import mx.edu.chmd.transportechmd.networking.TransporteAPI
import mx.edu.chmd.transportechmd.servicios.NetworkChangeReceiver
import mx.edu.chmd.transportechmd.utils.NFCDecrypt
import mx.edu.chmd.transportechmd.viewmodel.AsistenciaViewModel
import mx.edu.chmd.transportechmd.viewmodel.RutaViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AsistenciaTarActivity : AppCompatActivity() {
    var lstAsistencia:ArrayList<Asistencia> = ArrayList()
    var adapter:AsistenciaItemTarAdapter = AsistenciaItemTarAdapter()
    private lateinit var asistenciaViewModel: AsistenciaViewModel
    private lateinit var rutaViewModel: RutaViewModel
    private var sharedPreferences: SharedPreferences? = null
    private var networkChangeReceiver: NetworkChangeReceiver = NetworkChangeReceiver()
    lateinit var iTransporte: ITransporte
    private var nfcAdapter: NfcAdapter? = null
    private lateinit var nfcDecrypt: NFCDecrypt
    private val techList = arrayOf(
        arrayOf(
            NfcA::class.java.name,
            NfcB::class.java.name,
            NfcF::class.java.name,
            NfcV::class.java.name,
            IsoDep::class.java.name,
            MifareClassic::class.java.name,
            MifareUltralight::class.java.name, Ndef::class.java.name
        )
    )
    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        lstAsistencia.clear()


        val manager = getSystemService(Context.NFC_SERVICE) as NfcManager
        nfcAdapter = manager.defaultAdapter
        if(nfcAdapter!=null && nfcAdapter!!.isEnabled) {
            nfcAdapter!!.disableForegroundDispatch(this)
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(networkChangeReceiver)
    }
    var id_ruta:String=""
    public override fun onResume() {
        super.onResume()
        getAsistencia(id_ruta)
        //******************************
        //INICIO LECTURA NFC
        //******************************

        val manager = getSystemService(Context.NFC_SERVICE) as NfcManager
        nfcAdapter = manager.defaultAdapter
        if(nfcAdapter!=null && nfcAdapter!!.isEnabled){

            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_MUTABLE
            )
            // creating intent receiver for NFC events:
            val filter = IntentFilter()
            filter.addAction(NfcAdapter.ACTION_TAG_DISCOVERED)
            filter.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED)
            filter.addAction(NfcAdapter.ACTION_TECH_DISCOVERED)
            // enabling foreground dispatch for getting intent from NFC event:
            val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, arrayOf(filter), techList)
        }else{

        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent!!.action == NfcAdapter.ACTION_TAG_DISCOVERED) {
            Log.d("onNewIntent", "2")

            //if(getIntent().hasExtra(NfcAdapter.EXTRA_TAG)){
            val tagN = intent.getParcelableExtra<Parcelable>(NfcAdapter.EXTRA_TAG)
            if (tagN != null) {
                Log.d("LECTURA 0", "Parcelable OK")
                val msgs: Array<NdefMessage>
                val empty = ByteArray(0)
                val id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)
                val payload = nfcDecrypt.dumpTagData(tagN).toByteArray()
                val record = NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload)
                val msg = NdefMessage(arrayOf(record))
                msgs = arrayOf(msg)
                Log.d("LECTURA",nfcDecrypt.bytesToHexString(id))

                CoroutineScope(Dispatchers.IO).launch {
                    val db = TransporteDB.getInstance(application)
                    if(db.iAsistenciaDAO.existeTarjeta(nfcDecrypt.bytesToHexString(id),id_ruta)==1) {
                        db.iAsistenciaDAO.asisteTurnoTarNFC(
                            id_ruta,
                            nfcDecrypt.bytesToHexString(id),
                            getProcesable()
                        )
                        //Marcar en el servidor asistencia del alumno encontrado
                        val alumno = db.iAsistenciaDAO.getAlumnoByTarjeta(nfcDecrypt.bytesToHexString(id),id_ruta)
                        Log.d("LECTURA",alumno[0].idAlumno)
                        if(hayConexion())
                            CoroutineScope(Dispatchers.Main).launch {
                                enviarAsistencia(alumno[0].idAlumno, id_ruta)
                            }
                        Thread.sleep(2000)
                        getAsistencia(id_ruta)
                    }else{
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(applicationContext,"Esta tarjeta no existe en esta ruta",
                                Toast.LENGTH_LONG).show()
                        }

                    }
                }

                CoroutineScope(Dispatchers.Main).launch {
                    adapter.notifyDataSetChanged()
                }

            } else {

            }
            val messages1 = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            if (messages1 != null) {
                Log.d("LECTURA", "Found " + messages1.size + " NDEF messages")
            } else {
                Log.d("LECTURA", "Not EXTRA_NDEF_MESSAGES")
            }
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                Log.d("LECTURA:", "NfcAdapter.EXTRA_TAG")
                val messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                if (messages != null) {
                    Log.d("LECTURA", "Found " + messages.size + " NDEF messages")
                }
            } else {
                Log.d("LECTURA", "Write to an unformatted tag not implemented")
            }


            //mTextView.setText( "NFC Tag\n" + ByteArrayToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_TAG)));
        }
    }
    //******************************
    //FIN LECTURA NFC
    //******************************


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asistencia_tar)
        val SHARED:String=getString(R.string.spref)
        sharedPreferences = getSharedPreferences(SHARED, 0)
        val toolbar =
            findViewById<Toolbar>(R.id.tool_bar) // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar)
        asistenciaViewModel = ViewModelProvider(this)[AsistenciaViewModel::class.java]
        rutaViewModel = ViewModelProvider(this)[RutaViewModel::class.java]
        val db = TransporteDB.getInstance(this.application)
        iTransporte = TransporteAPI.getCHMDService()!!
        nfcDecrypt = NFCDecrypt()
        val tf = Typeface.createFromAsset(getAssets(),"fonts/Nunito-Bold.ttf")
        id_ruta = sharedPreferences!!.getString("idRuta","0").toString()
        val nomRuta = sharedPreferences!!.getString("nomRuta","--").toString()
        lblRuta.setText(nomRuta)
        lblRuta.typeface = tf
        btnCerrarRegistro.typeface = tf
        lblAscDesc.typeface = tf
        lblInasist.typeface = tf
        lblTotalInasist.typeface = tf
        lblTotales.typeface = tf

        //lstAsistencia.clear()
        btnCerrarRegistro.setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle("CHMD - Transporte")
                .setMessage(
                    "¿Desea efectuar el cierre de la ruta (subida de alumnos)?"
                )
                .setPositiveButton("Aceptar") { _, _ ->
                    //enviar reinicio de la asistencia

                    val db = TransporteDB.getInstance(this.application)
                    //cerrar registro en la base, ponerle estatus=1
                    CoroutineScope(Dispatchers.IO).launch {
                        db.iRutaDAO.cambiaEstatusRuta("1",id_ruta)
                    }
                    //enviar cierre de la ruta al server
                    cerrarRuta("1",id_ruta)
                    val intent = Intent(this@AsistenciaTarActivity,AsistenciaTarDropActivity::class.java)
                    startActivity(intent)
                    finish()

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
                Thread.sleep(2000)
                Log.d("TAREA C","llamada")
                swpContainer.isRefreshing=false
                getAsistencia2(id_ruta)
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


    fun reiniciarAsistencia(id_alumno:String, id_ruta: String){
        asistenciaViewModel.getAlumnoReiniciaAsistenciaResultObserver().observe(this){result->

        }
        asistenciaViewModel.enviaReiniciarAsistenciaAlumnoTar(id_alumno, id_ruta)
    }

    fun cerrarRuta(estatus:String, id_ruta: String){
        rutaViewModel.getCierreResultObserver().observe(this){result->

        }
        rutaViewModel.cerrarRuta(estatus,id_ruta)
    }

    fun enviarAsistencia(id_alumno:String, id_ruta: String){
        asistenciaViewModel.getAlumnoAsistenciaResultObserver().observe(this){result->

        }
        asistenciaViewModel.enviaAsistenciaAlumnoTar(id_alumno, id_ruta)
    }

    fun enviarAsistenciaCompleta(id_ruta: String){
        asistenciaViewModel.getSubidaCompletaResultObserver().observe(this){result->

        }
        asistenciaViewModel.enviaAsistenciaCompletaMan(id_ruta)
    }

    fun enviarComentario(id_ruta: String,c:String){
        asistenciaViewModel.getComentarioResultObserver().observe(this){r->

        }
        asistenciaViewModel.enviaComentario(id_ruta, c)
    }

    //fin

    fun getAsistencia(id_ruta:String)/*:ArrayList<Asistencia>*/{
        val db = TransporteDB.getInstance(this.application)
        var suben:Int=0
        var novan:Int=0
        var inasistencias:Int=0
        var totalAlumnos:Int=0
        //var lst:ArrayList<Asistencia> = ArrayList()
        lstAsistencia.clear()
        CoroutineScope(Dispatchers.IO).launch {
            var asistentes = db.iAsistenciaDAO.getAsistencia(id_ruta)
            totalAlumnos = db.iAsistenciaDAO.getTotalidadAsistenciaEnBus(id_ruta)
            asistentes.forEach{ alumno->
                if(alumno.ascenso_t=="1" && alumno.salida.equals("0"))
                    suben++
                if(alumno.ascenso_t=="1" && !alumno.salida.equals("0"))
                    novan++

                if(alumno.ascenso_t=="2")
                    inasistencias++

                lstAsistencia.add(Asistencia(alumno.tarjeta,alumno.ascenso, alumno.ascenso_t,
                    "",alumno.descenso,alumno.descenso_t,alumno.domicilio,
                    alumno.domicilio_s,"","",alumno.foto,alumno.grado,
                    alumno.grupo,alumno.horaManana,alumno.horaRegreso,alumno.idAlumno,
                    alumno.idRuta,alumno.idRuta,alumno.nivel,alumno.nombreAlumno,alumno.ordenIn,
                    alumno.ordenOut,alumno.salida,""))
            }

            //lst = lstAsistencia
            //adapter.notifyDataSetChanged()

        }

        CoroutineScope(Dispatchers.Main).launch{
            //adapter.clear()
            //adapter.addAll(lstAsistencia)

            if(suben + inasistencias == totalAlumnos - novan  && totalAlumnos>0){
                btnCerrarRegistro.setBackgroundResource(R.drawable.boton_redondeado)
                btnCerrarRegistro.isEnabled = true
                btnCerrarRegistro.text="Cerrar ruta"

            }



            adapter = AsistenciaItemTarAdapter(lstAsistencia,this@AsistenciaTarActivity)
            rvAsistencia.layoutManager = LinearLayoutManager(this@AsistenciaTarActivity)
            rvAsistencia.adapter = adapter
            //adapter.actualizarDatos(lstAsistencia)
            lblTotales.setText("$suben/${lstAsistencia.size}")
            lblTotalInasist.setText("$inasistencias")
        }
        //return lst
    }
    fun getAsistencia2(id_ruta:String)/*:ArrayList<Asistencia>*/{
        val db = TransporteDB.getInstance(this.application)

        //var lst:ArrayList<Asistencia> = ArrayList()
        CoroutineScope(Dispatchers.IO).launch {
            var asistentes = db.iAsistenciaDAO.getAsistencia(id_ruta)

            asistentes.forEach{ alumno->


                lstAsistencia.add(Asistencia(alumno.tarjeta,alumno.ascenso, alumno.ascenso_t,
                    "",alumno.descenso,alumno.descenso_t,alumno.domicilio,
                    alumno.domicilio_s,"","",alumno.foto,alumno.grado,
                    alumno.grupo,alumno.horaManana,alumno.horaRegreso,alumno.idAlumno,
                    alumno.idRuta,alumno.idRuta,alumno.nivel,alumno.nombreAlumno,alumno.ordenIn,
                    alumno.ordenOut,alumno.salida,""))
            }

            //lst = lstAsistencia
            //adapter.notifyDataSetChanged()

        }

        CoroutineScope(Dispatchers.Main).launch{
            //adapter.clear()
            //adapter.addAll(lstAsistencia)

            adapter = AsistenciaItemTarAdapter(lstAsistencia,this@AsistenciaTarActivity)
            rvAsistencia.layoutManager = LinearLayoutManager(this@AsistenciaTarActivity)
            rvAsistencia.adapter = adapter
            //adapter.actualizarDatos(lstAsistencia)

        }
        //return lst
    }
    fun buscarAlumno(id_ruta:String,nombre:String)/*:ArrayList<Asistencia>*/{
        val db = TransporteDB.getInstance(this.application)

        //var lst:ArrayList<Asistencia> = ArrayList()
        CoroutineScope(Dispatchers.IO).launch {
            var asistentes = db.iAsistenciaDAO.buscarAlumnos(id_ruta,nombre)

            asistentes.forEach{ alumno->


                lstAsistencia.add(Asistencia(alumno.tarjeta,alumno.ascenso, alumno.ascenso_t,
                    "",alumno.descenso,alumno.descenso_t,alumno.domicilio,
                    alumno.domicilio_s,"","",alumno.foto,alumno.grado,
                    alumno.grupo,alumno.horaManana,alumno.horaRegreso,alumno.idAlumno,
                    alumno.idRuta,alumno.idRuta,alumno.nivel,alumno.nombreAlumno,alumno.ordenIn,
                    alumno.ordenOut,alumno.salida,""))
            }

            //lst = lstAsistencia
            //adapter.notifyDataSetChanged()

        }

        CoroutineScope(Dispatchers.Main).launch{
            //adapter.clear()
            //adapter.addAll(lstAsistencia)


            adapter = AsistenciaItemTarAdapter(lstAsistencia,this@AsistenciaTarActivity)
            rvAsistencia.layoutManager = LinearLayoutManager(this@AsistenciaTarActivity)
            rvAsistencia.adapter = adapter
            //adapter.actualizarDatos(lstAsistencia)
        }
        //return lst
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_asistencia_subir, menu)

        // return true so that the menu pop up is opened
        return true
    }



    override fun onOptionsItemSelected(item: MenuItem):Boolean
    {
        if (item.itemId == R.id.action_back) {
            onBackPressed()
        }

        if (item.itemId == R.id.action_msj) {
            val btmComentarios = BottomSheetDialog(this@AsistenciaTarActivity)
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

            val dialog = Dialog(this@AsistenciaTarActivity)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.dialog_item_subida_masiva)
            val btnCancelarSubida = dialog.findViewById<Button>(R.id.btnCancelarSubida)
            val btnSubir = dialog.findViewById<Button>(R.id.btnSubir)
            val lblDialog = dialog.findViewById<TextView>(R.id.lblDialog)
            val tf = Typeface.createFromAsset(assets,"fonts/Nunito-Regular.ttf")
            btnCancelarSubida.typeface=tf
            btnSubir.typeface=tf
            lblDialog.typeface=tf
            btnCancelarSubida.setOnClickListener {
                dialog.dismiss()
            }
            btnSubir.setOnClickListener {
                enviarAsistenciaCompleta(id_ruta)
                val db = TransporteDB.getInstance(this.application)
                CoroutineScope(Dispatchers.IO).launch {
                    db.iAsistenciaDAO.asistenTodosTar(id_ruta,getProcesable())
                }
                recrear()
            }
            dialog.show()

            /*AlertDialog.Builder(this)
                .setTitle("CHMD - Transporte")
                .setMessage(
                    "¿Desea efectuar la subida de todos los alumnos a la ruta?"
                )
                .setPositiveButton("Aceptar") { _, _ ->
                    //enviar reinicio de la asistencia
                    enviarAsistenciaCompleta(id_ruta)
                    val db = TransporteDB.getInstance(this.application)
                    CoroutineScope(Dispatchers.IO).launch {
                        db.iAsistenciaDAO.asistenTodosTar(id_ruta,getProcesable())
                    }
                    recrear()
                }

                .setNegativeButton("Cancelar"){dialog,_->
                    dialog.dismiss()

                }

                .show()*/

        }
        return true
    }


    fun getProcesable(): Int {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return if(netInfo != null && netInfo.isConnectedOrConnecting)
            1
        else
            -1

    }

    fun hayConexion(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting

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