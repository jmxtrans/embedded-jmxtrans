/*
 * Copyright (c) 2010-2015 the original author or authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
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
