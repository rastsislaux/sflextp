package me.leepsky.esftp

import me.leepsky.esftp.input.SftpInputReader
import me.leepsky.esftp.output.SftpOutputWriter
import me.leepsky.esftp.processing.SftpPacketProcessor
import org.apache.sshd.server.channel.ChannelSession
import org.apache.sshd.server.subsystem.SubsystemFactory

class ESftpSubsystemFactory(
    private val readerProvider: () -> SftpInputReader,
    private val outputProvider: () -> SftpOutputWriter,
    private val processorProvider: () -> SftpPacketProcessor
): SubsystemFactory {

    override fun getName() = "sftp"

    override fun createSubsystem(channel: ChannelSession?) = ESftpSubsystem(channel,
                                                                            readerProvider(),
                                                                            outputProvider(),
                                                                            processorProvider())

}
