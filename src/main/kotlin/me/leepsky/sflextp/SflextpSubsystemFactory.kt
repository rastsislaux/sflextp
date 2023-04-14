package me.leepsky.sflextp

import me.leepsky.sflextp.input.SftpInputReader
import me.leepsky.sflextp.output.SftpOutputWriter
import me.leepsky.sflextp.processing.SftpPacketProcessor
import org.apache.sshd.server.channel.ChannelSession
import org.apache.sshd.server.subsystem.SubsystemFactory

class SflextpSubsystemFactory(
    private val readerProvider: () -> SftpInputReader,
    private val outputProvider: () -> SftpOutputWriter,
    private val processorProvider: () -> SftpPacketProcessor
): SubsystemFactory {

    override fun getName() = "sftp"

    override fun createSubsystem(channel: ChannelSession?) = SflextpSubsystem(channel,
                                                                            readerProvider(),
                                                                            outputProvider(),
                                                                            processorProvider())

}
