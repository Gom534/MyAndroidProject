package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Resultpage : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private var getcount1 = 1
    private lateinit var textview: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultpage) // setContentView를 먼저 호출

        textview = findViewById(R.id.textView4)
        val getcount = intent.getIntExtra("count", 1)
        getcount1 = getcount
        readAllData()

        var button: ImageButton = findViewById(R.id.imageButton2)
        button.setOnClickListener {
            val intent = Intent(this@Resultpage, Packaging::class.java)
            startActivity(intent)
        }
    }
    private fun readAllData() {
        database = FirebaseDatabase.getInstance().reference
        val categoryRef = database.child("장바구니").child(getcount1.toString())
        categoryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryNames = mutableListOf<String>()
                var money = 0
                for (categorySnapshot in snapshot.children) {
                    val categoryName = categorySnapshot.key
                    if (categoryName != null) {
                        categoryNames.add(categoryName)
                        val categoryData = categorySnapshot.value
                        if (categoryData is Map<*, *>) {
                            val price = extractPrice(categoryData)
                            money += price!!.toInt()

                        }
                    }
                }

                val categoryNamesString = categoryNames.joinToString(", ")
                val resultText = "받아가실곳 : 1번 입구 \n\n주문한 음식: $categoryNamesString\n\n 가격 : $money \n\n 주문번호 : $getcount1"
                textview.text = resultText
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Database error: ${error.message}")
            }
        })
    }
    private fun extractPrice(data: Map<*, *>): String? {
        for ((key, value) in data) {
            if (key == "가격") {
                return value.toString()
            }
        }
        return null
    }
}
