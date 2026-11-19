package su.afk.yummy.tv.feature.player.common.model

/** Снимок диагностики плеера для оверлея при записи логов. */
data class PlayerDiagnosticsInfo(
    val playbackState: Int,
    val playWhenReady: Boolean,
    val bufferedAheadMs: Long,
    val totalBufferedMs: Long,
    val videoWidth: Int,
    val videoHeight: Int,
    val videoBitrate: Int,
    val videoCodec: String?,
    val droppedFrames: Int,
    val bandwidthBps: Long,
    val freeRamMb: Long,
    val totalRamMb: Long,
    val network: String,
)
