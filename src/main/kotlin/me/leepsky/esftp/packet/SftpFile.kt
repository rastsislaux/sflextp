package me.leepsky.esftp.packet

import me.leepsky.esftp.FileAttributes

data class SftpFile(
    val filename: String,
    val longname: String,
    val attrs: FileAttributes
)
