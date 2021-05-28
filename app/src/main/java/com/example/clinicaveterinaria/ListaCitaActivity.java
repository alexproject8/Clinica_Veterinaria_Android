package com.example.clinicaveterinaria;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class ListaCitaActivity extends AppCompatActivity {
    static ArrayList<Cita> listCita;

    RecyclerView recycler;
    Socket sc;
    String mensaje;
    TextView txtMotivo;
    Cita cita;
    AdapterCita adaptador;
    ArrayList<Cita> pasadas=new ArrayList();
    ArrayList<Cita> pendientes=new ArrayList();
    ArrayList<Cita> listaTerminada=new ArrayList();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_cita);

        //ejecuta el método para mostrar las citas
        mostrarListaCitas();
        //ejecuta el método para eliminar cita delizando hacia la derecha o izquierda
        deslizarEliminar();

    }

    //impide que vaya atrás para no volver a la pantalla de inicio de sesión
    @Override
    public void onBackPressed() {
        salir();
        //Toast.makeText(ListaCitaActivity.this, getResources().getText(R.string.no_atras), Toast.LENGTH_SHORT).show();
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
            //va a la pantalla concertar cita
            case R.id.concertarcita:
                NuevaActivity.vieneDeDetalle=false;
                intento=new Intent(ListaCitaActivity.this,NuevaActivity.class);
                startActivity(intento);
                return true;
                //se mantienen en la pantalla actual
            case R.id.listadocitas:
                Toast.makeText(ListaCitaActivity.this,getResources().getText(R.string.esta_en_listado_citas),Toast.LENGTH_SHORT).show();
                return true;

                //va a la pantalla de inicio y elimina la clave y el ID del fichero
            case R.id.cerrasesion:
                SharedPreferences misPreferencias= PreferenceManager.getDefaultSharedPreferences(ListaCitaActivity.this);

                misPreferencias.edit().remove("ID").commit();
                misPreferencias.edit().remove("CLAVE").commit();

                intento=new Intent(ListaCitaActivity.this,MainActivity.class);
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

    //************************************************************************************************************************************************
    //************************* MÉTODOS **************************************************************************************************************
    //************************************************************************************************************************************************

    //--------- Metodo para devolver datos de inicio de sesión almacenados ----------
    public String datosInicioSesion(){

        SharedPreferences misPreferencias= PreferenceManager.getDefaultSharedPreferences(ListaCitaActivity.this);

        return misPreferencias.getString("ID","x")+";"+misPreferencias.getString("CLAVE","x");
    }

    //------------ Método para mensaje de confirmación para salir ----------------
    public void salir(){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ListaCitaActivity.this);
        alertDialog.setMessage(getResources().getText(R.string.pregunta_salir));
        alertDialog.setTitle(getResources().getText(R.string.salir_titulo));
        alertDialog.setIcon(R.drawable.ic_baseline_warning_amber_24);
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton(getResources().getText(R.string.afirmacion), new DialogInterface.OnClickListener()
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
                Toast.makeText(ListaCitaActivity.this,getResources().getText(R.string.salir_cancelada),Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.show();

    }

    //--------- Método para mostrar la lista de citas ---------------
    public void mostrarListaCitas(){
        recycler=(RecyclerView) findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));

        listCita=new ArrayList<Cita>();
        txtMotivo=findViewById(R.id.txtMotivo);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sc=new Socket(MainActivity.IP,MainActivity.PUERTO);

                    DataOutputStream out=new DataOutputStream(sc.getOutputStream());

                    out.writeUTF("listacitasmovil");

                    out.writeInt(Integer.parseInt(datosInicioSesion().substring(0,datosInicioSesion().indexOf(";"))));

                    DataInputStream in=new DataInputStream(sc.getInputStream());

                    int num=in.readInt();

                    Gson gson=new Gson();

                    for (int i=0;i<num;i++){
                        mensaje=in.readUTF();
                        CitaDate c=gson.fromJson(mensaje,CitaDate.class);

                        Cita cita = new Cita(c.getIdcita(), c.getMotivo(), new Date(c.getFecha()), c.getIdcliente(), c.getIdmascota());
                        listCita.add(cita);
                    }

                    for (int i=0;i<listCita.size();i++){
                        if(listCita.get(i).getFecha().before(new Date())){
                            pasadas.add(listCita.get(i));
                        }else{
                            pendientes.add(listCita.get(i));
                        }
                    }

                    //Ordeno el objeto cita de mayor a menor según la fecha
                    Collections.sort(pasadas, new Comparator<Cita>() {
                        @Override
                        public int compare(Cita o1, Cita o2) {
                            return new Integer(o2.getFecha().compareTo(o1.getFecha()));
                        }
                    });

                    //Ordeno el objeto cita de mayor a menor según la fecha
                    Collections.sort(pendientes, new Comparator<Cita>() {
                        @Override
                        public int compare(Cita o1, Cita o2) {
                            return new Integer(o1.getFecha().compareTo(o2.getFecha()));
                        }
                    });

                    for (int i=0;i<pendientes.size();i++){
                        listaTerminada.add(pendientes.get(i));
                    }

                    for (int i=0;i<pasadas.size();i++){
                        listaTerminada.add(pasadas.get(i));
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adaptador=new AdapterCita(listaTerminada);
                        recycler.setAdapter(adaptador);
                    }
                });
            }
        }).start();
    }

    //---------------------- Método swipe para eliminar elementos de la lista ----------------------------
    public void deslizarEliminar(){

        ItemTouchHelper ith=new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
                //No hace nada
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
               //consigo la posicion de la lista
                int posicion=viewHolder.getAdapterPosition();
                //uso el executor para realizar la acción de eliminar deslizando
                MiExecutor.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {
                        if(adaptador.getLista().get(posicion).getFecha().before(new Date())){

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //impide que vaya atrás cuando ha iniciado sesión
                                    Intent intento=new Intent(ListaCitaActivity.this,ListaCitaActivity.class);
                                    startActivity(intento);
                                    Toast.makeText(ListaCitaActivity.this,getResources().getString(R.string.cita_pasada),Toast.LENGTH_SHORT).show();
                                }
                            });

                        }else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    preguntaEliminar(posicion);
                                }
                            });
                        }
                    }
                });
            }
        });
        ith.attachToRecyclerView(recycler);
    }

    //-----------Método para eliminar cita -------------
    public void eliminarCita(Cita cita){
        if(cita.getIdcita()>0){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        sc=new Socket(MainActivity.IP,MainActivity.PUERTO);
                        DataOutputStream out=new DataOutputStream(sc.getOutputStream());

                        out.writeUTF("¡"+cita.getIdcita());

                        out.close();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //carga la activity para que refresque la lista
                                Intent intento=new Intent(ListaCitaActivity.this,ListaCitaActivity.class);
                                startActivity(intento);
                                Toast.makeText(ListaCitaActivity.this,getResources().getString(R.string.cita_anulada),Toast.LENGTH_SHORT).show();
                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

    }

    //---------- Método para preguntar y eliminar cita ------------
    public void preguntaEliminar(int posicion){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ListaCitaActivity.this);
        alertDialog.setMessage(getResources().getString(R.string.pregunta_anular_cita));
        alertDialog.setTitle(getResources().getString(R.string.anular_cita));
        alertDialog.setIcon(R.drawable.ic_baseline_warning_amber_24);
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton(getResources().getString(R.string.afirmacion), new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                eliminarCita(adaptador.getLista().get(posicion));
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                //carga la activity para que refresque la lista
                Intent intento=new Intent(ListaCitaActivity.this,ListaCitaActivity.class);
                startActivity(intento);
                Toast.makeText(ListaCitaActivity.this,getResources().getString(R.string.opcion_anular_cita_cancelada),Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.show();

    }

}