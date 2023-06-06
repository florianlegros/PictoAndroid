package com.example.pictopicto.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View


class SpectrogramView(context: Context, data: Array<DoubleArray>) :
    View(context) {

    private val paint: Paint = Paint()
    private var bmp: Bitmap? = null

    init {
        if (data != null) {
            paint.strokeWidth = 1F

            val width = data.size
            val height = data[0].size
            val arrayCol = IntArray( width* height)
            var counter = 0
            for (i in 0 until height) {
                for (j in 0 until width) {
                    var color: Int
                    val value: Int = 255 - (data[j][i] * 255).toInt()
                    color = value shl 16 or (value shl 8) or value or (255 shl 24)
                    arrayCol[counter] = color
                    counter++
                }
            }
            bmp = Bitmap.createBitmap(arrayCol, width, height, Bitmap.Config.ARGB_8888)
        } else {
            System.err.println("Data Corrupt")
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        bmp?.let { canvas.drawBitmap(it, 0F, 0F, paint) }
    }
}