package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException


class MainActivity : AppCompatActivity() {
    data class GasStation(
        @SerializedName("serviceAreaName") val name: String,
        @SerializedName("gasolinePrice") val gasolinePrice: String,
        @SerializedName("diselPrice") val dieselPrice: String,
        @SerializedName("lpgPrice") val lpgPrice: String
    )
    data class ApiResponse(
        @SerializedName("list") val gasStations: List<GasStation>
    )
    private lateinit var  textview : TextView
    private lateinit var tvNearestRestStop:TextView
    private lateinit var requestPermissionsUtil: RequestPermissionsUtil
    private lateinit var  editText: EditText
    private var restname  =""
    private val locationUtils by lazy { LocationUtils(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_practice)
        requestPermissionsUtil = RequestPermissionsUtil(this)
        val myButton: Button = findViewById(R.id.button)
        val mapbutton : Button = findViewById(R.id.button2)
        val cartimg : ImageButton = findViewById(R.id.imageButton4)

        tvNearestRestStop = findViewById(R.id.textView)
        editText = findViewById(R.id.editTextText)


        editText.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                editText.visibility = View.GONE
                val intent = Intent(this@MainActivity, Search::class.java)
                startActivity(intent)
            }
        }
        val searchText = intent.getStringExtra("SEARCH_TEXT")
        editText.setText(searchText)

        mapbutton.setOnClickListener{
            val intent = Intent(this@MainActivity, MapsActivity::class.java)
            startActivity(intent)
        }
        myButton.setOnClickListener {
                // 버튼 클릭 페이지 이동
                val intent = Intent(this@MainActivity, Store::class.java);
                intent.putExtra("restname", restname)
                startActivity(intent)
        }
        cartimg.setOnClickListener{
            val intent = Intent(this@MainActivity, Cart::class.java);
            startActivity(intent)
        }
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        viewPager.adapter = MyAdapter(this)
        initialize()
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    // API 요청을 보내고 응답을 처리하는 함수
    private fun fetchGasStationPrices(apiKey: String) {
        val client = OkHttpClient()
        val url = "https://data.ex.co.kr/openapi/business/curStateStation?key=$apiKey&type=json&serviceAreaName=$restname"
        Log.d("TAG", "주유소 이름 : $restname")

        val request = Request.Builder()
            .url(url)
            .build()
        // 주유 금액 받아오는 메소드
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                e.printStackTrace()
            }
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.body?.let { responseBody ->
                    val json = responseBody.string()
                    val gson = Gson()
                    val apiResponse = gson.fromJson(json, ApiResponse::class.java)
                    val stringBuilder = StringBuilder()
                    textview = findViewById(R.id.textView6)
                    for (station in apiResponse.gasStations) {
                        stringBuilder.append("가솔린 : ${station.gasolinePrice} " + "디젤 : ${station.dieselPrice} " + "LPG : ${station.lpgPrice} ")
                        textview.text = stringBuilder

                    }
                }
            }
        })
    }

    private fun oilPrice(apiKey: String) {
        fetchGasStationPrices(apiKey)
    }

    private fun nearStop(callback: () -> Unit) {
        locationUtils.getLastLocation().addOnSuccessListener { location ->
            val userLocation = LatLng(location.latitude, location.longitude)
            // 가장 가까운 휴게소 찾기
            val nearestRestStop = locationUtils.findNearestRestStop(userLocation)
            // 휴게소 이름 UI에 표시하기
            nearestRestStop?.let {
                tvNearestRestStop.text = it.name + "휴게소"
                restname = it.name
                Log.d("TAG", "주유소 이름 : $restname")

                // nearStop 작업이 완료되었을 때 콜백 호출
                callback()
            }
        }
    }

    // 이 메서드에서 nearStop과 oilPrice 호출
    private fun initialize() {
        val apiKey = "0315656628" // 여기에 API 키를 입력하세요
        //고차함수와{}치는게 고차함수
        // nearStip자체도 함수인데 그안에 함수를 집어넣어 순서를 결정 이렇게 안하면 콜백으로 인하여 니어스탑보다 oilprice가 우선실행되어
        // 내가원하는 값이 안나옴
        nearStop {
            oilPrice(apiKey)
        }
    }
}







