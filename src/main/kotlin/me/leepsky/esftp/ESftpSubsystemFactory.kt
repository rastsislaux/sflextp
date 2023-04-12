package me.leepsky.esftp

import org.apache.sshd.server.channel.ChannelSession
import org.apache.sshd.server.subsystem.SubsystemFactory

class ESftpSubsystemFactory: SubsystemFactory {

    override fun getName() = "sftp"

    override fun createSubsystem(channel: ChannelSession?) = ESftpSubsystem(channel)

}
