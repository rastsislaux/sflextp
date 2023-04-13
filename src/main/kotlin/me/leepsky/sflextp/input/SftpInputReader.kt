package me.leepsky.sflextp.input

import me.leepsky.sflextp.packet.SftpPacket
import org.apache.sshd.server.command.CommandDirectInputStreamAware

interface SftpInputReader : CommandDirectInputStreamAware {

    /**
     * Read a number of bytes from an input stream and translates it into SftpPacket object
     */
    fun read(): SftpPacket

}