package com.tachyonmusic.media.data

import android.content.Context
import android.net.Uri
import android.net.wifi.WifiManager
import androidx.media3.common.MimeTypes
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.media.domain.CastWebServerController
import fi.iki.elonen.NanoHTTPD
import java.io.FileNotFoundException
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.ByteBuffer
import java.nio.ByteOrder


class CastWebServerControllerImpl(
    private val context: Context,
    private val log: Logger
) : CastWebServerController {
    private val server = WebServer()
    private var ip: String? = null

    private val items = mutableListOf<Uri>()

    override fun start(newItems: List<Uri>) {
        ip = getIPAddress()
        if (ip == null)
            TODO("Not connected to WIFI: Warn user")

        items += newItems
        server.start()
    }

    override fun stop() {
        server.stop()
        ip = null
        items.clear()
    }

    override fun getUrl(uri: Uri): String {
        assert(ip != null) { "Server must be started before querying urls for media" }
        assert(items.contains(uri)) { "Uri $uri not contained in the items set in start()" }

        return "http://$ip:$PORT/${items.indexOf(uri)}".apply {
            log.debug("Url for $uri created: $this")
        }
    }

    private inner class WebServer : NanoHTTPD(PORT) {
        override fun serve(session: IHTTPSession?): Response {
            /**
             * We fail reading the Uri if we parse [session.uri] using [Uri] because we're
             * apparently reading a directory. But using the Uri from the media items works. Thus the
             * Url will contain the index of the Uri in [items]
             */
            val playbackId =
                session?.uri?.removePrefix("/")?.toIntOrNull() ?: return invalidServeResponse
            val playbackUri = items.getOrNull(playbackId) ?: return invalidServeResponse

            return try {
                val inputStream = context.contentResolver.openInputStream(playbackUri)

                // TODO: Different mime types for other file extensions
                newChunkedResponse(Response.Status.OK, MimeTypes.AUDIO_MPEG, inputStream)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                invalidServeResponse
            }
        }

        private val invalidServeResponse =
            newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found")
    }

    private fun getIPAddress(): String? {
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (wifiManager.connectionInfo != null) {
            try {
                return InetAddress.getByAddress(
                    ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                        .putInt(wifiManager.connectionInfo.ipAddress)
                        .array()
                ).hostAddress
            } catch (e: UnknownHostException) {
                log.error("Error finding IpAddress: " + e.message)
            }
        }
        return null
    }

    companion object {
        private const val PORT = 8080
    }
}