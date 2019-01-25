package com.faitechno.www.daskomqrcodegenerator;

import com.google.gson.annotations.SerializedName;

public class PraktikanModel {

    @SerializedName("nama")
    private String nama;
    @SerializedName("nim")
    private Integer nim;
    @SerializedName("kelas")
    private String kelas;

    public PraktikanModel(String nama, Integer nim, String kelas) {
        this.nama = nama;
        this.nim = nim;
        this.kelas = kelas;
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
}