package co.nos.noswallet.network.compression_stuff;

import org.msgpack.MessagePack;
import org.msgpack.type.Value;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class Zlib_testcases {
    static final Charset charset = Charset.forName("UTF-8");
    static MessagePack msgPackCompressor = new MessagePack();

    public static void main(String[] args) {

        String _inputString = "{\"currency\":\"usd\",\"action\":\"get_pow\", \"account\":\"xrb_3bgmpjak8j9c3muqk8u7ctr3qec4wdsdke3rgu958kmzbe4ehbjoihfxgdk9\"}";
        byte[] in = _inputString.getBytes(charset);

        byte[] result = compressWithZlib(in);
        System.out.println("\n***\n");


        for (int i = 0; i < result.length; i++) {
            String s = decimalToHexDecimal(result[i]);
            if (s.length() == 1) {
                s = "0" + s;
            }
            System.out.print(s);
        }
        System.out.println("\nexpected\n");
        System.out.println("\n789c0dc8510e83200c00d0ab2cfdf60f97c12e63a474280da09546b765771f9fef7d0155840abee1097a041860c6b6d6d219a94d5b3d61b8f5c3aaa5f5bcc44fc6c7bca5996d7268b2ee6cf5814dcc4e389ee1084c46a2babbe5fcf134d2e2535d97d715033bf8fd0102c928a0".toUpperCase());
        System.out.println("\n\n" + Arrays.toString(result) + "\n\n");

        System.out.println("\n***\b\nresult : " + new String(decompressWithZlib(result), charset));

    }

    public static byte[] compressWithZlib(byte[] bytes) {
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

    public static byte[] decompressWithZlib(byte[] bytes) {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        InflaterInputStream iis = new InflaterInputStream(bais);

        String result = "";
        byte[] buf = new byte[5];
        int rlen = -1;
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

    public static byte[] compressWithMsgPack(byte[] bytes) {
        try {
            byte[] value = msgPackCompressor.write(bytes);
            return value;
        } catch (IOException e) {
            System.err.println("compressWithMsgPack error " + e);
            e.printStackTrace();
            return new byte[0];
        }
    }

    public static byte[] decompressWithMsgPack(byte[] bytes) {
        System.out.println("decompressWithMsgPack() called with: bytes = [" + Arrays.toString(bytes) + "]");
        try {
            Value value = msgPackCompressor.read(bytes);
            System.out.println("value type: " + value.getType().name());
            return value.asRawValue().getByteArray();
        } catch (IOException e) {
            System.err.println("compressWithMsgPack error " + e);
            e.printStackTrace();
            return new byte[0];
        }
    }

    private static String decimalToHexDecimal(int N) {
        if (N < 0) {
            N = N & 0xff;
        }
        char hexaDecimals[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        StringBuilder builder = new StringBuilder();
        int base = 16;
        while (N != 0) {
            int reminder = N % base;
            builder.append(hexaDecimals[reminder]);
            N = N / base;
        }

        return builder.reverse().toString();
//        final int sizeOfIntInHalfBytes = 8;
//        final int numberOfBitsInAHalfByte = 4;
//        final int halfByte = 0x0F;
//        final char[] hexDigits = {
//                '0', '1', '2', '3', '4', '5', '6', '7',
//                '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
//        };
//        StringBuilder hexBuilder = new StringBuilder(sizeOfIntInHalfBytes);
//        hexBuilder.setLength(sizeOfIntInHalfBytes);
//        for (int i = sizeOfIntInHalfBytes - 1; i >= 0; --i) {
//            int j = N & halfByte;
//            hexBuilder.setCharAt(i, hexDigits[j]);
//            N >>= numberOfBitsInAHalfByte;
//        }
//        return hexBuilder.toString();
//        return Integer.toHexString(N);
    }
}
