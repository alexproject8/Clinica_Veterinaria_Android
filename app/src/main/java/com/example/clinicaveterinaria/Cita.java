package com.example.clinicaveterinaria;

import java.io.Serializable;
import java.util.Date;

public class Cita implements Serializable {
    static final long serialVersionUID=89L;

    private int idcita;
    private String motivo;
    private Date fecha;
    private int idcliente;
    private int idmascota;

    public Cita() {
    }

    public Cita(int idcita, String motivo, Date fecha, int idcliente, int idmascota) {
        this.idcita = idcita;
        this.motivo = motivo;
        this.fecha = fecha;
        this.idcliente = idcliente;
        this.idmascota = idmascota;
    }

    public Cita(String motivo, Date fecha, int idCliente, int idMascota) {

        this.motivo = motivo;
        this.fecha = fecha;
        this.idcliente = idCliente;
        this.idmascota = idMascota;
    }

    public int getIdcita() {
        return idcita;
    }
    public String getMotivo() {
        return motivo;
    }

    public Date getFecha() {
        return fecha;
    }

    public int getIdcliente() {
        return idcliente;
    }

    public int getIdmascota() {
        return idmascota;
    }
}
