package com.example.myapplication
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import android.Manifest
import android.content.pm.PackageManager
import java.util.*

class Fragement : AppCompatActivity() {

    private lateinit var storageReference: StorageReference
    private var filePath: Uri? = null
    private val pickImageRequestCode = 22
    private val permissionRequestCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragement)

        // Firebase Storage 초기화
        storageReference = FirebaseStorage.getInstance().reference

        // 버튼 초기화
        val buttonSelectImage: Button = findViewById(R.id.buttonSelectImage)
        val buttonUploadImage: Button = findViewById(R.id.buttonUploadImage)

        // 이미지 선택 버튼 클릭 리스너
        buttonSelectImage.setOnClickListener {
            selectImage()
        }

        // 이미지 업로드 버튼 클릭 리스너
        buttonUploadImage.setOnClickListener {
            uploadImage()
        }

        // 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    permissionRequestCode
                )
            }
        }
    }

    private fun selectImage() {
        // 인텐트를 사용하여 이미지 선택
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image"), pickImageRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickImageRequestCode && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            // 선택한 이미지의 URI를 저장
            filePath = data.data
        }
    }

    private fun uploadImage() {
        if (filePath != null) {
            // 고유한 파일 이름 생성
            val ref = storageReference.child("images/" + UUID.randomUUID().toString())
            // 파일 업로드
            ref.putFile(filePath!!)
                .addOnSuccessListener { taskSnapshot ->
                    // 업로드 성공 시
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                        val downloadUrl = uri.toString()
                        Toast.makeText(this, "Image Uploaded: $downloadUrl", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    // 업로드 실패 시
                    Toast.makeText(this, "Failed " + e.message, Toast.LENGTH_SHORT).show()
                }
        }
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionRequestCode) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // 권한 허용됨
            } else {
                // 권한 거부됨
            }
        }
    }
}