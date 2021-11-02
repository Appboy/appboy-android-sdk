package com.appboy.sample.imageloading

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import com.appboy.models.cards.Card
import com.braze.enums.BrazeViewBounds
import com.braze.images.IBrazeImageLoader
import com.braze.models.inappmessage.IInAppMessage
import com.braze.support.BrazeLogger.Priority.E
import com.braze.support.BrazeLogger.brazelog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class GlideAppboyImageLoader : IBrazeImageLoader {
    private var requestOptions = RequestOptions()
    override fun renderUrlIntoCardView(
        context: Context,
        card: Card?,
        imageUrl: String,
        imageView: ImageView,
        viewBounds: BrazeViewBounds?
    ) {
        renderUrlIntoView(context, imageUrl, imageView)
    }

    override fun renderUrlIntoInAppMessageView(
        context: Context,
        inAppMessage: IInAppMessage,
        imageUrl: String,
        imageView: ImageView,
        viewBounds: BrazeViewBounds?
    ) {
        renderUrlIntoView(context, imageUrl, imageView)
    }

    override fun getPushBitmapFromUrl(
        context: Context,
        extras: Bundle?,
        imageUrl: String,
        viewBounds: BrazeViewBounds?
    ): Bitmap? = getBitmapFromUrl(context, imageUrl)

    override fun getInAppMessageBitmapFromUrl(
        context: Context,
        inAppMessage: IInAppMessage,
        imageUrl: String,
        viewBounds: BrazeViewBounds?
    ): Bitmap? = getBitmapFromUrl(context, imageUrl)

    private fun renderUrlIntoView(
        context: Context,
        imageUrl: String,
        imageView: ImageView
    ) {
        Glide.with(context)
            .load(imageUrl)
            .apply(requestOptions)
            .into(imageView)
    }

    private fun getBitmapFromUrl(context: Context, imageUrl: String): Bitmap? {
        try {
            return Glide.with(context)
                .asBitmap()
                .apply(requestOptions)
                .load(imageUrl).submit().get()
        } catch (e: Exception) {
            brazelog(E, e) { "Failed to retrieve bitmap at url: $imageUrl" }
        }
        return null
    }

    override fun setOffline(isOffline: Boolean) {
        // If the loader is offline, then we should only be retrieving from the cache
        requestOptions = requestOptions.onlyRetrieveFromCache(isOffline)
    }
}
