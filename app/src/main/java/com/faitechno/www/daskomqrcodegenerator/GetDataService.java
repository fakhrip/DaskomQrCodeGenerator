package com.faitechno.www.daskomqrcodegenerator;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface GetDataService {

    @GET("/api/all_praktikan")
    Call<List<PraktikanModel>> getAllPraktikan();
}