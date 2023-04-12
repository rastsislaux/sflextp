package me.leepsky.esftp.processing

import me.leepsky.esftp.packet.FileAttributes
import me.leepsky.esftp.packet.FileAttributes.Companion.Type
import me.leepsky.esftp.packet.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import kotlin.io.path.Path

open class ESftpPacketProcessor: SftpPacketProcessor {

    private val dirHandles = mutableMapOf<String, Path?>()

    override fun process(packet: SftpPacket): SftpPacket {
        return when (packet) {
            is SftpPacket1 -> process(packet)
            is SftpPacket4 -> process(packet)
            is SftpPacket11 -> process(packet)
            is SftpPacket12 -> process(packet)
            is SftpPacket16 -> process(packet)
            else -> TODO("Processing of packet ${packet.typeId} is not yet implemented.")
        }
    }

    protected open fun process(packet: SftpPacket1): SftpPacket2 {
        return SftpPacket2(3)
    }

    protected open fun process(packet: SftpPacket4): SftpPacket101 {
        return SftpPacket101(packet.requestId, SftpPacket101.Companion.StatusCode.SSH_FX_OK,
                 "All good.", Locale.ENGLISH)
    }

    protected open fun process(packet: SftpPacket11): SftpPacket {
        val path = Path(packet.path)

        if (!Files.isDirectory(path)) {
            return SftpPacket101(packet.requestId, SftpPacket101.Companion.StatusCode.SSH_FX_NOT_A_DIRECTORY,
                     "Not a directory.", Locale.ENGLISH)
        }

        val handle = UUID.randomUUID().toString()
        dirHandles[handle] = path

        return SftpPacket102(packet.requestId, handle)
    }

    protected open fun process(packet: SftpPacket12): SftpPacket {
        val handle = packet.handle

        if (handle !in dirHandles) {
            return SftpPacket101(packet.requestId, SftpPacket101.Companion.StatusCode.SSH_FX_INVALID_HANDLE,
                "Invalid handle.", Locale.ENGLISH)
        }

        if (dirHandles[handle] == null) {
            dirHandles.remove(handle)
            return SftpPacket101(packet.requestId, SftpPacket101.Companion.StatusCode.SSH_FX_EOF,
                "EOF.", Locale.ENGLISH)
        }

        val files = Files.list(dirHandles[handle])
            .map { SftpFile(it.fileName.toString(), it.toString(), getFileAttributes(it)) }
            .toList()
        dirHandles[handle] = null

        return SftpPacket104(packet.requestId, files, true)
    }

    protected open fun process(packet: SftpPacket16): SftpPacket104 {
        val path = Path(packet.originalPath).toRealPath()

        return SftpPacket104(
            packet.requestId,
            listOf(SftpFile(path.toString(), path.toString(), getFileAttributes(path))),
            true
        )
    }

    private fun getFileAttributes(path: Path): FileAttributes {
        return FileAttributes(0, getFileType(path))
    }

    private fun getFileType(path: Path): Type {
        val attrs = Files.readAttributes(path, BasicFileAttributes::class.java)
        return when {
            attrs.isRegularFile -> Type.SSH_FILEXFER_TYPE_REGULAR
            attrs.isDirectory -> Type.SSH_FILEXFER_TYPE_DIRECTORY
            attrs.isSymbolicLink -> Type.SSH_FILEXFER_TYPE_SYMLINK
            else -> Type.SSH_FILEXFER_TYPE_UNKNOWN
        }
    }

}