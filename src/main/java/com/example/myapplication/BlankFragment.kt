package com.example.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

class BlankFragment : Fragment() {
    //사진 뛰우는 용도의 프레그먼트
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 레이아웃을 인플레이트
        val view = inflater.inflate(R.layout.fragment_blank, container, false)
        // 전달된 인자를 기반으로 UI 업데이트
        val imageView = view?.findViewById<ImageView>(R.id.imageView)
        val page = arguments?.getInt("page") ?: 0
        // 이미지 설정 (여기서는 예제로 ic_launcher_foreground를 사용)
        val imageResId = when (page) {
            0 -> R.drawable.image1 // 첫 번째 페이지 이미지
            1 -> R.drawable.image2 // 두 번째 페이지 이미지
            2 -> R.drawable.image3 // 세 번째 페이지 이미지
            else -> R.drawable.ic_launcher_foreground // 기본 이미지
        }
        imageView?.setImageResource(imageResId)
        return view
    }

    companion object {
        fun newInstance(page: Int): BlankFragment {
            val fragment = BlankFragment()
            val args = Bundle()
            args.putInt("page", page)
            fragment.arguments = args
            return fragment
        }
    }
}
