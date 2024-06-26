package com.example.myapplication

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleObserver
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
class Cart : AppCompatActivity(), LifecycleObserver {
    private lateinit var mybutton: ImageButton
    private lateinit var preparePaymentButton: Button
    private lateinit var database: DatabaseReference
    private lateinit var buttonContainer : LinearLayout
    private var getcount1 = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cart)
        preparePaymentButton = findViewById(R.id.button)
        preparePaymentButton.setOnClickListener {
            preparePayment()
        }
        val getcount = intent.getIntExtra("count", 1)
        getcount1 = getcount
        // UI 요소 초기화
         buttonContainer = findViewById(R.id.buttonContainercart)
        mybutton = findViewById(R.id.imageButton2)
        database = FirebaseDatabase.getInstance().reference
        mybutton.setOnClickListener {
            // 버튼 클릭 페이지 이동
            val intent = Intent(this@Cart, Packaging::class.java);
            startActivity(intent);
        }
        getCategoriesAndAllData()
    }

    private fun getCategoriesAndAllData() {
        // 장바구니 카테고리 데이터 참조
        Log.d("getcount",getcount1.toString())
        val categoriesRef = database.child("장바구니").child(getcount1.toString())
        val backgroudrawble : Drawable? = ContextCompat.getDrawable(this, R.drawable.rounded_button)
        categoriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 마테고리 안에있는 데이터 하나씩 가져오기
                for (categorySnapshot in snapshot.children) {
                    //데이터 가져와서 텍스트로 저장 후 초기화
                    val stringBuilder = StringBuilder()
                    //키값 받아오기
                    val categoryName = categorySnapshot.key
                    if (categoryName != null) {
                        stringBuilder.append("$categoryName\n")
                        //벨류값 가져오기
                        val categoryData = categorySnapshot.value
                        if (categoryData is Map<*, *>) {
                            // 데이터 처리
                            stringBuilder.append(formatCategoryData(categoryData)).append("")
                        }
                        val button = Button(this@Cart).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            setOnClickListener {

                            }
                            text = stringBuilder.toString()
                            setBackgroundColor(Color.parseColor("#FFFFFF"))
                            gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
                            setPadding(0, 0, 50, 0)
                            background = backgroudrawble

                        }
                        buttonContainer.addView(button)
                    }

                }

            }
            override fun onCancelled(error: DatabaseError) {
                println("Error getting categories: ${error.message}")
            }
        })
    }
    private fun formatCategoryData(data: Map<*, *>): String {
        val formattedString = StringBuilder()
        for ((key, value) in data) {
            formattedString.append("$key: ")
            if (value is Map<*, *>) {
                formattedString.append(formatCategoryData(value)).append("\n")
            } else {
                formattedString.append(value).append("\n")
            }
        }
        return formattedString.toString()
    }
    /*
        카카오페이API 사용시도
    =================================================================
    =================================================================
     */
    private fun preparePayment() {
        val authorization = "SECRET_KEY DEV01058BFFDB0F2DE565B5B7F539F7DC9B57EEB"
        val type = "application/json"
        val request = PaymentReadyRequest(
            cid = "TC0ONETIME",
            partnerOrderId = "1001",
            partnerUserId = "goguma",
            itemName = "chocopai",
            quantity = 1,
            totalAmount = 2200,
            vatAmount = 200,
            taxFreeAmount = 0,
            approvalUrl ="https://localhost:8080",
            failUrl = "https://localhost:8080",
            cancelUrl = "https://localhost:8080"
        )

        val apiService = RetrofitClient.instance.create(PayInterface::class.java)
        val prepareCall = apiService.preparePayment(authorization,type ,request)

        prepareCall.enqueue(object : Callback<PaymentReadyResponse> {
            override fun onResponse(call: Call<PaymentReadyResponse>, response: Response<PaymentReadyResponse>) {
                if (response.isSuccessful) {
                    val paymentReadyResponse = response.body()
                    Log.d("kakaopay", "Response body: ${response.body().toString()}")
                    paymentReadyResponse?.let {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.nextRedirectMobileUrl))
                        startActivity(intent)
                        val intent1 = Intent(this@Cart,Resultpage::class.java)
                        intent1.putExtra("count", getcount1)
                        startActivity(intent1)
                    }
                } else {
                    Log.e("PaymentError", "결제 준비 실패: ${response.code()}, ${response.errorBody()?.string()}")
                    Toast.makeText(this@Cart, "결제 준비 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PaymentReadyResponse>, t: Throwable) {
                Log.e("PaymentError", "결제 준비 중 에러 발생", t)
                Toast.makeText(this@Cart, "결제 준비 중 에러 발생", Toast.LENGTH_SHORT).show()
            }
        })
    }



    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.data?.let { uri ->
            val pgToken = uri.getQueryParameter("pg_token")
            val tid = uri.getQueryParameter("tid").toString()
            if (pgToken != null) {
                approvePayment(tid ,pgToken)
            }
        }
    }


    private fun approvePayment(tid: String, pgToken: String) {
        val authorization = "SECRET_KEY DEV01058BFFDB0F2DE565B5B7F539F7DC9B57EEB"
        val type = "application/json"
        val request = PaymentApproveRequest(
            cid = "TC0ONETIME",
            tid = tid,
            partnerOrderId = "1001",
            partnerUserId = "goguma",
            pgToken = pgToken
        )

        val apiService = RetrofitClient.instance.create(PayInterface::class.java)
        val approveCall = apiService.approvePayment(authorization,type, request)

        approveCall.enqueue(object : Callback<PaymentApproveResponse> {
            override fun onResponse(call: Call<PaymentApproveResponse>, response: Response<PaymentApproveResponse>) {
                if (response.isSuccessful) {
                    // 결제 승인 성공 처리
                    Toast.makeText(this@Cart, "결제 승인 성공", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("approveError", "결제 승인 실패: ${response.code()}, ${response.errorBody()?.string()}")
                    Toast.makeText(this@Cart, "결제 승인 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PaymentApproveResponse>, t: Throwable) {
                Log.e("approveError", "결제 승인 중 에러 발생", t)
                Toast.makeText(this@Cart, "결제 승인 중 에러 발생", Toast.LENGTH_SHORT).show()
            }
        })
    }
}