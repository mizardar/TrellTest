package com.mizardar.trelltest.model

import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.mizardar.trelltest.R


data class ModelVideo(
    var isBookMarked : Boolean,
    var videoPath : String
)

@BindingAdapter("app:bookmarked")
fun setImageResource(imageView: AppCompatImageView, isBookMarked: Boolean) {
    if (isBookMarked){
        imageView.setImageDrawable(ContextCompat.getDrawable(imageView.context, R.drawable.ic_bookmark_selected))
    }else {
        imageView.setImageDrawable(ContextCompat.getDrawable(imageView.context, R.drawable.ic_bookmark))
    }
}