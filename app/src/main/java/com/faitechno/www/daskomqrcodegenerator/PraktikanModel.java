package com.faitechno.www.daskomqrcodegenerator;

import com.google.gson.annotations.SerializedName;

public class PraktikanModel {

    @SerializedName("nama")
    private String nama;
    @SerializedName("nim")
    private Integer nim;
    @SerializedName("kelas")
    private String kelas;
    @SerializedName("status")
    private Integer status;

    public PraktikanModel(String nama, Integer nim, String kelas, Integer status) {
        this.nama = nama;
        this.nim = nim;
        this.kelas = kelas;
        this.status = status;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public Integer getNim() {
        return nim;
    }

    public void setNim(Integer nim) {
        this.nim = nim;
    }

    public String getKelas() {
        return kelas;
    }

    public void setKelas(String kelas) {
        this.kelas = kelas;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}