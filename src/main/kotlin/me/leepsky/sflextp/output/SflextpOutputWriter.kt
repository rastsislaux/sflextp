package me.leepsky.sflextp.output

import me.leepsky.sflextp.packet.*
import java.io.BufferedOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer

open class SflextpOutputWriter: SftpOutputWriter {

    private lateinit var out: BufferedOutputStream

    override fun write(packet: SftpPacket) {
        when (packet) {
            is SftpPacket2 -> writePacket(packet)
            is SftpPacket101 -> writePacket(packet)
            is SftpPacket102 -> writePacket(packet)
            is SftpPacket103 -> writePacket(packet)
            is SftpPacket104 -> writePacket(packet)
            is SftpPacket105 -> writePacket(packet)
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
            .putInt(packet.id)
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
            .putInt(packet.id)
            .putInt(handleBytes.size)
            .put(handleBytes)
            .array()

        out.write(bytes)
        out.flush()
    }

    protected open fun writePacket(packet: SftpPacket103) {
        val length = 9 + packet.data.size

        val bytes = ByteBuffer.allocate(length + 4)
            .putInt(length)
            .put(packet.typeId.toByte())
            .putInt(packet.id)
            .putInt(packet.data.size)
            .put(packet.data)
            .array()

        out.write(bytes)
        out.flush()
    }

    protected open fun writePacket(packet: SftpPacket104) {
        var length = 9

        packet.content.forEach {
            length += it.filename.toByteArray().size + it.longname.toByteArray().size + it.attrs.toByteArray().size + 8
        }

        val bytes = ByteBuffer.allocate(length + 4) // Chosen at random
            .putInt(length)
            .put(packet.typeId.toByte())
            .putInt(packet.id)
            .putInt(packet.content.size)

        for (it in packet.content) {
            val filenameBytes = it.filename.toByteArray()
            val longnameBytes = it.longname.toByteArray()
            val attrsBytes = it.attrs.toByteArray()

            bytes
                .putInt(filenameBytes.size)
                .put(filenameBytes)
                .putInt(longnameBytes.size)
                .put(longnameBytes)
                .put(attrsBytes)
        }


        out.write(bytes.array())
        out.flush()
    }

    protected open fun writePacket(packet: SftpPacket105) {
        val attrsBytes = packet.attrs.toByteArray()
        val length = 5 + attrsBytes.size

        val bytes = ByteBuffer.allocate(length + 4)
            .putInt(length)
            .put(packet.typeId.toByte())
            .putInt(packet.id)
            .put(attrsBytes)
            .array()

        out.write(bytes)
        out.flush()
    }

}