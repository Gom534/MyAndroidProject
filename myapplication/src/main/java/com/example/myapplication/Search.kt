package com.example.myapplication

import android.content.Intent
import android.content.res.AssetManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import java.io.InputStream

class Search : AppCompatActivity() {
    private lateinit var restStopNames: List<String>
    private lateinit var editTextSearch: EditText
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        editTextSearch = findViewById(R.id.editTextSearch)
        listView = findViewById(R.id.listView)
        fileRead()
        // Adapter 설정
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, restStopNames)
        listView.adapter = adapter

        // 엔터 키 입력을 감지하여 검색을 완료하는 방법
        editTextSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                // 검색 작업 완료 후 결과를 설정하고 종료
                val searchResultIntent = Intent(this, MainActivity::class.java)
                searchResultIntent.putExtra("SEARCH_TEXT", editTextSearch.text.toString())
                startActivity(searchResultIntent)
                true
            } else {
                false
            }
        }

        // EditText 검색 기능 추가
        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // 여기에 startActivity를 호출하지 않음
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
            }
        })
    }

    private fun fileRead() {
        val assetManager: AssetManager = applicationContext.assets
        val inputStream = assetManager.open("rest_stops1.csv")
        restStopNames = readRestStopNamesFromCsv(inputStream)
        Log.d("RestStopNames", restStopNames.toString())
    }

    private fun readRestStopNamesFromCsv(inputStream: InputStream): List<String> {
        val nameList = mutableListOf<String>()

        inputStream.bufferedReader(Charsets.UTF_8).useLines { lines ->
            val iterator = lines.iterator()
            if (iterator.hasNext()) iterator.next() // 헤더 행을 건너뜁니다.
            iterator.forEachRemaining { line ->
                val columns = line.split(",")
                val restStopName = columns[0] // 첫 번째 열의 값을 읽어옵니다.
                nameList.add(restStopName)
            }
        }
        return nameList
    }
}