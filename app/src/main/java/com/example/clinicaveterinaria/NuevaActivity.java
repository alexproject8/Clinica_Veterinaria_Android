package com.example.clinicaveterinaria;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class NuevaActivity extends AppCompatActivity {
    //variables para introducir texto y el botón para guardar los cambios
    TextView txtHora;
    EditText motivo;
    SeekBar seekBarHora;
    CalendarView calendarView;
    Button concertarCita;
    Socket sc;
    Date d;
    long l;
    int hayCita=1;
    Cita cita;
    Spinner spinner;
    static boolean vieneDeDetalle=false;
    ArrayList<Cita> lista=new ArrayList<>();
    ArrayList<Cita> listaHora=new ArrayList<>();
    ArrayList<String> listaMascota=new ArrayList<>();

    @Override
    protected void onStart() {
        super.onStart();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva);

        //se asocian las variables con los elementos xml
        motivo=findViewById(R.id.motivo);
        calendarView=findViewById(R.id.calendarView);
        seekBarHora=findViewById(R.id.seekBarHora);
        concertarCita =findViewById(R.id.concertarCita);
        txtHora=findViewById(R.id.txtHora);
        spinner=findViewById(R.id.spinner);

        txtHora.setText(getResources().getText(R.string.hora)+" 09:00");

        //se cargan los nombres de las mascotas en el spinner
        cargaSpinner();

        //si extras no es nulo recoge el json y lo pasa a cita
        Bundle extras=getIntent().getExtras();
        if(extras!=null){
            Gson gson=new Gson();
            cita=gson.fromJson(extras.getString("objeto"),Cita.class);
            //recupera el nombre de la mascota y lo añade al campo
            recuperaNombreMascota();

            motivo.setText(cita.getMotivo());
            concertarCita.setText(getResources().getString(R.string.modificar_cita));
        }else{
            concertarCita.setText(getResources().getString(R.string.pedir_cita));
        }

        seekBarHora.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress<1) {
                    progress=0;
                    seekBar.setProgress(0);
                }
                //txtHora.setText(String.format("%02d",progress));
                //asocia la hora al valor del seekbar
                asociaHoraSegunSeekBar();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //guarda el valor seleccionado del calendario
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);

                view.setDate(calendar.getTimeInMillis());

                Log.d("calendar", String.valueOf(view.getDate()));
                l=view.getDate();
            }
        });

        //Método para que al pulsar el botón grabar se guarden los datos
        concertarCita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date hoy=new Date();
                l=calendarView.getDate();
                d=new Date(l);

                //si el campo está vacío y la fecha es posterior a hoy ejecutará el método compruebaHora
                if(motivo.getText().toString().equals("")){

                    Toast.makeText(NuevaActivity.this,getResources().getString(R.string.campo_blanco),Toast.LENGTH_SHORT).show();

                }else if(hoy.after(d)) {
                    Toast.makeText(NuevaActivity.this,getResources().getString(R.string.fecha_mayor),Toast.LENGTH_SHORT).show();

                }else{
                    compruebaHora(l);
                }
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
            //continua en está activity y pone el boolean vieneDeDetalle a falso
            case R.id.concertarcita:
                NuevaActivity.vieneDeDetalle=false;
                return true;
            //viaja a la pantalla de lista de citas
            case R.id.listadocitas:
                intento=new Intent(NuevaActivity.this,ListaCitaActivity.class);
                startActivity(intento);
                return true;
            //va a la pantalla de inicio y elimina la clave y el ID del fichero
            case R.id.cerrasesion:
                SharedPreferences misPreferencias= PreferenceManager.getDefaultSharedPreferences(NuevaActivity.this);

                misPreferencias.edit().remove("ID").commit();
                misPreferencias.edit().remove("CLAVE").commit();

                intento=new Intent(NuevaActivity.this,MainActivity.class);
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

    //-------- Método para mostrar mensaje de confirmación y salir ---------------
    public void salir(){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(NuevaActivity.this);
        alertDialog.setMessage(getResources().getString(R.string.pregunta_salir));
        alertDialog.setTitle(getResources().getString(R.string.salir_titulo));
        alertDialog.setIcon(R.drawable.ic_baseline_warning_amber_24);
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton(getResources().getString(R.string.afirmacion), new DialogInterface.OnClickListener()
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
                Toast.makeText(NuevaActivity.this,getResources().getString(R.string.salir_cancelada),Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.show();

    }

    //-------------- Método para recuperar nombre de la mascota a partir del ID -----------------
    public void recuperaNombreMascota(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //pongo a dormir el hilo durante medio segundo para que no haya conflicto al ejecutarse otro hilo
                    Thread.sleep(500);
                    sc=new Socket(MainActivity.IP,MainActivity.PUERTO);
                    DataOutputStream out=new DataOutputStream(sc.getOutputStream());
                    DataInputStream in=new DataInputStream(sc.getInputStream());
                    //le envía el id de la mascota al servidor
                    out.writeUTF("!"+cita.getIdmascota());
                    String nombre=in.readUTF();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                           for (int i=0;i<spinner.getCount();i++){
                                spinner.setSelection(i);
                                if(spinner.getSelectedItem().toString().equals(nombre)){
                                    break;
                                }
                            }
                        }
                    });

                    out.close();
                    in.close();

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //---------- Método para guardar la cita en BBDD ------------
    public void guardarCita(){
        //paso la fecha a long, lo guardo en una variable Date, añado la hora con el método horaYminuto() y lo vuelvo a convertir en long
        l = calendarView.getDate();
        d = new Date(l);
        d = horaYminuto(seekBarHora.getProgress(), d);
        l = d.getTime();

        //recojo los datos de inicio de sesión
        String datos = datosInicioSesion();

        //recojo todos los datos para construir un objeto citaDate
        CitaDate c = new CitaDate(motivo.getText().toString(), l, Integer.parseInt(datos.substring(0, datos.indexOf(";"))), 0);

            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {

                        sc=new Socket(MainActivity.IP,MainActivity.PUERTO);

                        DataOutputStream out = new DataOutputStream(sc.getOutputStream());
                        DataInputStream in = new DataInputStream(sc.getInputStream());
                        //convierto a json
                        Gson gson = new Gson();
                        String json = gson.toJson(c);

                        //si viene de Detalle envía el id e la cita y si no envía un 0 al servidor
                        if (vieneDeDetalle) {
                            out.writeUTF("." + json + "+" + spinner.getSelectedItem().toString() + "?" + cita.getIdcita());
                        } else {
                            out.writeUTF("." + json + "+" + spinner.getSelectedItem().toString() + "?" + 0);
                            hayCita=in.readInt();
                        }

                        out.close();
                        in.close();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                    //si hayCita es 0 y no viene de detalle va la pantalla de lista citas y muestra un mensaje
                                        if (hayCita == 0 && !vieneDeDetalle) {
                                            Toast.makeText(NuevaActivity.this, getResources().getString(R.string.cita_concertada) + " " + new SimpleDateFormat("dd-MM-yyyy").format(d) +
                                                    " a las " + new SimpleDateFormat("hh:mm a").format(d), Toast.LENGTH_SHORT).show();
                                            Intent intento = new Intent(NuevaActivity.this, ListaCitaActivity.class);
                                            startActivity(intento);
                                            //si hayCita es distinto de 0 y viene de detalle va la pantalla de lista citas y muestra otro mensaje
                                        } else if(hayCita > 0 && vieneDeDetalle){
                                            Toast.makeText(NuevaActivity.this, getResources().getString(R.string.cita_modificada) + " " + new SimpleDateFormat("dd-MM-yyyy").format(d) +
                                                    " a las " + new SimpleDateFormat("hh:mm a").format(d), Toast.LENGTH_SHORT).show();
                                            Intent intento = new Intent(NuevaActivity.this, ListaCitaActivity.class);
                                            startActivity(intento);
                                        }else {
                                            //si no es ninguna de las anteriores muestra un mensaje
                                            motivo.setText("");
                                            seekBarHora.setProgress(0);
                                            Toast.makeText(NuevaActivity.this, getResources().getString(R.string.tienes_cita), Toast.LENGTH_SHORT).show();
                                        }
                            }
                        });

                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }).start();
        }

    public void compruebaHora(long l){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sc=new Socket(MainActivity.IP,MainActivity.PUERTO);

                    DataOutputStream out=new DataOutputStream(sc.getOutputStream());

                    out.writeUTF("listacitashorasmovil");

                    DataInputStream in=new DataInputStream(sc.getInputStream());

                    int num=in.readInt();

                    Gson gson=new Gson();
                    String mensaje;

                    //guardo el mensaje que está en formato json y lo convierto a objeto citaDate
                    //Después convierto CitaDate a Cita y lo añado a la lista
                    for (int i=0;i<num;i++) {
                        mensaje = in.readUTF();
                        CitaDate c = gson.fromJson(mensaje, CitaDate.class);

                        Cita cita = new Cita(c.getIdcita(), c.getMotivo(), new Date(c.getFecha()), c.getIdcliente(), c.getIdmascota());
                        lista.add(cita);
                    }

                    out.close();
                    in.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                //en un hilo de interfaz aparte
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //paso la fecha a long, lo guardo en una variable Date, añado la hora con el método horaYminuto()
                        Date hoy=new Date();
                        long l=calendarView.getDate();
                        d=new Date(l);
                        d = horaYminuto(seekBarHora.getProgress(), d);

                        boolean coincide=false;
                        //comprueba si la fecha elegida es igual a alguna hora de la base de datos
                        for (int i=0;i<lista.size();i++){
                            if(lista.get(i).getFecha().toString().equals(d.toString())){

                                coincide=true;
                                break;

                            }else{

                                coincide=false;

                            }
                        }
                        if(!coincide){
                            //si no coincide se guarda la cita
                            guardarCita();

                        }else{
                            Toast.makeText(NuevaActivity.this, getResources().getString(R.string.fecha_no_disponible), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        }).start();

    }

    //-------------- Método para cargar el spinner con los nombres de la mascota del cliente actual ---------------------
    public void cargaSpinner(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sc=new Socket(MainActivity.IP,MainActivity.PUERTO);

                    DataOutputStream out=new DataOutputStream(sc.getOutputStream());
                    DataInputStream in=new DataInputStream(sc.getInputStream());

                    SharedPreferences misPreferencias= PreferenceManager.getDefaultSharedPreferences(NuevaActivity.this);

                    int idcliente=Integer.parseInt(misPreferencias.getString("ID","x"));

                    out.writeUTF("}"+idcliente);

                    int num=in.readInt();

                    for (int i=0;i<num;i++){
                        listaMascota.add(in.readUTF());
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayAdapter<String> listAdapter=new ArrayAdapter(NuevaActivity.this,R.layout.support_simple_spinner_dropdown_item,listaMascota);

                            spinner.setAdapter(listAdapter);

                            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    Toast.makeText(NuevaActivity.this, spinner.getSelectedItem().toString()+" seleccionado", Toast.LENGTH_SHORT).show();
                                }
                                @Override
                                public void onNothingSelected(AdapterView<?> arg0) {

                                }
                            });
                        }
                    });

                    out.close();
                    in.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    //------------- Método para asociar el valor seleccionado en el seekbar con la hora --------------
    public void asociaHoraSegunSeekBar(){
        switch (seekBarHora.getProgress()){
            case 0:
                txtHora.setText(getResources().getText(R.string.hora)+" 09:00");
                break;
            case 1:
                txtHora.setText(getResources().getText(R.string.hora)+" 09:30");
                break;
            case 2:
                txtHora.setText(getResources().getText(R.string.hora)+" 10:00");
                break;
            case 3:
                txtHora.setText(getResources().getText(R.string.hora)+" 10:30");
                break;
            case 4:
                txtHora.setText(getResources().getText(R.string.hora)+" 11:00");
                break;
            case 5:
                txtHora.setText(getResources().getText(R.string.hora)+" 11:30");
                break;
            case 6:
                txtHora.setText(getResources().getText(R.string.hora)+" 12:00");
                break;
            case 7:
                txtHora.setText(getResources().getText(R.string.hora)+" 12:30");
                break;
            case 8:
                txtHora.setText(getResources().getText(R.string.hora)+" 13:00");
                break;
            case 9:
                txtHora.setText(getResources().getText(R.string.hora)+" 13:30");
                break;
            case 10:
                txtHora.setText(getResources().getText(R.string.hora)+" 14:00");
                break;
            case 11:
                txtHora.setText(getResources().getText(R.string.hora)+" 16:00");
                break;
            case 12:
                txtHora.setText(getResources().getText(R.string.hora)+" 16:30");
                break;
            case 13:
                txtHora.setText(getResources().getText(R.string.hora)+" 17:00");
                break;
            case 14:
                txtHora.setText(getResources().getText(R.string.hora)+" 17:30");
                break;
            case 15:
                txtHora.setText(getResources().getText(R.string.hora)+" 18:00");
                break;
        }
    }

    //--------- Método para modificar hora según elección del seekbar ------------
    public static Date horaYminuto(int num,Date d){
        switch (num){
            case 0:
                d.setHours(9);
                d.setMinutes(0);
                d.setSeconds(0);
                break;
            case 1:
                d.setHours(9);
                d.setMinutes(30);
                d.setSeconds(0);
                break;
            case 2:
                d.setHours(10);
                d.setMinutes(0);
                d.setSeconds(0);
                break;
            case 3:
                d.setHours(10);
                d.setMinutes(30);
                d.setSeconds(0);
                break;
            case 4:
                d.setHours(11);
                d.setMinutes(0);
                d.setSeconds(0);
                break;
            case 5:
                d.setHours(11);
                d.setMinutes(30);
                d.setSeconds(0);
                break;
            case 6:
                d.setHours(12);
                d.setMinutes(0);
                d.setSeconds(0);
                break;
            case 7:
                d.setHours(12);
                d.setMinutes(30);
                d.setSeconds(0);
                break;
            case 8:
                d.setHours(13);
                d.setMinutes(0);
                d.setSeconds(0);
                break;
            case 9:
                d.setHours(13);
                d.setMinutes(30);
                d.setSeconds(0);
                break;
            case 10:
                d.setHours(14);
                d.setMinutes(0);
                d.setSeconds(0);
                break;
            case 11:
                d.setHours(16);
                d.setMinutes(0);
                d.setSeconds(0);
                break;
            case 12:
                d.setHours(16);
                d.setMinutes(30);
                d.setSeconds(0);
                break;
            case 13:
                d.setHours(17);
                d.setMinutes(0);
                d.setSeconds(0);
                break;
            case 14:
                d.setHours(17);
                d.setMinutes(30);
                d.setSeconds(0);
                break;
            case 15:
                d.setHours(18);
                d.setMinutes(0);
                d.setSeconds(0);
                break;
        }

        return d;

    }

    //--------- Metodo para devolver datos de inicio de sesión almacenados ----------
    public String datosInicioSesion(){

        SharedPreferences misPreferencias= PreferenceManager.getDefaultSharedPreferences(NuevaActivity.this);

        return misPreferencias.getString("ID","x")+";"+misPreferencias.getString("CLAVE","x");
    }
}