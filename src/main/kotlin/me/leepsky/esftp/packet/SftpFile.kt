package me.leepsky.esftp.packet

data class SftpFile(
    val filename: String,
    val longname: String,
    val attrs: FileAttributes
)
