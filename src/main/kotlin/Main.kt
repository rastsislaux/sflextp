import me.leepsky.sflextp.SflextpSubsystemFactory
import me.leepsky.sflextp.input.SflextpInputReader
import me.leepsky.sflextp.output.SflextpOutputWriter
import me.leepsky.sflextp.processing.SflextpPacketProcessor
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import java.io.File

fun main() {
    val sshd = SshServer.setUpDefaultServer()
    sshd.port = 2000
    sshd.keyPairProvider = SimpleGeneratorHostKeyProvider(File("hosts.ser").toPath())
    sshd.subsystemFactories = listOf(SflextpSubsystemFactory({ SflextpInputReader() },
                                                             { SflextpOutputWriter() },
                                                             { SflextpPacketProcessor() }))
    sshd.setPasswordAuthenticator { _, _, _ -> true}
    sshd.start()

    while (true) { }
}