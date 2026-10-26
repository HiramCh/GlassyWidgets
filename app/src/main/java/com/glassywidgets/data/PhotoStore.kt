package com.glassywidgets.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max

/** Guarda en la app las fotos elegidas para los widgets (reducidas a ~1000 px). */
object PhotoStore {
    private const val MAX_SIDE = 1000

    suspend fun import(context: Context, uri: Uri, id: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val src: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { dec, info, _ ->
                    val s = info.size
                    val f = max(s.width, s.height).toFloat() / MAX_SIDE
                    if (f > 1f) dec.setTargetSize((s.width / f).toInt(), (s.height / f).toInt())
                    dec.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            } else {
                context.contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it) }
                    ?: error("imagen inválida")
            }
            val dir = File(context.filesDir, "photos").apply { mkdirs() }
            val file = File(dir, "$id-${System.currentTimeMillis()}.jpg")
            file.outputStream().use { src.compress(Bitmap.CompressFormat.JPEG, 90, it) }
            file.absolutePath
        }.getOrNull()
    }

    private val cache = object : android.util.LruCache<String, Bitmap>(8) {}

    fun load(path: String): Bitmap? {
        if (path.isBlank()) return null
        cache.get(path)?.let { return it }
        val bmp = runCatching { BitmapFactory.decodeFile(path) }.getOrNull() ?: return null
        cache.put(path, bmp)
        return bmp
    }
}
