package me.leepsky.sflextp.processing

import me.leepsky.sflextp.packet.SftpPacket

interface SftpPacketProcessor {

    fun process(packet: SftpPacket): SftpPacket

}