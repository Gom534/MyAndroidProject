package com.example.myapplication
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

// 데이터 클래스 정의
data class GasStation(
    @SerializedName("serviceAreaName") val name: String,
    @SerializedName("gasolinePrice") val gasolinePrice: String,
    @SerializedName("diselPrice") val dieselPrice: String,
    @SerializedName("lpgPrice") val lpgPrice: String
)

data class ApiResponse(
    @SerializedName("list") val gasStations: List<GasStation>
)

// API 요청을 보내고 응답을 처리하는 함수
fun fetchGasStationPrices(apiKey: String) {
    val client = OkHttpClient()
    val url = "https://data.ex.co.kr/openapi/business/curStateStation?key=$apiKey&type=json&serviceAreaName=김해금관가야"

    val request = Request.Builder()
        .url(url)
        .build()

    client.newCall(request).enqueue(object : okhttp3.Callback {
        override fun onFailure(call: okhttp3.Call, e: IOException) {
            e.printStackTrace()
        }

        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
            response.body?.let { responseBody ->
                val json = responseBody.string()
                val gson = Gson()
                val apiResponse = gson.fromJson(json, ApiResponse::class.java)

                for (station in apiResponse.gasStations) {
                    println("Station: ${station.name}")
                    println("Gasoline Price: ${station.gasolinePrice}")
                    println("Diesel Price: ${station.dieselPrice}")
                    println("LPG Price: ${station.lpgPrice}")
                }
            }
        }
    })
}

fun oilPrice() {
    val apiKey = "0315656628" // 여기에 API 키를 입력하세요
    fetchGasStationPrices(apiKey)
}
