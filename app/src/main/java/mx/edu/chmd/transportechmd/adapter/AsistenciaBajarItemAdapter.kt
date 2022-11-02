package mx.edu.chmd.transportechmd.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.edu.chmd.transportechmd.R
import mx.edu.chmd.transportechmd.db.TransporteDB
import mx.edu.chmd.transportechmd.model.Asistencia
import mx.edu.chmd.transportechmd.turnom.AsistenciaManDropActivity


class AsistenciaBajarItemAdapter(var lstAsistencia:ArrayList<Asistencia>? = null, var c: Context?=null)
    : RecyclerView.Adapter<AsistenciaBajarItemAdapter.ViewHolder>()
{
    private var sharedPreferences: SharedPreferences? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgFotoEstudiante  : ImageView = view.findViewById(R.id.imgFotoEstudiante)
        val lblHora : TextView = view.findViewById(R.id.lblHora)
        val lblParada : TextView = view.findViewById(R.id.lblParada)
        val lblDireccion : TextView = view.findViewById(R.id.lblDireccion)
        val lblNombre : TextView = view.findViewById(R.id.lblNombre)
        val lblInasistencia : TextView = view.findViewById(R.id.lblInasistencia)
        val btnInasistencia : RelativeLayout = view.findViewById(R.id.btnInasistencia)
        val llContenedor : LinearLayout = view.findViewById(R.id.llContenedor)
        //val icoEstado:ImageView = view.findViewById(R.id.icoEstado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_asistencia, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var items = lstAsistencia!![position]
        val SHARED:String=c!!.getString(R.string.spref)
        sharedPreferences = c!!.getSharedPreferences(SHARED, 0)
        val tf = Typeface.createFromAsset(holder.lblHora.context.getAssets(),"fonts/Nunito-Bold.ttf")
        holder.lblHora.text = items.hora_manana
        holder.lblParada.text = items.orden_in
        holder.lblDireccion.text = items.domicilio
        holder.lblNombre.text = items.nombre
        holder.lblInasistencia.typeface = tf
        holder.lblHora.typeface = tf
        holder.lblParada.typeface = tf
        holder.lblDireccion.typeface = tf
        holder.lblNombre.typeface = tf
        holder.btnInasistencia.visibility = View.GONE

        //Ha subido

/*
        holder.btnInasistencia.setOnClickListener {

            val editor:SharedPreferences.Editor = sharedPreferences!!.edit()
            editor.putString("idRuta",items.id_ruta_h)
            editor.apply()
            if(c is AsistenciaManActivity){

                AlertDialog.Builder(c)
                    .setTitle("CHMD - Transporte")
                    .setMessage(
                        "¿Desea registrar la inasistencia de ${items.nombre}?"
                    )
                    .setPositiveButton("Aceptar") { _, _ ->
                        alumnoNoAsiste(items.id_alumno,items.id_ruta_h)
                        (c as AsistenciaManActivity).enviarInasistencia(
                            items.id_alumno,
                            items.id_ruta_h
                        )
                        (c as AsistenciaManActivity).recrear()
                        notifyDataSetChanged()
                    }

                    .setNegativeButton("Cancelar"){dialog,_->
                        dialog.dismiss()
                        notifyDataSetChanged()
                    }

                    .show()


            }

        }
  */
        if(items.ascenso == "0" && items.descenso == "0"){
            holder.llContenedor.setBackgroundColor(Color.parseColor("#ffffff"))
        }
        if(items.ascenso == "1" && items.descenso == "0"){
            holder.llContenedor.setBackgroundColor(Color.parseColor("#ffff85"))
        }
        if(items.ascenso == "1" && items.descenso == "1"){
            holder.llContenedor.setBackgroundColor(Color.parseColor("#50C878"))
        }
        if(items.ascenso == "0" && items.descenso == "0"){
            holder.llContenedor.setBackgroundColor(Color.parseColor("#ffffff"))
        }

        if(items.asistencia=="0"){
            holder.imgFotoEstudiante.isEnabled=false
            holder.llContenedor.setBackgroundColor(Color.parseColor("#ff4122"))
            holder.btnInasistencia.setBackgroundColor(Color.parseColor("#ff4122"))
            holder.lblInasistencia.text="No asistió"

        }

        //Entró caminando (color aqua)
        if(items.asistencia=="2"){
            holder.imgFotoEstudiante.isEnabled=false
            holder.llContenedor.setBackgroundColor(Color.parseColor("#7adfb5"))
            holder.btnInasistencia.setBackgroundColor(Color.parseColor("#7adfb5"))
            holder.lblInasistencia.text="Entró por otro medio"
        }

        if(items.salida=="3"){
            holder.llContenedor.setBackgroundColor(Color.parseColor("#f1b04c"))
        }
        if(items.salida=="2"){
            holder.llContenedor.setBackgroundColor(Color.parseColor("#89cff0"))
        }
        //No ha salido, pero está por salir
        /*if(items.salida=="3"){
            holder.icoEstado.setImageResource(R.drawable.huellas)
            notifyDataSetChanged()
        }
        //Ya salió
        if(items.salida=="2"){
            holder.icoEstado.setImageResource(R.drawable.footstep_verde)
            notifyDataSetChanged()
        }*/

        holder.imgFotoEstudiante.setOnClickListener {
            //La ruta va comenzando, recogiendo niños en sus casas
           // if(items.estatus=="0"){

            if(items.ascenso=="0" && items.descenso=="0"){
                CoroutineScope(Dispatchers.IO).launch{
                    alumnoBaja(items.id_alumno,items.id_ruta_h)
                }

                if(c is AsistenciaManDropActivity){
                    (c as AsistenciaManDropActivity).recrear()
                    if(hayConexion())
                        (c as AsistenciaManDropActivity).enviarBajada(items.id_alumno,items.id_ruta_h)
                }

            }


            //Bajar alumno
            if(items.ascenso=="1" && items.descenso=="0"){
                CoroutineScope(Dispatchers.IO).launch{
                   alumnoBaja(items.id_alumno,items.id_ruta_h)
                }

                if(c is AsistenciaManDropActivity){
                    (c as AsistenciaManDropActivity).recrear()
                    if(hayConexion())
                        (c as AsistenciaManDropActivity).enviarBajada(items.id_alumno,items.id_ruta_h)
                }

            }

                if(items.ascenso=="1" && items.descenso=="1"){

                    if(c is AsistenciaManDropActivity){

                        AlertDialog.Builder(c)
                            .setTitle("CHMD - Transporte")
                            .setMessage(
                                "¿Desea reiniciar la subida de ${items.nombre}?"
                            )
                            .setPositiveButton("Aceptar") { _, _ ->

                                //enviar reinicio de la asistencia

                                val editor:SharedPreferences.Editor = sharedPreferences!!.edit()
                                editor.putString("idRuta",items.id_ruta_h)
                                editor.apply()
                                if(c is AsistenciaManDropActivity){
                                    alumnoReiniciaAsistencia(items.id_alumno,items.id_ruta_h)
                                    if(hayConexion())
                                        (c as AsistenciaManDropActivity).reiniciarSubida(items.id_alumno,items.id_ruta_h)
                                    (c as AsistenciaManDropActivity).recrear()

                                }
                                notifyDataSetChanged()

                            }

                            .setNegativeButton("Cancelar"){dialog,_->
                                dialog.dismiss()
                                notifyDataSetChanged()
                            }

                            .show()


                    }






                }
            //}

        }

        var foto = items.foto.replace(
            "C:\\IDCARDDESIGN\\CREDENCIALES\\alumnos\\",
            "http://chmd.chmd.edu.mx:65083/CREDENCIALES/alumnos/"
        )
        foto = foto.replace(" ", "%20")


        try {
            Glide.with(c!!)
                .load(foto) // image url
                .placeholder(R.drawable.usuario) // any placeholder to load at start
                .error(R.drawable.usuario) // any image in case of error
                .circleCrop()
                .into(holder.imgFotoEstudiante)
        } catch (ex: Exception) {
        }







    }



    fun actualizarDatos(ls: ArrayList<Asistencia>){
        try {
            lstAsistencia!!.clear()
            lstAsistencia!!.addAll(ls)
            notifyDataSetChanged()
        }catch (e:Exception){}
    }


    override fun getItemCount(): Int {
        return lstAsistencia!!.size
    }

    fun alumnoAsiste(id_alumno:String, id_ruta:String){
        val db = TransporteDB.getInstance(c!!)
        CoroutineScope(Dispatchers.IO).launch {
            db.iAsistenciaDAO.asisteTurnoMan(id_ruta,id_alumno,getProcesable())

        }
        CoroutineScope(Dispatchers.Main).launch {
            notifyDataSetChanged()
        }
    }

    fun alumnoNoAsiste(id_alumno:String, id_ruta:String){
        val db = TransporteDB.getInstance(c!!)
        CoroutineScope(Dispatchers.IO).launch {
            db.iAsistenciaDAO.noAsisteTurnoMan(id_ruta,id_alumno,getProcesable())
        }

        CoroutineScope(Dispatchers.Main).launch {
            (c as AsistenciaManDropActivity).getAsistencia(id_ruta)
        }
    }

    fun alumnoReiniciaAsistencia(id_alumno:String, id_ruta:String){
        val db = TransporteDB.getInstance(c!!)
        CoroutineScope(Dispatchers.IO).launch {
            db.iAsistenciaDAO.reiniciaSubidaMan(id_ruta,id_alumno,getProcesable())

        }
        CoroutineScope(Dispatchers.Main).launch {
            notifyDataSetChanged()
        }
    }

    fun alumnoBaja(id_alumno:String, id_ruta:String){
        val db = TransporteDB.getInstance(c!!)
        CoroutineScope(Dispatchers.IO).launch {
            db.iAsistenciaDAO.bajaTurnoMan(id_ruta,id_alumno,getProcesable())

        }
        CoroutineScope(Dispatchers.Main).launch {
            notifyDataSetChanged()
        }
    }


    fun getProcesable(): Int {
        val cm = c!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return if(netInfo != null && netInfo.isConnectedOrConnecting)
            1
        else
            -1

    }

    fun hayConexion(): Boolean {
        val cm = c!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting

    }


}