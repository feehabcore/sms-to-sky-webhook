package com.payment.smsforwarder.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

interface WebhookApi {
    @POST
    suspend fun postPayment(
        @Url url: String,
        @Header("X-Api-Token") apiToken: String,
        @Header("X-Signature") signature: String,
        @Header("X-Device-Id") deviceId: String,
        @Header("X-Timestamp") timestamp: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body payload: WebhookPaymentPayload
    ): Response<ResponseBody>
}
