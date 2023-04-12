package me.leepsky.esftp

import me.leepsky.esftp.FileAttributes.Companion.Type
import me.leepsky.esftp.packet.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import kotlin.io.path.Path

open class SftpPacketProcessor {

    fun process(packet: SftpPacket): SftpPacket {
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

        return SftpPacket102(packet.requestId, path.toString())
    }

    protected open fun process(packet: SftpPacket12): SftpPacket {
        val path = Path(packet.handle)

        if (!Files.isDirectory(path)) {
            return SftpPacket101(packet.requestId, SftpPacket101.Companion.StatusCode.SSH_FX_INVALID_HANDLE,
                "Not a directory.", Locale.ENGLISH)
        }

        val files = Files.list(path).toList().associate { it.toString() to getFileAttributes(it) }

        return SftpPacket104(packet.requestId, files, false)
    }

    protected open fun process(packet: SftpPacket16): SftpPacket104 {
        val path = Path(packet.originalPath).toRealPath()

        return SftpPacket104(
            packet.requestId,
            mapOf(path.toString() to getFileAttributes(path)),
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