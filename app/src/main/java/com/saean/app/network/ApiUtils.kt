package com.saean.app.network

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class ApiUtils {
    fun getServiceDefault(): ApiServices {
        val retrofit = Retrofit.Builder()
            .baseUrl(ApiServices.URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
        return retrofit.create(ApiServices::class.java)
    }
}