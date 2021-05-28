package com.example.clinicaveterinaria;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdapterCita extends RecyclerView.Adapter<AdapterCita.ViewHolderCita> {
    ArrayList<Cita> listCita;
    Context contexto;

    //constructor con la lista
    public AdapterCita(ArrayList<Cita> listCita) {
        this.listCita = listCita;
    }

    @Override
    public ViewHolderCita onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list,null,false);
        return new ViewHolderCita(view);
    }

    @Override
    public void onBindViewHolder(ViewHolderCita holder, int position) {
        holder.asignarCita(listCita.get(position));
        //si la fecha es posterior a la de hoy estará de color verde
        if(listCita.get(position).getFecha().before(new Date())){
            holder.itemView.findViewById(R.id.layout).setBackgroundColor(Color.parseColor("#F5A08E"));

            //si la fecha es anterior a la de hoy estará de color rojo
        }else{
            holder.itemView.findViewById(R.id.layout).setBackgroundColor(Color.parseColor("#B7F58E"));
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Envío los datos de las citas en formato json a la activity detalle
                Intent intento=new Intent(contexto,DetalleActivity.class);
                Gson gson=new Gson();
                intento.putExtra("objeto",gson.toJson(listCita.get(position)));
                contexto.startActivity(intento);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listCita.size();
    }

    public List<Cita> getLista() {
        return listCita;
    }

    public class ViewHolderCita extends RecyclerView.ViewHolder {
        TextView fecha;
        TextView hora;

        public ViewHolderCita(View itemView) {
            super(itemView);
            contexto=itemView.getContext();
            fecha=(TextView) itemView.findViewById(R.id.fecha);
            hora=(TextView) itemView.findViewById(R.id.hora);
        }

        public void asignarCita(Cita cita) {
            //pongo formato de fecha a la fecha y hora
            fecha.setText(new SimpleDateFormat("dd-MM-yyyy").format(cita.getFecha()));
            hora.setText(new SimpleDateFormat("hh:mm a").format(cita.getFecha()));
        }
    }
}
