package me.leepsky.esftp.output

import me.leepsky.esftp.packet.*
import java.io.BufferedOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer

open class DefaultSftpOutputWriter: SftpOutputWriter {

    private lateinit var out: BufferedOutputStream

    override fun write(packet: SftpPacket) {
        when (packet) {
            is SftpPacket2 -> writePacket(packet)
            is SftpPacket101 -> writePacket(packet)
            is SftpPacket102 -> writePacket(packet)
            is SftpPacket104 -> writePacket(packet)
            else -> TODO("Writing this packet is not yet supported.")
        }
    }

    override fun setOutputStream(out: OutputStream) {
        this.out = out.buffered()
    }

    protected open fun writePacket(packet: SftpPacket2) {
        val bytes = ByteBuffer.allocate(9)
            .putInt(5) // Length of response
            .put(packet.typeId.toByte())
            .putInt(packet.version)
            .array()
        out.write(bytes)
        out.flush()
    }

    protected open fun writePacket(packet: SftpPacket101) {
        var length = 9 // Length of Type ID + Request ID + Status Code
        val errorMessageBytes = packet.errorMessage.toByteArray()
        length += errorMessageBytes.size + 4

        val languageTagBytes = packet.languageTag.toLanguageTag().toByteArray()
        length += languageTagBytes.size + 4

        val bytes = ByteBuffer.allocate(length + 4) // Length + 4 bytes of length itself
            .putInt(length)
            .put(packet.typeId.toByte())
            .putInt(packet.requestId)
            .putInt(packet.statusCode.value)
            .putInt(errorMessageBytes.size)
            .put(errorMessageBytes)
            .putInt(languageTagBytes.size)
            .put(languageTagBytes)
            .array()

        out.write(bytes)
        out.flush()
    }

    protected open fun writePacket(packet: SftpPacket102) {
        val handleBytes = packet.handle.toByteArray()

        val bytes = ByteBuffer.allocate(13 + handleBytes.size) // Length of Type ID + Request ID + Handle length
            .putInt(9 + handleBytes.size)
            .put(packet.typeId.toByte())
            .putInt(packet.requestId)
            .putInt(handleBytes.size)
            .put(handleBytes)
            .array()

        out.write(bytes)
        out.flush()
    }

    protected open fun writePacket(packet: SftpPacket104) {
        val bytes = ByteBuffer.allocate(1024) // Chosen at random
            .putInt(0) // Length is unknown at this point
            .put(packet.typeId.toByte())
            .putInt(packet.requestId)
            .putInt(packet.content.size)

        var length = 9 // Length of Type ID + Request ID + Content Size
        for (it in packet.content) {
            val pathBytes = it.key.toByteArray()
            val attrsBytes = it.value.toByteArray()
            length += pathBytes.size + attrsBytes.size + 4 // 4 is the size of string length
            bytes
                .putInt(pathBytes.size)
                .put(pathBytes)
                .put(attrsBytes)
            break
        }

        packet.endOfList?.let {
            length += 1
            bytes.put(if (it) 1 else 0)
        }

        bytes.putInt(0, length)

        out.write(bytes.array().copyOfRange(0, length + 4))
        out.flush()
    }

}