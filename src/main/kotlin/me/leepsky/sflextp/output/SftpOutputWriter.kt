package me.leepsky.sflextp.output

import me.leepsky.sflextp.packet.SftpPacket
import org.apache.sshd.server.command.CommandDirectOutputStreamAware

interface SftpOutputWriter: CommandDirectOutputStreamAware {

    fun write(packet: SftpPacket)

}