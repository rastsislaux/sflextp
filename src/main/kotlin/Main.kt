import me.leepsky.esftp.ESftpSubsystemFactory
import me.leepsky.esftp.input.ESftpInputReader
import me.leepsky.esftp.output.ESftpOutputWriter
import me.leepsky.esftp.processing.ESftpPacketProcessor
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import java.io.File

fun main() {
    val sshd = SshServer.setUpDefaultServer()
    sshd.port = 2000
    sshd.keyPairProvider = SimpleGeneratorHostKeyProvider(File("hosts.ser").toPath())
    sshd.subsystemFactories = listOf(ESftpSubsystemFactory({ ESftpInputReader() },
                                                           { ESftpOutputWriter() },
                                                           { ESftpPacketProcessor() }))
    sshd.setPasswordAuthenticator { _, _, _ -> true}
    sshd.start()

    while (true) { }
}