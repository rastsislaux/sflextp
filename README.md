# SFlexTP Subsystem for Apache Mina
## Overview
*SFlexTP* is a simple and extensible SFTP subsystem for Apache Mina. It provides a way to handle SFTP requests and responses in a flexible way, by allowing users to customize the input reader, output writer, and packet processor used by the subsystem.

## Getting Started
To use SFlexTP, you need to add the following dependency to your project:

Maven:
```xml
<dependency>
    <groupId>me.leepsky</groupId>
    <artifactId>sflextp</artifactId>
    <version>1.0.0</version>
</dependency>
```
Gradle:
```groovy
implementation("me.leepsky:sflextp:1.0.0")
```

Once you have added the dependency, you can create an SFTP server and add the sflextp subsystem to it like this:

```kotlin
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
```
Or, if you prefer Java:
```java
public class Main {
    public static void main(String[] args) throws IOException {
        SshServer sshd = SshServer.setUpDefaultServer();
        sshd.setPort(2000);
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File("hosts.ser").toPath()));
        sshd.setSubsystemFactories(
                List.of(new SflextpSubsystemFactory(SflextpInputReader::new,
                                                    SflextpOutputWriter::new,
                                                    SflextpPacketProcessor::new)));
        sshd.setPasswordAuthenticator((username, password, session) -> true);
        sshd.start();

        while (true) { }
    }
}
```
This code creates an SFTP server on port 2000 and adds the SFlexTP subsystem to it. It uses the default input reader, output writer, and packet processor, but you can customize these by passing your own instances to the SflextpSubsystemFactory constructor.

## Packet processors

### InMemoryPacketProcessor

This implementation of the SftpPacketProcessor interface processes SFTP packets in memory. It does not store the data to any external file system or storage. This implementation is suitable for scenarios where the SFTP data needs to be processed in-memory, and the data is not required to be stored persistently. It uses Google's JimFS internally.

```kotlin
ssh.subsystemFactories = listOf(SflextpSubsystemFactory({ SflextpInputReader() },
                                                        { SflextpOutputWriter() },
                                                        { InMemoryPacketProcessor() }))
```

### FilesystemPacketProcessor

This implementation of the SftpPacketProcessor interface processes SFTP packets by storing and retrieving data to and from a file system. This implementation is suitable for scenarios where the SFTP data needs to be stored persistently and accessed later.

```kotlin
ssh.subsystemFactories = listOf(SflextpSubsystemFactory({ SflextpInputReader() },
                                                        { SflextpOutputWriter() },
                                                        { FilesystemPacketProcessor() }))
```

## Customization
To customize the SFlexTP subsystem, you can implement your own input reader, output writer, or packet processor, and pass them to the SflextpSubsystemFactory constructor. For example, here's how you could create a custom packet processor:

```kotlin
class MyPacketProcessor: SflextpPacketProcessor() {

    override fun process(packet: SftpPacket1): SftpPacket2 {
        println("Overriden method!")
        return super.process(packet)
    }

}
```
You could then use this packet processor in your SFTP server like this:

```kotlin
val sshd = SshServer.setUpDefaultServer()
sshd.port = 2000
sshd.keyPairProvider = SimpleGeneratorHostKeyProvider(File("hosts.ser").toPath())
sshd.subsystemFactories = listOf(SflextpSubsystemFactory({ SflextpInputReader() },
                                                         { SflextpOutputWriter() },
                                                         { MyPacketProcessor() }))
sshd.setPasswordAuthenticator { _, _, _ -> true}
sshd.start()

while (true) { }
```

## License
Sflextp is released under the MIT License. See the LICENSE file for more information.

## Contributing
We welcome contributions to SFlexTP! If you would like to contribute code, documentation, or anything else, please submit a pull request on GitHub.
