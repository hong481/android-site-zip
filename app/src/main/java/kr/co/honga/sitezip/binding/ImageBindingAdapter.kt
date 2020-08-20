package kr.co.honga.sitezip.binding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.StorageReference
import kr.co.honga.sitezip.glide.GlideApp

object ImageBindingAdapter {

    @JvmStatic
    @BindingAdapter("loadImage")
    fun loadImage(imageView: ImageView, resId: Int) {
        imageView.setImageResource(resId)
    }

    @JvmStatic
    @BindingAdapter("firebaseLoadImage")
    fun firebaseLoadImage(imageView: ImageView, reference: StorageReference?) {
        GlideApp.with(imageView.context)
            .load(reference)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .into(imageView)
    }

}