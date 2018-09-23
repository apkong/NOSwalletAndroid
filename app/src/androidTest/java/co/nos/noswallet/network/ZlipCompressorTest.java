package co.nos.noswallet.network;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.Charset;

//todo: fix tests
public class ZlipCompressorTest {

    public static void main(String[] args) {
        final Charset charset = Charset.forName("UTF-8");

        ZlipCompressor compressor = new ZlipCompressor(charset);

        byte[] result = compressor.compress("{\"currency\":\"usd\",\"action\":\"get_pow\", \"account\":\"xrb_3bgmpjak8j9c3muqk8u7ctr3qec4wdsdke3rgu958kmzbe4ehbjoihfxgdk9\"}".getBytes(charset));


        byte[] expected = new byte[]{120, -100, 13, -56, 81, 14, -125, 32, 12, 0, -48, -85, 44,
                -3, -10, 15, -105, -63, 46, 99, -92, 116, 40, 13, -96, -107, 70, -73, 101,
                119, 31, -97, -17, 125, 1, 85, -124, 10, -66, -31, 9, 122, 4, 24,
                96, -58, -74, -42, -46, 25, -87, 77, 91, 61, 97, -72,
                -11, -61, -86, -91, -11, -68, -60, 79, -58, -57, -68,
                -91, -103, 109, 114, 104, -78, -18, 108, -11, -127,
                77, -52, 78, 56, -98, -31, 8, 76, 70, -94, -70, -69,
                -27, -4, -15, 52, -46, -30, 83, 93, -105, -41, 21, 3, 59, -8, -3, 1, 2, -55, 40, -96};

        Assert.assertArrayEquals(expected, result);
    }


}