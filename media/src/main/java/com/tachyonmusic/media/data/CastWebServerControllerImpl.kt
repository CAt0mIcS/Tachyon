package com.tachyonmusic.media.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.net.wifi.WifiManager
import androidx.media3.common.MimeTypes
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.media.domain.CastWebServerController
import dagger.hilt.android.qualifiers.ApplicationContext
import fi.iki.elonen.NanoHTTPD
import java.io.FileNotFoundException
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.ByteBuffer
import java.nio.ByteOrder


class CastWebServerControllerImpl(
    @ApplicationContext private val context: Context,
    private val log: Logger
) : CastWebServerController {
    private val server = WebServer()
    private var ip: String? = null

    override fun start() {
        ip = getIPAddress()
        if (ip == null)
            TODO("Not connected to WIFI: Warn user")

        server.start()
    }

    override fun stop() {
        server.stop()
        ip = null
    }

    override fun getUrl(uri: Uri): String {
        assert(ip != null) { "Server must be started before querying urls for media" }
        return "http://$ip:$PORT/${uri}"
    }

    private inner class WebServer : NanoHTTPD(PORT) {
        override fun serve(session: IHTTPSession?): Response {
            val playbackUri = Uri.parse(session?.uri ?: return invalidServeResponse)
                .buildUpon()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .build()

            try {
                val inputStream = context.contentResolver.openInputStream(playbackUri)
                    ?: return invalidServeResponse

                // TODO: Different mime types for other file extensions
                return newChunkedResponse(Response.Status.OK, MimeTypes.AUDIO_MPEG, inputStream)
            } catch (e: FileNotFoundException) {
                TODO("File not found $playbackUri")
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
        private val PORT = 8080
    }
}