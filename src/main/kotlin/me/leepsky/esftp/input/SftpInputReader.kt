package me.leepsky.esftp.input

import me.leepsky.esftp.packet.SftpPacket
import org.apache.sshd.server.command.CommandDirectInputStreamAware

interface SftpInputReader : CommandDirectInputStreamAware {

    /**
     * Read a number of bytes from an input stream and translates it into SftpPacket object
     */
    fun read(): SftpPacket

}