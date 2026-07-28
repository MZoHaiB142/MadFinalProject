package com.example.madfinalproject.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface SopApiService {

    // FastAPI endpoint
    @POST("/generate-sop")
    Call<SopApiResponse> generateSop(@Body SopApiRequest request);
}