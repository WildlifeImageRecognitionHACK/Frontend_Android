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
    internal lateinit var originalImageLabel: String
    internal lateinit var currentImageLabel: String
    private lateinit var imageId: String
    private lateinit var imageLink: String
    lateinit var imageBits: Bitmap

    companion object {
        // List of all the categories
        val labels = listOf(
                R.id.category_animal,
                R.id.category_human,
                R.id.category_neither)
        const val domain = "wildlifeimagevm.westus2.cloudapp.azure.com:8080"
        const val jsonImageLabel = "imageLabel"
        const val jsonImageId = "imageId"
        const val jsonImageLink = "imageLink"

    }

    fun retrievePhotoInfo() = async(UI){
        val imageJson = restRequestJson(createJsonRequest("GET", "images")).await()

        imageJson?.let {
            originalImageLabel = imageJson[jsonImageLabel].toString()
            currentImageLabel = originalImageLabel
            imageId = imageJson[jsonImageId].toString()
            imageLink = imageJson[jsonImageLink].toString()
        }

        imageBits = retrievePhoto().await()
    }

    fun postNewLabel() = async(UI){
        val postRequest = createJsonRequest("POST", "new_label")
        postRequest.setRequestProperty(jsonImageId, imageId)
        postRequest.setRequestProperty("newLabel", currentImageLabel)
        postRequest.setRequestProperty("originalLabel", originalImageLabel)

        restRequestJson(postRequest).await()
    }

    private fun restRequestJson(connection: HttpURLConnection): Deferred<JsonObject?> = async(CommonPool) {
        try {
            lateinit var infoJson: JsonObject
            val job = async(CommonPool) {
                connection.connect()
                val stream: InputStream = connection.inputStream
                infoJson = Parser().parse(stream) as JsonObject
            }
            job.await()
            return@async infoJson
        } catch (exception: Exception){
            throw exception
        }
    }

    private fun createJsonRequest(method: String, uri: String): HttpURLConnection {
        val connection: HttpURLConnection =
                URL(listOf("http:", "", domain, uri).reduce { acc, s -> "$acc/$s" }).openConnection()
                        as HttpURLConnection
        connection.requestMethod = method
        return connection
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
}
