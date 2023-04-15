package me.leepsky.sflextp.processing

import me.leepsky.sflextp.packet.SftpPacket3
import java.nio.file.Path

data class FileHandle(
    val path: Path,
    val pflags: SftpPacket3.Companion.PFlags,
    val bytes: ByteArray? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileHandle

        if (path != other.path) return false
        if (!bytes.contentEquals(other.bytes)) return false
        if (pflags != other.pflags) return false

        return true
    }

    override fun hashCode(): Int {
        var result = path.hashCode()
        result = 31 * result + bytes.contentHashCode()
        result = 31 * result + pflags.hashCode()
        return result
    }
}