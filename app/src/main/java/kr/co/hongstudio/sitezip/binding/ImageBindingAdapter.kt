package kr.co.hongstudio.sitezip.binding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import kr.co.hongstudio.sitezip.R
import kr.co.hongstudio.sitezip.glide.GlideApp

object ImageBindingAdapter {

    @JvmStatic
    @BindingAdapter("loadImage")
    fun loadImage(imageView: ImageView, resId: Int) {
        imageView.setImageResource(resId)
    }

    @JvmStatic
    @BindingAdapter("loadIcon")
    fun loadIcon(imageView: ImageView, url: String?) {
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
            .thumbnail(Glide.with(imageView.context).load(R.raw.gif_loading))
            .fitCenter()
            .centerInside()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .into(imageView)
    }

    @JvmStatic
    @BindingAdapter("loadImage")
    fun loadImage(imageView: ImageView, url: String?) {
        if (url.isNullOrEmpty()) {
            return
        }
        GlideApp.with(imageView.context)
            .load(url)
            .thumbnail(Glide.with(imageView.context).load(R.raw.gif_loading))
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(imageView)
    }

}