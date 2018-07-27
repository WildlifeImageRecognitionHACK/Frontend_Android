package wildlifeimagerecognition.wlir

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class WlirSession {
    internal lateinit var imageLabel: String
    internal lateinit var imageId: String
    internal lateinit var imageLink: String
    lateinit var imageBits: Bitmap

    companion object {
        const val domain = "wildlifeimagevm.westus2.cloudapp.azure.com:8080"
        const val jsonImageLabel = "imageLabel"
        const val jsonImageId = "imageId"
        const val jsonImageLink = "imageLink"
    }

    fun retrievePhotoInfo() = async(UI){
        val imageJson: JsonObject? = restRequestJson("images").await()

        imageJson?.let {
            imageLabel = imageJson[jsonImageLabel].toString()
            imageId = imageJson[jsonImageId].toString()
            imageLink = imageJson[jsonImageLink].toString()
        }

        imageBits = retrievePhoto().await()
    }

    private fun retrievePhoto(): Deferred<Bitmap> = async(CommonPool) {
        val connection = URL(imageLink).openConnection()

        try {
            lateinit var bitmap: Bitmap
            val job = async(CommonPool) {
                connection.connect()
                val input = connection.inputStream
                bitmap = BitmapFactory.decodeStream(input)
            }
            job.await()
            return@async bitmap
        } catch (exception: Exception){
            throw exception
        }
    }

    private fun restRequestJson(uri: String): Deferred<JsonObject?> = async(CommonPool) {
        val connection: HttpURLConnection =
            URL(listOf("http:", "", domain, uri).reduce { acc, s -> "$acc/$s" }).openConnection()
                as HttpURLConnection
        connection.requestMethod = "GET"

        try {
            lateinit var infoJson: JsonObject
            val job = async(CommonPool) {
                connection.connect()
                val status = connection.responseCode
                val stream: InputStream = connection.inputStream
                infoJson = Parser().parse(stream) as JsonObject
            }
            job.await()
            return@async infoJson
        } catch (exception: Exception){
            throw exception
        }
    }

    fun waiting() {
        // TODO: show loading animation
    }
}
