
package com.example.clinicaveterinaria;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DetalleActivity extends AppCompatActivity {
    //declaración de variables
    Cita cita;
    TextView lblMotivo,lblFecha,lblMascota;
    ImageView imgEdit,imgEliminar;
    Socket sc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle);

        //inicialización de variables
        lblMotivo=findViewById(R.id.lblMotivo);
        lblFecha=findViewById(R.id.lblFecha);
        lblMascota=findViewById(R.id.lblMascota);
        imgEdit=findViewById(R.id.imgEdit);
        imgEliminar=findViewById(R.id.imgEliminar);

        //recoger datos de la cita seleccionada en ListaCitaActivity
        conseguirDatos();

        //se guardan los datos de la cita en los labels
        lblMotivo.setText(cita.getMotivo());
        lblFecha.setText(new SimpleDateFormat("dd-MM-yyyy").format(cita.getFecha())+
                " a las "+new SimpleDateFormat("hh:mm a").format(cita.getFecha()));

        //al pulsar en editar se envía el json del objeto Cita y viaja a la NuevaActivity
        imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NuevaActivity.vieneDeDetalle=true;

                Intent intento=new Intent(DetalleActivity.this,NuevaActivity.class);
                Gson gson=new Gson();
                intento.putExtra("objeto",gson.toJson(cita));
                startActivity(intento);
            }
        });
    //al pulsar eliminar se ejecuta el método confirmarAnularCita() para anular una cita
        imgEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                confirmarAnularCita();

            }
        });
    }

    //************ MÉTODOS SOBREESCRITO MENÚ **************

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intento;
        switch(item.getItemId()){
            //Viaja a NuevaActivity
            case R.id.concertarcita:
                intento=new Intent(DetalleActivity.this,NuevaActivity.class);
                startActivity(intento);
                return true;
                //Viaja a ListaCitaActivity
            case R.id.listadocitas:
                intento=new Intent(DetalleActivity.this,ListaCitaActivity.class);
                startActivity(intento);
                return true;

            //va a la pantalla de inicio y elimina la clave y el ID del fichero
            case R.id.cerrasesion:
                SharedPreferences misPreferencias= PreferenceManager.getDefaultSharedPreferences(DetalleActivity.this);

                misPreferencias.edit().remove("ID").commit();
                misPreferencias.edit().remove("CLAVE").commit();

                intento=new Intent(DetalleActivity.this,MainActivity.class);
                startActivity(intento);
                return true;
            //ejecuta el método para salir de la app
            case R.id.salir:
                salir();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //********************************************************************
    //************************* MÉTODOS **********************************
    //********************************************************************


//------ Método para salir de la aplicación ---------------
    public void salir(){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(DetalleActivity.this);
        alertDialog.setMessage(getResources().getString(R.string.pregunta_salir));
        alertDialog.setTitle(getResources().getString(R.string.salir_titulo));
        alertDialog.setIcon(R.drawable.ic_baseline_warning_amber_24);
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("Sí", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                finishAffinity();
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                Toast.makeText(DetalleActivity.this,getResources().getString(R.string.salir_cancelada),Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.show();

    }

//---------Método para confirmar anular cita -----------
    public void confirmarAnularCita(){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(DetalleActivity.this);
        alertDialog.setMessage(getResources().getString(R.string.pregunta_anular_cita));
        alertDialog.setTitle(getResources().getString(R.string.anular_cita));
        alertDialog.setIcon(R.drawable.ic_baseline_warning_amber_24);
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton(getResources().getString(R.string.afirmacion), new DialogInterface.OnClickListener()
        {
            //al pulsar en si se ejecuta el método de eliminar cita
            public void onClick(DialogInterface dialog, int which)
            {
                eliminarCita();
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                Toast.makeText(DetalleActivity.this,getResources().getString(R.string.opcion_anular_cita_cancelada),Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.show();

    }

    //-----------Método para eliminar cita -------------
    public void eliminarCita(){
        //si el id de la cita es mayor a 0 se conecta al servidor, la elimina y va a la pantalla de las lista de citas
        if(cita.getIdcita()>0){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        sc=new Socket(MainActivity.IP,MainActivity.PUERTO);
                        DataOutputStream out=new DataOutputStream(sc.getOutputStream());

                        out.writeUTF("¡"+cita.getIdcita());

                        out.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        //viaja a listaCitaActivity
        Intent intento=new Intent(DetalleActivity.this,ListaCitaActivity.class);
        startActivity(intento);
        Toast.makeText(DetalleActivity.this,getResources().getString(R.string.cita_anulada),Toast.LENGTH_SHORT).show();
    }

    //---------- Método para mostrar los datos en el detalle -------------
    public void conseguirDatos(){

        //Recoge el json y lo convierte al objeto cita
        Bundle extras=getIntent().getExtras();
        if(extras!=null){
            Gson gson=new Gson();
            cita=gson.fromJson(extras.getString("objeto"),Cita.class);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //conecta al servidor y recupera el nombre de la mascota para añadirlo al label
                    sc=new Socket(MainActivity.IP,MainActivity.PUERTO);
                    DataOutputStream out=new DataOutputStream(sc.getOutputStream());
                    DataInputStream in=new DataInputStream(sc.getInputStream());

                    out.writeUTF("!"+cita.getIdmascota());
                    String nombre=in.readUTF();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            lblMascota.setText(nombre);
                        }
                    });

                    out.close();
                    in.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        //si la fecha ha pasado se pone imvisible para que no pueda modificar ni anular una cita que ya ha pasado
        if(cita.getFecha().before(new Date())){
            imgEdit.setVisibility(View.INVISIBLE);
            imgEliminar.setVisibility(View.INVISIBLE);
        }else{
            imgEdit.setVisibility(View.VISIBLE);
            imgEliminar.setVisibility(View.VISIBLE);
        }
    }
}