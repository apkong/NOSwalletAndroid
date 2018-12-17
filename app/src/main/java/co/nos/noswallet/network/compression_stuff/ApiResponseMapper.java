package co.nos.noswallet.network.compression_stuff;

import java.nio.charset.Charset;
import java.util.Arrays;

import javax.inject.Inject;

public class ApiResponseMapper {

    private final MsgPackCompressor msgPackCompressor;
    private final ZlibCompressor zlipCompressor;
    private final Charset charset = Charset.forName("UTF-8");

    @Inject
    public ApiResponseMapper(MsgPackCompressor msgPackCompressor,
                             ZlibCompressor zlipCompressor) {
        this.msgPackCompressor = msgPackCompressor;
        this.zlipCompressor = zlipCompressor;
    }

    public byte[] serialize(String in) {
        System.out.println("ApiResponseMapper :: serialize called with input: [" + in + "]");
        return zlipCompressor.compress(msgPackCompressor.compress(in));
    }

    public byte[] deserialize(byte[] in) {
        String s = msgPackCompressor.decompress(zlipCompressor.decompress(in));
        return s.getBytes();
    }

//    message = '{"currency":"usd","action":"get_pow", "account":"xrb_3bgmpjak8j9c3muqk8u7ctr3qec4wdsdke3rgu958kmzbe4ehbjoihfxgdk9"}'

//    a = zlib.compress(msgpack.packb(message))
//    print(a.hex())

    public static void main(String[] args) {
        Charset charset = Charset.forName("utf-8");

        ApiResponseMapper mapper = new ApiResponseMapper(new MsgPackCompressor(charset), new ZlibCompressor(charset));

        String request = "{\"currency\":\"usd\",\"action\":\"get_pow\", \"account\":\"xrb_3bgmpjak8j9c3muqk8u7ctr3qec4wdsdke3rgu958kmzbe4ehbjoihfxgdk9\"}";

        byte[] result = mapper.serialize(request);

        System.out.println("\ncompressed [" + Arrays.toString(result) + "]\n");

        for (int i = 0; i < result.length; i++) {
            System.out.print(decimalToHexDecimal(result[i]));
        }
//        System.out.println("\ndecompressing takes place...");
//
//        System.out.println(Arrays.toString(mapper.deserialize(result)));
    }

    public String toHexString(byte[] result) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < result.length; i++) {
            stringBuilder.append(decimalToHexDecimal(result[i]));
        }
        return stringBuilder.toString();
    }

    public byte[] toUnsignedResult(byte[] result) {
        byte[] data = new byte[result.length];
        for (int i = 0; i < result.length; i++) {
            int value = result[i];

            data[i] = result[i];

            if (value < 0) {
                data[i] += 256;
            }
        }
        return data;
    }

    private static String decimalToHexDecimal(int N) {
        if (N < 0) {
            N = 256 + N;
        }
        final boolean shouldIncludeZero = N < 16;
        final boolean isZero = N == 0;
        if (isZero) return "00";

        char hexaDecimals[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        StringBuilder builder = new StringBuilder();
        int base = 16;
        while (N != 0) {
            int reminder = N % base;
            builder.append(hexaDecimals[reminder]);
            N = N / base;
        }
        String result = builder.reverse().toString();
        if (shouldIncludeZero) {
            return "0" + result;
        }
        return result;
    }
}
