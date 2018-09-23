package co.nos.noswallet.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import javax.inject.Inject;

public class ZlipCompressor {

    private final Charset charset;

    @Inject
    public ZlipCompressor(Charset charset) {
        this.charset = charset;
    }

    public byte[] compress(byte[] bytes) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream dos = new DeflaterOutputStream(baos);
        try {
            dos.write(bytes);
            dos.flush();
            dos.close();
            byte[] zlibResult = baos.toByteArray();
            return zlibResult;
        } catch (IOException x) {
            System.err.println(x);
            x.printStackTrace();
            return new byte[0];
        }
    }

    public byte[] decompress(byte[] bytes) {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        InflaterInputStream iis = new InflaterInputStream(bais);

        String result = "";
        byte[] buf = new byte[5];
        int rlen;
        try {
            while ((rlen = iis.read(buf)) != -1) {
                result += new String(Arrays.copyOf(buf, rlen));
            }
        } catch (IOException x) {
            System.err.println(x);
            x.printStackTrace();
            return new byte[0];
        }
        return result.getBytes(charset);
    }
}
