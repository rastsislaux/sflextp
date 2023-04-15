package me.leepsky.sflextp.processing

import me.leepsky.sflextp.packet.*
import java.io.File
import java.lang.UnsupportedOperationException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.io.path.*

open class FilesystemPacketProcessor: DecomposedSftpPacketProcessor() {

    protected open val dirHandles = mutableMapOf<String, Path?>()

    protected open val fileHandles = mutableMapOf<String, FileHandle>()

    /**
     * Process SSH_FXP_INIT. Always responds with SSH_FXP_VERSION.
     */
    override fun process(packet: SftpPacket1): SftpPacket2 {
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
    override fun process(packet: SftpPacket3): SftpPacket {
        val path = Path(packet.filename)
        return process(packet, path)
    }

    protected open fun process(packet: SftpPacket3, path: Path): SftpPacket {
        fun read(): SftpPacket {
            if (!path.isRegularFile()) {
                return SftpPacket101(packet.id, SftpPacket101.Companion.StatusCode.SSH_FX_FAILURE,
                    "Not a file.", Locale.ENGLISH)
            }

            val handle = UUID.randomUUID().toString()
            fileHandles[handle] = FileHandle(path, packet.pflags, bytes=path.readBytes())

            return SftpPacket102(packet.id, handle)
        }

        fun write(): SftpPacket {
            val handle = UUID.randomUUID().toString()
            if (!path.exists()) {
                path.createFile()
            }
            fileHandles[handle] = FileHandle(path, packet.pflags)
            return SftpPacket102(packet.id, handle)
        }

        return when {
            packet.pflags.isRead -> read()
            packet.pflags.isWrite -> write()
            else -> TODO("File open mode is not yet implemented.")
        }
    }

    /**
     * Process SSH_FXP_CLOSE.
     *
     * For handles created by SSH_FXP_OPENDIR - does nothing, always returns SSH_FX_OK.
     * For handles created by SSH_FXP_OPEN - clears the memory, returns SSH_FX_OK.
     */
    override fun process(packet: SftpPacket4): SftpPacket101 {
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
    override fun process(packet: SftpPacket5): SftpPacket {
        val handle = packet.handle

        if (handle !in fileHandles || !fileHandles[handle]!!.pflags.isRead) {
            return SftpPacket101(packet.id, SftpPacket101.Companion.StatusCode.SSH_FX_FAILURE,
                "Invalid handle.", Locale.ENGLISH)
        }

        val byteArray = fileHandles[handle]!!.bytes!!

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

    /**
     * Process SSH_FXP_WRITE.
     *
     * The write will extend the file if writing beyond the end of the file.
     *    It is legal to write way beyond the end of the file; the semantics
     *    are to write zeroes from the end of the file to the specified offset
     *    and then the data.  On most operating systems, such writes do not
     *    allocate disk space but instead leave "holes" in the file.
     *
     * The server responds to a write request with a SSH_FXP_STATUS message.
     */
    override fun process(packet: SftpPacket6): SftpPacket {
        if (packet.handle !in fileHandles || !fileHandles[packet.handle]!!.pflags.isWrite) {
            return SftpPacket101(packet.id, SftpPacket101.Companion.StatusCode.SSH_FX_FAILURE,
                "Invalid handle.", Locale.ENGLISH)
        }

        val handle = fileHandles[packet.handle]!!
        handle.path.appendBytes(packet.data)

        return SftpPacket101(packet.id, SftpPacket101.Companion.StatusCode.SSH_FX_OK,
            "All OK.", Locale.ENGLISH)
    }

    override fun process(packet: SftpPacket11): SftpPacket {
        val path = Path(packet.path)
        return process(packet, path)
    }

    protected open fun process(packet: SftpPacket11, path: Path): SftpPacket {
        if (!Files.isDirectory(path)) {
            return SftpPacket101(packet.id, SftpPacket101.Companion.StatusCode.SSH_FX_FAILURE,
                "Not a directory.", Locale.ENGLISH)
        }

        val handle = UUID.randomUUID().toString()
        dirHandles[handle] = path

        return SftpPacket102(packet.id, handle)
    }

    override fun process(packet: SftpPacket12): SftpPacket {

        fun makeLongname(path: Path): String {
            val perms = (if (path.isDirectory()) "d" else "-") +
                        (if (path.isReadable()) "r" else "-") +
                        (if (path.isWritable()) "w" else "-") +
                        if (path.isExecutable()) "x" else "-"
            val owner = try { Files.getOwner(path).name } catch (ex: UnsupportedOperationException) { "unknown" }
            val size = Files.size(path)
            val lastModified = Date(path.getLastModifiedTime().toMillis())
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

    override fun process(packet: SftpPacket13): SftpPacket {
        val path = Path(packet.filename)
        return process(packet, path)
    }

    protected open fun process(packet: SftpPacket13, path: Path): SftpPacket {
        if (Files.isDirectory(path)) {
            return SftpPacket101(packet.id, SftpPacket101.Companion.StatusCode.SSH_FX_FAILURE,
                "Is a directory.", Locale.ENGLISH)
        }

        if (!Files.exists(path)) {
            return SftpPacket101(packet.id, SftpPacket101.Companion.StatusCode.SSH_FX_NO_SUCH_FILE,
                "No such file.", Locale.ENGLISH)
        }

        path.deleteIfExists()

        return SftpPacket101(packet.id, SftpPacket101.Companion.StatusCode.SSH_FX_OK,
            "OK.", Locale.ENGLISH)
    }

    /**
     * Process SSH_FXP_MKDIR
     *
     * An error will be returned if a file or
     *    directory with the specified path already exists.  The server will
     *    respond to this request with a SSH_FXP_STATUS message.
     */
    override fun process(packet: SftpPacket14): SftpPacket {
        val path = Path(packet.path)
        return process(packet, path)
    }

    protected open fun process(packet: SftpPacket14, path: Path): SftpPacket {
        if (Files.exists(path)) {
            return SftpPacket101(packet.id, SftpPacket101.Companion.StatusCode.SSH_FX_FAILURE,
                "Already exists.", Locale.ENGLISH)
        }

        File(path.toString()).mkdirs()

        return SftpPacket101(packet.id, SftpPacket101.Companion.StatusCode.SSH_FX_OK,
            "Created.", Locale.ENGLISH)
    }

    /**
     * Process SSH_FXP_RMDIR.
     *
     * An error will be returned if no directory
     *    with the specified path exists, or if the specified directory is not
     *    empty, or if the path specified a file system object other than a
     *    directory.  The server responds to this request with a SSH_FXP_STATUS
     *    message.
     */
    override fun process(packet: SftpPacket15): SftpPacket {
        val path = Path(packet.path)
        return process(packet, path)
    }

    protected open fun process(packet: SftpPacket15, path: Path): SftpPacket {
        if (!path.isDirectory()) {
            return SftpPacket101(packet.id, SftpPacket101.Companion.StatusCode.SSH_FX_FAILURE,
                "Not a directory.", Locale.ENGLISH)
        }

        if (path.listDirectoryEntries().isNotEmpty()) {
            return SftpPacket101(packet.id, SftpPacket101.Companion.StatusCode.SSH_FX_FAILURE,
                "Directory not empty.", Locale.ENGLISH)
        }

        path.deleteIfExists()

        return SftpPacket101(packet.id, SftpPacket101.Companion.StatusCode.SSH_FX_OK,
            "OK.", Locale.ENGLISH)
    }

    override fun process(packet: SftpPacket16): SftpPacket {
        val path = Path(packet.path)
        return process(packet, path)
    }

    protected open fun process(packet: SftpPacket16, path: Path): SftpPacket {
        val realPath = try { path.toRealPath() }

        catch (e: NoSuchFileException) {
            return SftpPacket101(packet.id, SftpPacket101.Companion.StatusCode.SSH_FX_NO_SUCH_FILE,
                "No such file.", Locale.ENGLISH)
        }

        return SftpPacket104(
            packet.id,
            listOf(SftpFile(realPath.toString(), realPath.toString(), getFileAttributes(realPath)))
        )
    }

    override fun process(packet: SftpPacket17): SftpPacket {
        val path = Path(packet.path)
        return process(packet, path)
    }

    protected open fun process(packet: SftpPacket17, path: Path): SftpPacket {
        return SftpPacket105(packet.id, getFileAttributes(path))
    }

    /**
     * Process SSH_FXP_RENAME.
     *
     * It is an error if there already exists a file
     *    with the name specified by newpath.  The server may also fail rename
     *    requests in other situations, for example if 'oldpath' and 'newpath'
     *    point to different file systems on the server.
     *
     * The server will respond to this request with a SSH_FXP_STATUS
     *    message.
     */
    override fun process(packet: SftpPacket18): SftpPacket {
        val oldPath = Path(packet.oldPath)
        val newPath = Path(packet.newPath)
        return process(packet, oldPath, newPath)
    }

    protected open fun process(packet: SftpPacket18, oldPath: Path, newPath: Path): SftpPacket {
        if (!oldPath.exists()) {
            return SftpPacket101(packet.id, SftpPacket101.Companion.StatusCode.SSH_FX_NO_SUCH_FILE,
                "File does not exist.", Locale.ENGLISH)
        }

        if (newPath.exists()) {
            return SftpPacket101(packet.id, SftpPacket101.Companion.StatusCode.SSH_FX_FAILURE,
                "File exists.", Locale.ENGLISH)
        }

        oldPath.moveTo(newPath, overwrite = false)

        return SftpPacket101(packet.id, SftpPacket101.Companion.StatusCode.SSH_FX_OK,
            "OK.", Locale.ENGLISH)
    }

    private fun getFileAttributes(path: Path): FileAttributes {
        return FileAttributes(0)
    }

}
