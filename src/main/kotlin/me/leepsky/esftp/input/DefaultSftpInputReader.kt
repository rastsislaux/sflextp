package me.leepsky.esftp.input

import me.leepsky.esftp.packet.*
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

open class DefaultSftpInputReader: SftpInputReader {

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
            SftpPacketType.SSH_FXP_INIT -> readSftpPacket1(packet)
            SftpPacketType.SSH_FXP_CLOSE -> readSftpPacket4(packet)
            SftpPacketType.SSH_FXP_OPENDIR -> readSftpPacket11(packet)
            SftpPacketType.SSH_FXP_READDIR -> readSftpPacket12(packet)
            SftpPacketType.SSH_FXP_REALPATH -> readSftpPacket16(packet)
            else -> TODO("Reading packet $typeId not supported yet.")
        }
    }

    protected open fun readSftpPacket1(packet: ByteBuffer) = SftpPacket1(packet.int)

    protected open fun readSftpPacket4(packet: ByteBuffer): SftpPacket4 {
        val requestId = packet.int
        val handleLength = packet.int
        val handle = readUTF8String(packet, handleLength)

        return SftpPacket4(requestId, handle)
    }

    protected open fun readSftpPacket11(packet: ByteBuffer): SftpPacket11 {
        val requestId = packet.int
        val pathLength = packet.int
        val path = readUTF8String(packet, pathLength)

        return SftpPacket11(requestId, path)
    }

    protected open fun readSftpPacket12(packet: ByteBuffer): SftpPacket12 {
        val requestId = packet.int
        val handleLength = packet.int
        val handle = readUTF8String(packet, handleLength)

        return SftpPacket12(requestId, handle)
    }

    protected open fun readSftpPacket16(packet: ByteBuffer): SftpPacket16 {
        val requestId = packet.int
        val originalPathLength = packet.int
        val originalPath = readUTF8String(packet, originalPathLength)
        val controlByte: SftpPacket16.Companion.ControlByte?
        if (packet.hasRemaining()) {
            TODO("Control byte is not yet implemented for SSH_FXP_REALPATH")
            // controlByte = SftpPacket16.Companion.ControlByte.values().find { packet.get() == it.value }
        }
        return SftpPacket16(requestId, originalPath, null, null)
    }

    companion object {
        private const val LENGTH_BYTE_COUNT = 4
        private val BYTE_ORDER = ByteOrder.BIG_ENDIAN

        private fun readUTF8String(buffer: ByteBuffer, length: Int): String {
            val bytes = ByteArray(length)
            for (i in 0 until length) {
                bytes[i] = buffer.get()
            }
            return String(bytes)
        }
    }

}