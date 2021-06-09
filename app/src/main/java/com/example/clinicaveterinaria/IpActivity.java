package com.example.clinicaveterinaria;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class IpActivity extends AppCompatActivity {

    Button btnAceptar;
    TextView txtIp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip);

        btnAceptar=findViewById(R.id.btnAceptar);
        txtIp=findViewById(R.id.txtIp);

        btnAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.IP=txtIp.getText().toString().trim();
                //viaja a la activity con la lista de citas
                Intent intent = new Intent(IpActivity.this, MainActivity.class);
                startActivity(intent);
                Toast.makeText(IpActivity.this,"Cambios realizados",Toast.LENGTH_SHORT).show();
            }
        });
    }
}