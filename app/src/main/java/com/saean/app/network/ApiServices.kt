package com.saean.app.network

import retrofit2.Call
import retrofit2.http.*

interface ApiServices {
    companion object {
        const val URL = "https://apiblanje.bigtek.id/"
        const val clientID = "saean"
        const val securityKey = "!@bgtk-smd2020"
    }

    @FormUrlEncoded
    @POST("auth/login")
    fun login(
        @Field("security_code") code : String,
        @Field("email") email: String,
        @Field("password") password: String): Call<String>

    @FormUrlEncoded
    @POST("auth/register")
    fun register(
        @Field("security_code") code : String,
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("phone") phone: String,
        @Field("address") address: String,
        @Field("password") password: String): Call<String>

    @FormUrlEncoded
    @POST("user/change")
    fun edit(
        @Field("security_code") code : String,
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("phone") phone: String,
        @Field("address") address: String): Call<String>

    @FormUrlEncoded
    @POST("produk/list")
    fun productList(
        @Field("security_code") code : String,
        @Field("sort") sort: String,
        @Field("id_toko") id_toko: Int): Call<String>

    @FormUrlEncoded
    @POST("produk/list")
    fun productSearch(
        @Field("security_code") code : String,
        @Field("sort") sort: String,
        @Field("cari") cari: String,
        @Field("id_toko") id_toko: Int): Call<String>

    @FormUrlEncoded
    @POST("produk/list")
    fun productSearchFilter(
        @Field("security_code") code : String,
        @Field("kategori") kategori: Int,
        @Field("subkat") subkat: Int,
        @Field("cari") cari: String,
        @Field("id_toko") id_toko: Int): Call<String>

    @FormUrlEncoded
    @POST("produk/detail")
    fun productDetail(
        @Field("security_code") code : String,
        @Field("id_produk") id_produk: Int,
        @Field("id_toko") id_toko: Int): Call<String>

    @FormUrlEncoded
    @POST("konfigurasi/banner")
    fun getBanner(
        @Field("security_code") code : String): Call<String>

    @FormUrlEncoded
    @POST("konfigurasi/bannerToko")
    fun getMarketBanner(
        @Field("security_code") code : String,
        @Field("id_toko") id_toko: Int): Call<String>

    @FormUrlEncoded
    @POST("konfigurasi/kontak")
    fun getKontak(
        @Field("security_code") code : String): Call<String>

    @FormUrlEncoded
    @POST("ongkir/area")
    fun getArea(
        @Field("security_code") code : String): Call<String>

    @FormUrlEncoded
    @POST("ongkir/area_desa")
    fun getAreaDesa(
        @Field("security_code") code : String,
        @Field("id_area") id_area : Int): Call<String>

    @FormUrlEncoded
    @POST("pembelian/add_transaksi")
    fun createTransaction(
        @Field("security_code") code : String,
        @Field("email") email : String,
        @Field("id_toko") id_toko: Int): Call<String>

    @FormUrlEncoded
    @POST("pembelian/add_item")
    fun sendItems(
        @Field("security_code") code : String,
        @Field("id_transaksi") id_transaksi : Int,
        @Field("produk_id") produk_id : Int,
        @Field("qty") qty : Int,
        @Field("id_toko") id_toko: Int): Call<String>

    @FormUrlEncoded
    @POST("pembelian/pengiriman")
    fun sendDataPembeli(
        @Field("security_code") code : String,
        @Field("id_transaksi") id_transaksi : Int,
        @Field("nama") nama : String,
        @Field("alamat") alamat : String,
        @Field("hp") hp : String): Call<String>

    @FormUrlEncoded
    @POST("pembelian/ongkir")
    fun sendOngkir(
        @Field("security_code") code : String,
        @Field("id_transaksi") id_transaksi : Int,
        @Field("ongkir") ongkir : Double,
        @Field("id_area") id_area : Int,
        @Field("longitude") longitude : String,
        @Field("latitude") latitude : String): Call<String>

    @FormUrlEncoded
    @POST("pembelian/history_list")
    fun getHistory(
        @Field("security_code") code : String,
        @Field("email") email : String): Call<String>

    @FormUrlEncoded
    @POST("pembelian/notifikasi_list")
    fun getNotifikasi(
        @Field("security_code") code : String,
        @Field("email") email : String): Call<String>

    @FormUrlEncoded
    @POST("pembelian/detail_transaksi")
    fun getDetailTransaction(
        @Field("security_code") code : String,
        @Field("id_transaksi") id_transaksi : Int): Call<String>

    @FormUrlEncoded
    @POST("produk/getKategori")
    fun getKategori(
        @Field("security_code") code : String,
        @Field("id_toko") id_toko: Int): Call<String>

    @FormUrlEncoded
    @POST("produk/getSubKategori")
    fun getSubKategori(
        @Field("security_code") code : String,
        @Field("id_kategori") id_kategori : Int,
        @Field("id_toko") id_toko: Int): Call<String>

    @FormUrlEncoded
    @POST("auth/updateToken")
    fun updateToken(
        @Field("security_code") code : String,
        @Field("email") email : String,
        @Field("token") token : String): Call<String>

    @FormUrlEncoded
    @POST("konfigurasi/caraBayar")
    fun getHelp(
        @Field("security_code") code : String): Call<String>

    @FormUrlEncoded
    @POST("konfigurasi/info")
    fun getInfo(
        @Field("security_code") code : String): Call<String>

    @GET("weather")
    fun cuaca(
        @Query("lat") lat : Double,
        @Query("lon") lon : Double,
        @Query("units") units: String,
        @Query("appid") appid: String,
        @Query("lang") lang:String
    ): Call<String>
}