package me.leepsky.esftp.processing

import me.leepsky.esftp.packet.SftpPacket

interface SftpPacketProcessor {

    fun process(packet: SftpPacket): SftpPacket

}