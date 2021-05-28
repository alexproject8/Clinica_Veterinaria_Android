package com.example.clinicaveterinaria;

public class CitaDate {

    private int idcita;
    private String motivo;
    private long fecha;
    private int idcliente;
    private int idmascota;

    public CitaDate() {
    }

    public CitaDate(String motivo, long fecha, int idCliente, int idMascota) {

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

    public long getFecha() {
        return fecha;
    }

    public int getIdcliente() {
        return idcliente;
    }

    public int getIdmascota() {
        return idmascota;
    }
}
