package com.tachyonmusic.domain.use_case.profile

import android.content.Context
import android.net.Uri
import com.tachyonmusic.app.R
import com.tachyonmusic.database.data.data_source.Database
import com.tachyonmusic.util.ChannelEvent
import com.tachyonmusic.util.EventSeverity
import com.tachyonmusic.util.UiText
import com.tachyonmusic.util.domain.EventChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class ExportDatabaseEvent(override val message: UiText, override val severity: EventSeverity) :
    ChannelEvent


class ExportDatabase(
    private val database: Database,
    private val context: Context,
    private val eventChannel: EventChannel
) {
    suspend operator fun invoke(destination: Uri?) = withContext(Dispatchers.IO) {
        if (destination == null) {
            eventChannel.push(
                ExportDatabaseEvent(
                    UiText.StringResource(R.string.database_export_destination_empty),
                    EventSeverity.Warning
                )
            )
            return@withContext
        }

        database.checkpoint()
        try {
            val jsonString = database.toJson()
            val outputStream =
                context.contentResolver.openOutputStream(destination) ?: throw IOException()
            outputStream.write(jsonString.encodeToByteArray())
            outputStream.close()

            eventChannel.push(
                ExportDatabaseEvent(
                    UiText.StringResource(R.string.database_export_success),
                    EventSeverity.Info
                )
            )

        } catch (e: IOException) {
            e.printStackTrace()
            eventChannel.push(
                ExportDatabaseEvent(
                    UiText.StringResource(
                        R.string.database_export_error,
                        e.localizedMessage ?: "Unknown"
                    ),
                    EventSeverity.Error
                )
            )
        }
    }
}