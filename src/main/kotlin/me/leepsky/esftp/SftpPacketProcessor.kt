package me.leepsky.esftp

import me.leepsky.esftp.FileAttributes.Companion.Type
import me.leepsky.esftp.packet.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import kotlin.io.path.Path

open class SftpPacketProcessor {

    fun processPacket(packet: SftpPacket): SftpPacket {
        return when (packet) {
            is SftpPacket1 -> processPacket(packet)
            is SftpPacket11 -> processPacket(packet)
            is SftpPacket12 -> processPacket(packet)
            is SftpPacket16 -> processPacket(packet)
            else -> TODO("Processing of packet ${packet.typeId} is not yet implemented.")
        }
    }

    protected open fun processPacket(packet: SftpPacket1): SftpPacket2 {
        return SftpPacket2(3)
    }

    protected open fun processPacket(packet: SftpPacket11): SftpPacket {
        val path = Path(packet.path)

        if (!Files.isDirectory(path)) {
            return SftpPacket101(packet.requestId, SftpPacket101.Companion.StatusCode.SSH_FX_NOT_A_DIRECTORY,
                "Not a directory.", Locale.ENGLISH)
        }

        return SftpPacket102(packet.requestId, path.toString())
    }

    protected open fun processPacket(packet: SftpPacket12): SftpPacket {
        val path = Path(packet.handle)

        if (!Files.isDirectory(path)) {
            return SftpPacket101(packet.requestId, SftpPacket101.Companion.StatusCode.SSH_FX_INVALID_HANDLE,
                "Not a directory.", Locale.ENGLISH)
        }

        val files = Files.list(path).toList()
            .map { it.toString() to getFileAttributes(it) }
            .toMap()

        return SftpPacket104(packet.requestId, files, true)
    }

    protected open fun processPacket(packet: SftpPacket16): SftpPacket104 {
        val path = Path(packet.originalPath).toRealPath()
        val type = getFileType(path)

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