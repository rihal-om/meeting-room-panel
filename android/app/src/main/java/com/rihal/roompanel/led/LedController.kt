package com.rihal.roompanel.led

import android.content.Context
import android.net.Uri
import android.util.Log
import com.rihal.roompanel.domain.RoomStatus

/**
 * The colour of the panel's side light bar. Bright, saturated shades so the status reads from
 * down the corridor; the on-screen [com.rihal.roompanel.ui.StatusColors] are deliberately
 * muted for white text and would look dim on the LEDs.
 */
enum class LedColor(val hex: String) {
    FREE("#00EE00"),
    STARTING_SOON("#EEEE00"),
    BUSY("#EE0000"),
    ;

    companion object {
        /** Green when free, yellow when a meeting starts soon, red when in use. */
        fun of(status: RoomStatus): LedColor = when (status) {
            is RoomStatus.Free -> FREE
            is RoomStatus.StartingSoon -> STARTING_SOON
            is RoomStatus.Busy -> BUSY
        }
    }
}

/** Drives the side light bar, where the hardware has one. */
interface LedController {
    fun show(color: LedColor)

    companion object {
        /**
         * Milesight DS-series room panels (the DS7510 and siblings) expose their light bar through
         * a pre-installed system content provider. Any other hardware — or the emulator — gets a
         * no-op so the app still runs everywhere.
         */
        fun create(context: Context): LedController {
            val app = context.applicationContext
            return if (MilesightLed.isAvailable(app)) MilesightLed(app) else NoopLed
        }
    }
}

/** No side light bar on this device. */
object NoopLed : LedController {
    override fun show(color: LedColor) = Unit
}

/**
 * Milesight DS7510 and its siblings. The privileged write to the LED device is done by the
 * pre-installed `com.ds.system` service in response to this call, so the app needs no root or
 * system permission of its own — only the matching `<queries>` entry in the manifest so Android
 * 11+ package-visibility lets it see the provider.
 */
class MilesightLed(private val context: Context) : LedController {

    override fun show(color: LedColor) {
        runCatching {
            context.contentResolver.call(PROVIDER, METHOD_CUSTOM, color.hex, null)
        }.onFailure { Log.w(TAG, "could not set LED to ${color.hex}", it) }
    }

    companion object {
        private const val TAG = "MilesightLed"
        const val AUTHORITY = "com.ds.system.provider"

        /** Takes a "#RRGGBB" argument, which the service writes to the LED driver. */
        private const val METHOD_CUSTOM = "set_light_custom"
        private val PROVIDER: Uri = Uri.parse("content://$AUTHORITY")

        fun isAvailable(context: Context): Boolean =
            context.contentResolver.acquireContentProviderClient(PROVIDER)?.use { true } ?: false
    }
}
