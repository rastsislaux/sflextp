package me.leepsky.sflextp.processing

import me.leepsky.sflextp.packet.*
import java.nio.file.Files
import java.nio.file.Path
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.readBytes

open class SflextpPacketProcessor: SftpPacketProcessor {

    private val dirHandles = mutableMapOf<String, Path?>()

    private val fileHandles = mutableMapOf<String, ByteArray>()

    override fun process(packet: SftpPacket): SftpPacket {
        return when (packet) {
            is SftpPacket1  -> process(packet)
            is SftpPacket3  -> process(packet)
            is SftpPacket4  -> process(packet)
            is SftpPacket5  -> process(packet)
            is SftpPacket11 -> process(packet)
            is SftpPacket12 -> process(packet)
            is SftpPacket16 -> process(packet)
            is SftpPacket17 -> process(packet)
            else -> TODO("Processing of packet ${packet.typeId} is not yet implemented.")
        }
    }

    /**
     * Process SSH_FXP_INIT. Always responds with SSH_FXP_VERSION.
     */
    protected open fun process(packet: SftpPacket1): SftpPacket2 {
        return SftpPacket2(3)
    }

    /**
     * Process SSH_FXP_OPEN.
     *
     * Regardless the server operating system, the file will always be
     * opened in "binary" mode (i.e., no translations between different
     * character sets and newline encodings).
     *
     * The response to this message will be either SSH_FXP_HANDLE (if the
     * operation is successful) or SSH_FXP_STATUS (if the operation fails).
     */
    protected open fun process(packet: SftpPacket3): SftpPacket {
        val path = Path(packet.filename)

        if (!path.isRegularFile()) {
            return SftpPacket101(packet.id, SftpPacket101.Companion.StatusCode.SSH_FX_FAILURE,
                "Not a file.", Locale.ENGLISH)
        }

        val handle = UUID.randomUUID().toString()
        fileHandles[handle] = path.readBytes()

        return SftpPacket102(packet.id, handle)
    }

    /**
     * Process SSH_FXP_CLOSE.
     *
     * For handles created by SSH_FXP_OPENDIR - does nothing, always returns SSH_FX_OK.
     * For handles created by SSH_FXP_OPEN - clears the memory, returns SSH_FX_OK.
     */
    protected open fun process(packet: SftpPacket4): SftpPacket101 {
        if (packet.handle in fileHandles) {
            fileHandles.remove(packet.handle)
        }

        return SftpPacket101(packet.id, SftpPacket101.Companion.StatusCode.SSH_FX_OK,
                 "All good.", Locale.ENGLISH)
    }

    /**
     * Process SSH_FXP_READ.
     *
     * In response to this request, the server will read as many bytes as it
     *    can from the file (up to `len'), and return them in a SSH_FXP_DATA
     *    message.  If an error occurs or EOF is encountered before reading any
     *    data, the server will respond with SSH_FXP_STATUS.  For normal disk
     *    files, it is guaranteed that this will read the specified number of
     *    bytes, or up to end of file.  For e.g.  device files this may return
     *    fewer bytes than requested.
     */
    protected open fun process(packet: SftpPacket5): SftpPacket {
        val handle = packet.handle

        if (handle !in fileHandles) {
            return SftpPacket101(packet.id, SftpPacket101.Companion.StatusCode.SSH_FX_FAILURE,
                "Invalid handle.", Locale.ENGLISH)
        }

        val byteArray = fileHandles[handle]!!

        if (packet.offset > byteArray.size) {
            return SftpPacket101(packet.id, SftpPacket101.Companion.StatusCode.SSH_FX_EOF,
                "EOF.", Locale.ENGLISH)
        }

        val data = when (val lIndex = packet.offset.toInt() + packet.len) {
            in 0..byteArray.size -> byteArray.copyOfRange(packet.offset.toInt(), lIndex)
            else -> byteArray.copyOfRange(packet.offset.toInt(), byteArray.size)
        }

        return SftpPacket103(packet.id, data)
    }

    protected open fun process(packet: SftpPacket11): SftpPacket {
        val path = Path(packet.path)

        if (!Files.isDirectory(path)) {
            return SftpPacket101(packet.id, SftpPacket101.Companion.StatusCode.SSH_FX_FAILURE,
                     "Not a directory.", Locale.ENGLISH)
        }

        val handle = UUID.randomUUID().toString()
        dirHandles[handle] = path

        return SftpPacket102(packet.id, handle)
    }

    protected open fun process(packet: SftpPacket12): SftpPacket {

        fun makeLongname(path: Path): String {
            val file = path.toFile()
            val perms = (if (file.isDirectory) "d" else "-") +
                        (if (file.canRead()) "r" else "-") +
                        (if (file.canWrite()) "w" else "-") +
                        if (file.canExecute()) "x" else "-"
            val owner = Files.getOwner(path).name
            val size = Files.size(path)
            val lastModified = Date(file.lastModified())
            val formatter = DateTimeFormatter.ofPattern("MMM d HH:mm")
            val formattedDate = formatter.format(lastModified.toInstant().atZone(TimeZone.getDefault().toZoneId()))
            return "$perms $owner $size $formattedDate ${path.fileName}"
        }

        val handle = packet.handle

        if (handle !in dirHandles) {
            return SftpPacket101(packet.id, SftpPacket101.Companion.StatusCode.SSH_FX_FAILURE,
                "Invalid handle.", Locale.ENGLISH)
        }

        if (dirHandles[handle] == null) {
            dirHandles.remove(handle)
            return SftpPacket101(packet.id, SftpPacket101.Companion.StatusCode.SSH_FX_EOF,
                "EOF.", Locale.ENGLISH)
        }

        val files = Files.list(dirHandles[handle])
            .map { SftpFile(it.fileName.toString(), makeLongname(it), getFileAttributes(it)) }
            .toList()
        dirHandles[handle] = null

        return SftpPacket104(packet.id, files)
    }

    protected open fun process(packet: SftpPacket16): SftpPacket104 {
        val path = Path(packet.path).toRealPath()

        return SftpPacket104(
            packet.id,
            listOf(SftpFile(path.toString(), path.toString(), getFileAttributes(path)))
        )
    }

    protected open fun process(packet: SftpPacket17): SftpPacket {
        val path = Path(packet.path)
        return SftpPacket105(packet.id, getFileAttributes(path))
    }

    private fun getFileAttributes(path: Path): FileAttributes {
        return FileAttributes(0)
    }

}