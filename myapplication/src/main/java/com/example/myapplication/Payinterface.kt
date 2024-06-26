package com.example.myapplication

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Field
import retrofit2.http.Header


interface PayInterface {
    @POST("online/v1/payment/ready")
    fun preparePayment(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String ,
        @Body request: PaymentReadyRequest
    ): Call<PaymentReadyResponse>

    @POST("online/v1/payment/approve")
    fun approvePayment(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String ,
        @Body request: PaymentApproveRequest
    ): Call<PaymentApproveResponse>
}

data class PaymentReadyRequest(
    @SerializedName("cid") val cid: String,
    @SerializedName("partner_order_id") val partnerOrderId: String,
    @SerializedName("partner_user_id") val partnerUserId: String,
    @SerializedName("item_name") val itemName: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("total_amount") val totalAmount: Int,
    @SerializedName("vat_amount") val vatAmount: Int,
    @SerializedName("tax_free_amount") val taxFreeAmount: Int,
    @SerializedName("approval_url") val approvalUrl: String,
    @SerializedName("fail_url") val failUrl: String,
    @SerializedName("cancel_url") val cancelUrl: String
)

data class PaymentApproveRequest(
    @SerializedName("cid") val cid: String,
    @SerializedName("tid") val tid: String,
    @SerializedName("partner_order_id") val partnerOrderId: String,
    @SerializedName("partner_user_id") val partnerUserId: String,
    @SerializedName("pg_token") val pgToken: String
)