package com.example.myapplication

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.util.TypedValue
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import java.util.concurrent.TimeUnit

data class CartItem(val 가격: Int, val 남은수량: Int)

class Packaging : AppCompatActivity() {
    private lateinit var btnInsert2: Button
    private lateinit var cartbutton: Button
    private lateinit var database: DatabaseReference
    private lateinit var buttonContainer: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences
    private val PREFSNAME = "counter_prefs"
    private val COUNTERKEY = "counter"
    private val LASTRESETKEY = "last_reset"
    private lateinit var restname: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.packaging)

        sharedPreferences = getSharedPreferences(PREFSNAME, Context.MODE_PRIVATE)
        checkAndResetCounter()

        cartbutton = findViewById(R.id.button3)
        buttonContainer = findViewById(R.id.buttonContainer)

        val myButton: ImageButton = findViewById(R.id.imageButton2)
        myButton.setOnClickListener {
            val intent = Intent(this@Packaging, MainActivity::class.java)
            startActivity(intent)
        }

        restname = intent.getStringExtra("restname")!!

        database = FirebaseDatabase.getInstance().reference
        getCategoriesAndAllData()

        btnInsert2 = findViewById(R.id.button3)
        btnInsert2.setOnClickListener {
            val intent1 = Intent(this@Packaging, Cart::class.java)
            intent1.putExtra("count", getCounter())
            intent1.putExtra("restname", restname)
            startActivity(intent1)
            incrementCounter()
        }
    }

    private fun getCounter(): Int {
        return sharedPreferences.getInt(COUNTERKEY, 1)
    }

    private fun incrementCounter() {
        val currentCounter = getCounter()
        sharedPreferences.edit().putInt(COUNTERKEY, currentCounter + 1).apply()
    }

    private fun checkAndResetCounter() {
        val lastResetTime = sharedPreferences.getLong(LASTRESETKEY, 0L)
        val currentTime = System.currentTimeMillis()

        if (TimeUnit.MILLISECONDS.toDays(currentTime - lastResetTime) >= 1) {
            sharedPreferences.edit().putInt(COUNTERKEY, 0).apply()
            sharedPreferences.edit().putLong(LASTRESETKEY, currentTime).apply()
            database = FirebaseDatabase.getInstance().reference
            val deletecategory = database.child("장바구니")
            deletecategory.removeValue()
            Log.d("MainActivity", "Counter has been reset.")
        }
    }

    private fun getCategoriesAndAllData() {
        val buttonStates = mutableMapOf<String, Int>()
        var money = 0
        val backgroudrawble: Drawable? = ContextCompat.getDrawable(this, R.drawable.rounded_button)
        val categoriesRef = database.child(restname)

        categoriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val childrenCount = snapshot.child("BHC").children.count()
                for ((index, categorySnapshot) in snapshot.child("BHC").children.withIndex()) {
                    val categoryName = categorySnapshot.key
                    if (categoryName != null) {
                        val stringBuilder = StringBuilder()
                        stringBuilder.append(categoryName + "\n")
                        val categoryData = categorySnapshot.value
                        val cartItemData = categorySnapshot.value as? Map<String, Any>
                        val imageUrl = cartItemData?.get("이미지")?.toString()
                        if (categoryData is Map<*, *>) {
                            stringBuilder.append(formatCategoryData(categoryData))
                        }

                        val button = Button(this@Packaging).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )

                            setOnClickListener {
                                val cartItemName = categoryName
                                val price = cartItemData?.get("가격")?.toString()?.toIntOrNull() ?: 0
                                val quantity = cartItemData?.get("현재수량")?.toString()?.toIntOrNull() ?: 1

                                if (!buttonStates.containsKey(cartItemName)) {
                                    buttonStates[cartItemName] = 0
                                }

                                if (buttonStates[cartItemName] == 0) {
                                    addItemToCart(cartItemName, price, quantity)
                                    buttonStates[cartItemName] = 1
                                    money += price
                                } else {
                                    deleteItem(cartItemName)
                                    buttonStates[cartItemName] = 0
                                    money -= price
                                }
                                cartbutton.text = "$money\t 장바구니"
                            }

                            val excludedKeys = listOf("이미지", "현재수량")
                            val mainKeys = cartItemData?.keys?.filterNot { it in excludedKeys }
                            val textBuilder = StringBuilder()
                            textBuilder.append(categoryName + "\n") // 상위 키 값을 추가
                            mainKeys?.forEach { key ->
                                val value = cartItemData?.get(key)
                                textBuilder.append("$key: $value\n")
                            }
                            text = textBuilder.toString()
                            setTextSize(TypedValue.COMPLEX_UNIT_DIP,20.0f)
                            setBackgroundColor(Color.parseColor("#FFFFFF"))
                            gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
                            setPadding(0, 0, 50, 0)
                            background = backgroudrawble
                            Glide.with(this)
                                .load(imageUrl)
                                .override(300, 300)
                                .into(object : SimpleTarget<Drawable>() {
                                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                                        setCompoundDrawablesWithIntrinsicBounds(resource, null, null, null)
                                        compoundDrawablePadding = 16
                                    }
                                })
                        }
                        buttonContainer.addView(button)
                        if (index < childrenCount - 1) {
                            val dottedLine = View(this@Packaging).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    resources.getDimensionPixelSize(R.dimen.dotted_line_height)
                                )
                                setBackgroundColor(Color.parseColor("#D3D3D3"))
                            }
                            buttonContainer.addView(dottedLine)
                            println("Added dotted line after button $index")
                        }
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
            formattedString.append("$key: $value\n")
        }
        return formattedString.toString()
    }

    private fun addItemToCart(name: String, price: Int, quality: Int) {
        val currentCounter = getCounter()
        val cartRef = database.child("장바구니").child(currentCounter.toString())
        val cartItem = CartItem(가격 = price, 남은수량 = quality)
        cartRef.child(name).setValue(cartItem)
    }

    private fun deleteItem(name: String) {
        val cartRef = database.child("장바구니")
        cartRef.child("$name").removeValue()
    }
}
