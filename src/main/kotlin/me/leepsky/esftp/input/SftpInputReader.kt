package me.leepsky.esftp.input

import me.leepsky.esftp.packet.SftpPacket
import org.apache.sshd.server.command.CommandDirectInputStreamAware

interface SftpInputReader : CommandDirectInputStreamAware {

    fun read(): SftpPacket

}