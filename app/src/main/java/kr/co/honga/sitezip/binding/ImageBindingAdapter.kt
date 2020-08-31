package kr.co.honga.sitezip.binding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import kr.co.honga.sitezip.glide.GlideApp

object ImageBindingAdapter {

    @JvmStatic
    @BindingAdapter("loadImage")
    fun loadImage(imageView: ImageView, resId: Int) {
        imageView.setImageResource(resId)
    }

    @JvmStatic
    @BindingAdapter("loadImage")
    fun loadImage(imageView: ImageView, url: String?) {
        if (url.isNullOrEmpty()) {
            return
        }
        GlideApp.with(imageView.context)
            .load(
                if (url.contains("icons")) {
                    FirebaseStorage.getInstance().getReference(url)
                } else {
                    url
                }
            )
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .into(imageView)
    }

}