package com.matatkoj.nbaplayers.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.matatkoj.nbaplayers.Constants
import com.matatkoj.nbaplayers.R

@Composable
fun loadTeamPicture(teamAbbreviation: String): State<Bitmap> {
    val url = remember {
        Constants.TEAM_IMAGE_URL.format(
            // hacky way to fix different abbreviation
            teamAbbreviation.takeUnless { it == "PHX" } ?: "PHO"
        )
    }
    return loadPicture(url = url, defaultImage = R.mipmap.ic_launcher_foreground)
}

@Composable
fun loadPicture(url: String, @DrawableRes defaultImage: Int): MutableState<Bitmap> {

    val bitmap = BitmapFactory.decodeResource(
        LocalContext.current.resources,
        defaultImage
    )

    val bitmapState: MutableState<Bitmap> = remember { mutableStateOf(bitmap) }

    // get network image
    Glide.with(LocalContext.current)
        .asBitmap()
        .load(url)
        .into(object : CustomTarget<Bitmap>() {
            override fun onLoadCleared(placeholder: Drawable?) { }
            override fun onResourceReady(
                resource: Bitmap,
                transition: Transition<in Bitmap>?
            ) {
                bitmapState.value = resource
            }
        })

    return bitmapState
}