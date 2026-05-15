package com.paryavarankavalu.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

object ImageTools {
    fun compressUnder500Kb(source: File): File {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(source.absolutePath, options)
        options.inSampleSize = maxOf(1, minOf(options.outWidth / 1280, options.outHeight / 1280))
        options.inJustDecodeBounds = false
        val bitmap = BitmapFactory.decodeFile(source.absolutePath, options) ?: return source
        val output = File(source.parentFile, "compressed_${source.name}")
        var quality = 82
        do {
            FileOutputStream(output).use { bitmap.compress(Bitmap.CompressFormat.JPEG, quality, it) }
            quality -= 8
        } while (output.length() > 500_000 && quality >= 42)
        return output
    }
}
