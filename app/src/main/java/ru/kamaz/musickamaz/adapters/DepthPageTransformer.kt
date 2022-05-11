package ru.kamaz.musickamaz.adapters

import android.view.View
import androidx.annotation.RequiresApi
import androidx.viewpager2.widget.ViewPager2

private const val MIN_SCALE = 0.75f

@RequiresApi(21)
class DepthPageTransformer : ViewPager2.PageTransformer {

    override fun transformPage(view: View, position: Float) {
        view.apply {
            val pageWidth = width
            when {
                position < -1 -> { // [-Infinity,-1)

                    alpha = 0f
                }
                position <= 0 -> {
                    alpha = 1f
                    translationX = pageWidth * -position
                    translationZ = -1f
                    scaleX = 1f
                    scaleY = 1f
                }
                position <= 1 -> { // (0,1]

                    alpha = 1f
                    translationX = 0f
                    translationZ = 0f
                    scaleX = 1f
                    scaleY = 1f
                }
                else -> { // (1,+Infinity]
                    alpha = 0f
                }
            }
        }
    }
}