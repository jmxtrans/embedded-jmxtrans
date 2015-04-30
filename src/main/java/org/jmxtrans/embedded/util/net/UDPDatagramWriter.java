package org.jmxtrans.embedded.util.net;

import java.io.IOException;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.Charset;

/**
 * Convenience class for writing characters to a UDP {@linkplain java.nio.channels.DatagramChannel}.
 *
 * @author <a href="mailto:patrick.bruehlmann@gmail.com">Patrick Brühlmann</a>
 */
public class UDPDatagramWriter extends Writer {


    private final DatagramSocket datagramSocket;
    private final Charset charset;

    public UDPDatagramWriter(DatagramSocket datagramSocket, Charset charset) {
        this.datagramSocket = datagramSocket;
        this.charset = charset;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {

        DatagramPacket datagramPacket = new DatagramPacket(new String(cbuf).getBytes(charset), off, len);
        datagramSocket.send(datagramPacket);
    }

    @Override
    public void flush() throws IOException {
        // nothing to do here
    }

    @Override
    public void close() throws IOException {
        // nothing to do here
    }
}
