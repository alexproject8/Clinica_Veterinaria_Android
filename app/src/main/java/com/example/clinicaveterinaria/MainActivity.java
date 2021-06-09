package com.example.clinicaveterinaria;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

            public class MainActivity extends AppCompatActivity {

                //------------------- VARIABLES ----------------------
                ImageView acceder,inicarSesion,modificar,logo,imgSalir;
                TextView txtId,txtClave,consultarId,txtDni,lblId,lblClave,lblNuevaClave,txtNuevaClave;
                Socket sc;
                boolean iniciar=true;
                Button btnCredenciales;

                public static String IP = "127.0.0.1";
                public static final int PUERTO = 9999;

                @Override
                protected void onCreate(Bundle savedInstanceState) {
                    super.onCreate(savedInstanceState);

                    //comprueba si hay o no datos de inicio de sesión para entrar directamente o no
                    if(!datosInicioSesion().equals("x;x")){
                        Intent intent = new Intent(MainActivity.this, ListaCitaActivity.class);
                        startActivity(intent);

                    }else{

                        setContentView(R.layout.activity_main);

                        //------------- Asociando variables a sus elementos gráficos --------------
                        acceder=findViewById(R.id.acceder);
                        inicarSesion=findViewById(R.id.iniciarSesion);
                        modificar=findViewById(R.id.modificar);
                        txtId=findViewById(R.id.txtId);
                        txtClave=findViewById(R.id.txtclave);
                        consultarId=findViewById(R.id.consultarId);
                        txtDni=findViewById(R.id.txtDni);
                        lblId=findViewById(R.id.lblId);
                        lblClave=findViewById(R.id.lblClave);
                        logo=findViewById(R.id.logo);
                        lblNuevaClave=findViewById(R.id.lblNuevaClave);
                        txtNuevaClave=findViewById(R.id.txtNuevaClave);
                        imgSalir=findViewById(R.id.imgSalir);
                        btnCredenciales=findViewById(R.id.btnCredenciales);

                        btnCredenciales.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //viaja a la activity con la lista de citas
                                Intent intent = new Intent(MainActivity.this, IpActivity.class);
                                startActivity(intent);
                            }
                        });

                        //---------- Detectar pulsación en el texto Consulta tu ID --------
                        consultarId.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                txtDni.setVisibility(View.VISIBLE);
                                consultarId.setText(getResources().getText(R.string.escribe_id));
                            }
                        });

                        //------- Detectar Enter en EditText ------------
                        txtDni.setOnKeyListener(new View.OnKeyListener() {
                            public boolean onKey(View v, int keyCode, KeyEvent event) {
                                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {

                                    if(!txtDni.getText().toString().equals("")) {

                                        consultaId();

                                    }else{
                                        Toast.makeText(MainActivity.this, getResources().getText(R.string.campo_vacio), Toast.LENGTH_SHORT).show();
                                        consultarId.setText(getResources().getText(R.string.consulta_id));
                                        txtDni.setVisibility(View.INVISIBLE);
                                        txtDni.setText("");
                                    }
                                    return true;
                                }
                                return false;
                            }
                        });

                        imgSalir.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                salir();
                            }
                        });

                        //---------- Detectar pulsación en la imagen (botón) del appbar para inicio de sesión --------
                        inicarSesion.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                iniciar=true;
                                acceder.setVisibility(View.VISIBLE);
                                txtClave.setVisibility(View.VISIBLE);
                                txtId.setVisibility(View.VISIBLE);
                                lblId.setVisibility(View.VISIBLE);
                                lblClave.setVisibility(View.VISIBLE);
                                logo.setVisibility(View.INVISIBLE);
                                lblNuevaClave.setVisibility(View.GONE);
                                txtNuevaClave.setVisibility(View.GONE);

                                //cambio la imagen
                                acceder.setImageResource(R.drawable.ic_baseline_open_in_browser_24);

                            }
                        });

                        //---------- Detectar pulsación en la imagen (botón) del appbar para modificar contraseña --------
                        modificar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                iniciar=false;
                                acceder.setVisibility(View.VISIBLE);
                                txtClave.setVisibility(View.VISIBLE);
                                txtId.setVisibility(View.VISIBLE);
                                lblId.setVisibility(View.VISIBLE);
                                lblClave.setVisibility(View.VISIBLE);
                                logo.setVisibility(View.INVISIBLE);
                                lblNuevaClave.setVisibility(View.VISIBLE);
                                txtNuevaClave.setVisibility(View.VISIBLE);
                                //cambio la imagen
                                acceder.setImageResource(R.drawable.ic_baseline_save_24);
                            }
                        });

                        //---------- Detectar pulsación en la imagen (botón) para acceder/iniciar sesión o modificar contraseña --------
                        acceder.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //si iniciar es verdadero se accede o iniciar sesion
                                if (iniciar){
                                    if(txtId.getText().toString().equals("") || txtClave.getText().toString().equals("")){
                                        Toast.makeText(MainActivity.this, getResources().getText(R.string.campo_blanco), Toast.LENGTH_LONG).show();
                                    }else {

                                        //va a la pantalla con la lista de cita
                                        muestraListaCita();

                                    }
                                    //si iniciar es falso se modifica la contraseña
                                }else{
                                    if(txtId.getText().toString().equals("") || txtClave.getText().toString().equals("")  || txtNuevaClave.getText().toString().equals("") ){
                                        Toast.makeText(MainActivity.this, getResources().getText(R.string.campo_blanco), Toast.LENGTH_LONG).show();
                                    }else {

                                        //modifica la clave del cliente
                                        modificarClave();

                                    }
                                }
                            }
                        });
                    }

                }
                //evita que vaya hacia atrás para no volver a acceder a otra pantalla después de cerrrar sesión
                @Override
                public void onBackPressed() {
                    salir();
                    //Toast.makeText(MainActivity.this, getResources().getText(R.string.no_atras), Toast.LENGTH_SHORT).show();
                }

    //********************************************************************
    //************************* MÉTODOS **********************************
    //********************************************************************

    //-------------- Método para guardar datos de inicio sesión del usuario ---------------
    public void guardarDatosInicioSesion(String id, String clave){
        SharedPreferences misPreferencias= PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        SharedPreferences.Editor miEditor = misPreferencias.edit();

        miEditor.putString("ID", id);
        miEditor.putString("CLAVE", clave);

        miEditor.commit();
    }

    //--------- Metodo para devolver datos de inicio de sesión almacenados ----------
    public String datosInicioSesion(){

        SharedPreferences misPreferencias= PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        return misPreferencias.getString("ID","x")+";"+misPreferencias.getString("CLAVE","x");
    }

    //------------- Método para consultar ID del cliente----------
    public void consultaId(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sc = new Socket(IP, PUERTO);

                    DataOutputStream out = new DataOutputStream(sc.getOutputStream());
                    DataInputStream in = new DataInputStream(sc.getInputStream());

                    //envía el dbi al servidor y recoge el id
                    out.writeUTF("^" + txtDni.getText().toString());
                    int id = in.readInt();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //si el id es distinto de -1
                            if (id != -1) {
                                Toast.makeText(MainActivity.this, getResources().getText(R.string.tu_id_es) +" "+ id, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, getResources().getText(R.string.tu_id_es_incorrecto), Toast.LENGTH_SHORT).show();
                            }
                            consultarId.setText(getResources().getText(R.string.consulta_id));
                            txtDni.setVisibility(View.INVISIBLE);
                            txtDni.setText("");
                        }
                    });


                    out.close();
                    in.close();

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

                //----------------------- Método para iniciar sesión y mostrar lista de citas -------------------
    public void muestraListaCita(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    sc = new Socket(IP, PUERTO);

                    DataOutputStream out = new DataOutputStream(sc.getOutputStream());
                    DataInputStream in = new DataInputStream(sc.getInputStream());

                    out.writeUTF(">" + txtId.getText().toString() + "-" + txtClave.getText().toString());
                    boolean existe = in.readBoolean();

                    if (existe) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                //Una vez inicia sesión se guardan los datos en el fichero preferencias
                                guardarDatosInicioSesion(txtId.getText().toString(),txtClave.getText().toString());

                                txtId.setText(null);
                                txtClave.setText(null);
                                //viaja a la activity con la lista de citas
                                Intent intent = new Intent(MainActivity.this, ListaCitaActivity.class);
                                startActivity(intent);
                            }
                        });

                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //se ponen los campos a nulos
                                txtId.setText(null);
                                txtClave.setText(null);
                                Toast.makeText(MainActivity.this, getResources().getText(R.string.tu_id_clave_es_incorrecto), Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    out.close();
                    in.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }

    //------------------ Método para modificar la contraseña ---------------------
    public void modificarClave(){
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    sc = new Socket(IP, PUERTO);

                    DataOutputStream out = new DataOutputStream(sc.getOutputStream());
                    DataInputStream in = new DataInputStream(sc.getInputStream());
                    //envía el id, la clave, y la nueva clave al servidor
                    out.writeUTF("<" + txtId.getText().toString() + "-" + txtClave.getText().toString() + "+" + txtNuevaClave.getText().toString());
                    boolean existe = in.readBoolean();

                    if (existe) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //se ponen los campos a nulos
                                txtId.setText(null);
                                txtClave.setText(null);
                                txtNuevaClave.setText(null);
                                Toast.makeText(MainActivity.this, getResources().getText(R.string.clave_modificada), Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //se ponen los campos a nulos
                                txtId.setText(null);
                                txtClave.setText(null);
                                txtNuevaClave.setText(null);
                                Toast.makeText(MainActivity.this, getResources().getText(R.string.tu_id_clave_es_incorrecto), Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    out.close();
                    in.close();

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //---------- Método para salir de la aplicación ------------
    public void salir(){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setMessage(getResources().getText(R.string.pregunta_salir));
        alertDialog.setTitle(getResources().getText(R.string.salir));
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
                Toast.makeText(MainActivity.this,getResources().getText(R.string.salir_cancelada),Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.show();

    }
}