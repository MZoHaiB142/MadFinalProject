package com.example.madfinalproject.ai;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

interface OpenAIApi {
    @POST("chat/completions") Call<OpenAIResponse> evaluate(@Body Map<String,Object> body);
}
