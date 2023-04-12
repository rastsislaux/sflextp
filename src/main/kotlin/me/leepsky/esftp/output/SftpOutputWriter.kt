package me.leepsky.esftp.output

import me.leepsky.esftp.packet.SftpPacket
import org.apache.sshd.server.command.CommandDirectOutputStreamAware

interface SftpOutputWriter: CommandDirectOutputStreamAware {

    fun write(packet: SftpPacket)

}