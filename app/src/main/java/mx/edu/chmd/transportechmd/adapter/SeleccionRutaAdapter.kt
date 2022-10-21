package mx.edu.chmd.transportechmd.adapter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.edu.chmd.transportechmd.R
import mx.edu.chmd.transportechmd.db.TransporteDB
import mx.edu.chmd.transportechmd.model.Ruta
import mx.edu.chmd.transportechmd.turnom.AsistenciaManActivity
import mx.edu.chmd.transportechmd.turnom.AsistenciaManDropActivity
import mx.edu.chmd.transportechmd.turnot.AsistenciaTarActivity
import mx.edu.chmd.transportechmd.turnot.AsistenciaTarDropActivity

class SeleccionRutaAdapter(var lstRuta:ArrayList<Ruta>,var c:Context)
    : RecyclerView.Adapter<SeleccionRutaAdapter.ViewHolder>()
{
    private var sharedPreferences: SharedPreferences? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val lblRuta  : TextView = view.findViewById(R.id.lblRuta)
        val llRuta : LinearLayout = view.findViewById(R.id.llRuta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ruta, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val items = lstRuta[position]
        val SHARED:String= c.getString(R.string.spref)
        sharedPreferences = c.getSharedPreferences(SHARED, 0)
        val tf = Typeface.createFromAsset(holder.lblRuta.context.getAssets(),"fonts/Nunito-Bold.ttf")
        holder.lblRuta.text = generarCodigoRuta(items.turno,items.tipo_ruta,items.camion)+ " - " + items.nombreRuta
        holder.lblRuta.typeface = tf
        holder.llRuta.setOnClickListener {
            if(items.turno=="1") {
                if(items.estatus=="0" /*subiendo en casa*/) {
                    val intent = Intent(c, AsistenciaManActivity::class.java)
                    ///intent.putExtra("idRuta",items.idRutaH)
                    //intent.putExtra("nomRuta",items.nombreRuta)
                    val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                    editor.putString("idRuta", items.idRutaH)
                    editor.putString("nomRuta", items.nombreRuta)
                    editor.apply()
                    c.startActivity(intent)
                }
                if(items.estatus=="1" /*bajando en la escuela*/) {
                    val intent = Intent(c, AsistenciaManDropActivity::class.java)
                    ///intent.putExtra("idRuta",items.idRutaH)
                    //intent.putExtra("nomRuta",items.nombreRuta)
                    val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                    editor.putString("idRuta", items.idRutaH)
                    editor.putString("nomRuta", items.nombreRuta)
                    editor.apply()
                    c.startActivity(intent)

                }




            }

            if(items.turno=="2") {
                val db = TransporteDB.getInstance(c.applicationContext)
                var rutasAbiertas=0
                CoroutineScope(Dispatchers.IO).launch {
                    rutasAbiertas = db.iRutaDAO.getTotalRutasAbiertasMan()
                    CoroutineScope(Dispatchers.Main).launch {
                        //if(rutasAbiertas>0){
                        //    Toast.makeText(c.applicationContext,"No puedes trabajar rutas de la tarde si no has cerrado las de la mañana",Toast.LENGTH_LONG).show()
                        //}else{
                            if(items.estatus=="0" /*subiendo en la escuela*/) {
                                val intent = Intent(c, AsistenciaTarActivity::class.java)
                                ///intent.putExtra("idRuta",items.idRutaH)
                                //intent.putExtra("nomRuta",items.nombreRuta)
                                val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                                //editor.putString("idRuta", items.idRutaH)
                                editor.putString("idRuta", items.idRutaH)
                                editor.putString("nomRuta", items.nombreRuta)
                                editor.apply()
                                c.startActivity(intent)
                            }

                        if(items.estatus=="1" /*subiendo en la escuela*/) {
                            val intent = Intent(c, AsistenciaTarDropActivity::class.java)
                            ///intent.putExtra("idRuta",items.idRutaH)
                            //intent.putExtra("nomRuta",items.nombreRuta)
                            val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
                            //editor.putString("idRuta", items.idRutaH)
                            editor.putString("idRuta", items.idRutaH)
                            editor.putString("nomRuta", items.nombreRuta)
                            editor.apply()
                            c.startActivity(intent)
                        }
                        //}
                    }
                }

            }


        }
    }

    override fun getItemCount(): Int {
        return lstRuta.size
    }

    private fun generarCodigoRuta(turno:String, tipo_ruta:String, camion:String):String{
        var trn = ""
        var truta = ""
        var cmn = ""
        if (turno.equals("1")) {
            trn = "M"
        }
        if (turno.equals("2")) {
            trn = "T"
        }
        if (tipo_ruta.equals("1")) {
            truta = "G"
        }
        if (tipo_ruta.equals("2")) {
            truta = "K"
        }
        if (tipo_ruta.equals("3")) {
            truta = "T"
        }
        if (tipo_ruta.equals("4")) {
            truta = "R"
        }

        cmn = camion
        return trn + truta + cmn

    }

}