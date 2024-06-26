package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class Store : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store)

        // 버튼을 추가할 LinearLayout을 가져옵니다.
        val backgroudrawble : Drawable? = ContextCompat.getDrawable(this, R.drawable.rounded_button)

        val buttonContainer = findViewById<LinearLayout>(R.id.buttonContainer)
        val restname = intent.getStringExtra("restname")

        // 새로운 버튼을 생성합니다.
        //파이어베이스에서 가게이름 받아오는 메서도
        val button = Button(this@Store).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                // 받아온 데이터 정리 및 설정
                val intent = Intent(context, Packaging::class.java)
                intent.putExtra("restname", restname)
                startActivity(intent)
            }
            // 버튼 꾸미기
            setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
            text = "명량 핫도그 \n\n 운영시간 \n08시 ~ 16시"
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25f)
            val paddingInDp = 50
            val scale = resources.displayMetrics.density
            val paddingInPx = (paddingInDp * scale + 0.5f).toInt()
            setPadding(0, 0, paddingInPx, 0)
            background = backgroudrawble
        }

        // 이미지의 크기를 조정하는 함수 호출
        val drawable = resizeDrawable(this, R.drawable.mungrang, 100, 100)
        button.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)

        // 버튼을 LinearLayout에 추가합니다.
        buttonContainer.addView(button)
    }
    // 꾸미기
    private fun resizeDrawable(context: Context, resId: Int, width: Int, height: Int): Drawable? {
        val drawable = ContextCompat.getDrawable(context, resId) ?: return null
        val bitmap = (drawable as BitmapDrawable).bitmap
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
        return BitmapDrawable(context.resources, resizedBitmap)
    }
}