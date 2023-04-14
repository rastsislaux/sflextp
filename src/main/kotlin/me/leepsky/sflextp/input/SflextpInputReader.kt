package me.leepsky.sflextp.input

import me.leepsky.sflextp.packet.*
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.attribute.FileAttribute

open class SflextpInputReader: SftpInputReader {

    private lateinit var inputStream: InputStream

    override fun setInputStream(inputStream: InputStream) {
        this.inputStream = inputStream
    }

    override fun read(): SftpPacket {
        val lengthBytes = inputStream.readNBytes(LENGTH_BYTE_COUNT)
        val length = ByteBuffer.wrap(lengthBytes)
            .order(BYTE_ORDER)
            .int

        val packet = ByteBuffer.wrap(inputStream.readNBytes(length))
            .order(BYTE_ORDER)

        return when (val typeId = packet.get().toInt()) {
            SftpPacketType.SSH_FXP_INIT     -> read1(packet)
            SftpPacketType.SSH_FXP_OPEN     -> read3(packet)
            SftpPacketType.SSH_FXP_CLOSE    -> read4(packet)
            SftpPacketType.SSH_FXP_READ     -> read5(packet)
            SftpPacketType.SSH_FXP_WRITE    -> read6(packet)
            SftpPacketType.SSH_FXP_OPENDIR  -> read11(packet)
            SftpPacketType.SSH_FXP_READDIR  -> read12(packet)
            SftpPacketType.SSH_FXP_REMOVE   -> read13(packet)
            SftpPacketType.SSH_FXP_MKDIR    -> read14(packet)
            SftpPacketType.SSH_FXP_REALPATH -> read16(packet)
            SftpPacketType.SSH_FXP_STAT     -> read17(packet)
            else -> TODO("Reading packet $typeId not supported yet.")
        }
    }

    protected open fun read1(packet: ByteBuffer) = SftpPacket1(packet.int)

    protected open fun read3(packet: ByteBuffer): SftpPacket3 {
        val id = packet.int
        val filenameLength = packet.int
        val filename = readUTF8String(packet, filenameLength)
        val pFlags = SftpPacket3.Companion.PFlags(packet.int)
        val attrs = readAttrs(packet)

        return SftpPacket3(id, filename, pFlags, attrs)
    }

    protected open fun read4(packet: ByteBuffer): SftpPacket4 {
        val requestId = packet.int
        val handleLength = packet.int
        val handle = readUTF8String(packet, handleLength)

        return SftpPacket4(requestId, handle)
    }

    protected open fun read5(packet: ByteBuffer): SftpPacket5 {
        val id = packet.int
        val handleLength = packet.int
        val handle = readUTF8String(packet, handleLength)
        val offset = packet.long
        val len = packet.int

        return SftpPacket5(id, handle, offset, len)
    }

    protected open fun read6(packet: ByteBuffer): SftpPacket6 {
        val id = packet.int
        val handleLength = packet.int
        val handle = readUTF8String(packet, handleLength)
        val offset = packet.long
        val dataLength = packet.int
        val data = readBytes(packet, dataLength)

        return SftpPacket6(id, handle, offset, data)
    }

    protected open fun read11(packet: ByteBuffer): SftpPacket11 {
        val requestId = packet.int
        val pathLength = packet.int
        val path = readUTF8String(packet, pathLength)

        return SftpPacket11(requestId, path)
    }

    protected open fun read12(packet: ByteBuffer): SftpPacket12 {
        val requestId = packet.int
        val handleLength = packet.int
        val handle = readUTF8String(packet, handleLength)

        return SftpPacket12(requestId, handle)
    }

    protected open fun read13(packet: ByteBuffer): SftpPacket13 {
        val id = packet.int
        val filenameLength = packet.int
        val filename = readUTF8String(packet, filenameLength)

        return SftpPacket13(id, filename)
    }

    protected open fun read14(packet: ByteBuffer): SftpPacket14 {
        val id = packet.int
        val pathLength = packet.int
        val path = readUTF8String(packet, pathLength)
        val attrs = readAttrs(packet)

        return SftpPacket14(id, path, attrs)
    }

    protected open fun read16(packet: ByteBuffer): SftpPacket16 {
        val requestId = packet.int
        val originalPathLength = packet.int
        val originalPath = readUTF8String(packet, originalPathLength)
        return SftpPacket16(requestId, originalPath)
    }

    protected open fun read17(packet: ByteBuffer): SftpPacket17 {
        val id = packet.int
        val pathLength = packet.int
        val path = readUTF8String(packet, pathLength)
        return SftpPacket17(id, path)
    }

    companion object {
        private const val LENGTH_BYTE_COUNT = 4
        private val BYTE_ORDER = ByteOrder.BIG_ENDIAN

        private fun readUTF8String(buffer: ByteBuffer, length: Int): String {
            return String(readBytes(buffer, length))
        }

        private fun readBytes(buffer: ByteBuffer, length: Int): ByteArray {
            val bytes = ByteArray(length)
            for (i in 0 until length) {
                bytes[i] = buffer.get()
            }
            return bytes
        }

        private fun readAttrs(buffer: ByteBuffer): FileAttributes {
            val flags = buffer.int

            val size = if (flags and FileAttributeFlag.SSH_FILEXFER_ATTR_SIZE != 0)       { buffer.long } else null
            val uid = if (flags and FileAttributeFlag.SSH_FILEXFER_ATTR_UIDGID != 0)      { buffer.int }  else null
            val gid = if (flags and FileAttributeFlag.SSH_FILEXFER_ATTR_UIDGID != 0)      { buffer.int }  else null
            val atime = if (flags and FileAttributeFlag.SSH_FILEXFER_ATTR_ACMODTIME != 0) { buffer.int }  else null
            val mtime = if (flags and FileAttributeFlag.SSH_FILEXFER_ATTR_ACMODTIME != 0) { buffer.int }  else null

            return FileAttributes(flags, size, uid, gid, atime, mtime)
        }
    }

}