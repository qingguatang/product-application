package com.qingguatang.application;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

public interface HelloService {
  @GET("/")
  Call<ResponseBody> hello();
}
