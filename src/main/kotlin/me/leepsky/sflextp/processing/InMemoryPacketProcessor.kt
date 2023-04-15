package me.leepsky.sflextp.processing

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import me.leepsky.sflextp.packet.*

class InMemoryPacketProcessor: FilesystemPacketProcessor() {

    private val fs = Jimfs.newFileSystem(Configuration.unix())

    override fun process(packet: SftpPacket3): SftpPacket {
        val path = fs.getPath(packet.filename)
        return process(packet, path)
    }

    override fun process(packet: SftpPacket11): SftpPacket {
        val path = fs.getPath(packet.path)
        return process(packet, path)
    }

    override fun process(packet: SftpPacket13): SftpPacket {
        val path = fs.getPath(packet.filename)
        return process(packet, path)
    }

    override fun process(packet: SftpPacket14): SftpPacket {
        val path = fs.getPath(packet.path)
        return process(packet, path)
    }

    override fun process(packet: SftpPacket16): SftpPacket {
        val path = fs.getPath(packet.path)
        return process(packet, path)
    }

    override fun process(packet: SftpPacket18): SftpPacket {
        val oldPath = fs.getPath(packet.oldPath)
        val newPath = fs.getPath(packet.newPath)
        return process(packet, oldPath, newPath)
    }

}