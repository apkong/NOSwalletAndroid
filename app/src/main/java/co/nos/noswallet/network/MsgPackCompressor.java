package co.nos.noswallet.network;

import org.msgpack.MessagePack;
import org.msgpack.type.ArrayValue;
import org.msgpack.type.Value;
import org.msgpack.type.ValueType;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.inject.Inject;

public class MsgPackCompressor {

    private final MessagePack messagePack = new MessagePack();


    private final Charset charset;

    @Inject
    public MsgPackCompressor(Charset charset) {
        this.charset = charset;
    }



    public byte[] compress(String data) {
        try {
            byte[] raw = messagePack.write(data);
            return raw;
        } catch (IOException e) {
            System.err.println("exception ocurred during compression: " + e);
            e.printStackTrace();

            return null;
        }
    }

    public String decompress(byte[] raw) {

        try {
            return messagePack.read(raw, String.class);
        } catch (IOException e) {
            System.err.println("exception ocurred during decompression: " + e);
            e.printStackTrace();
            return null;
        }
    }

    public byte[] decompressBytes(byte[] raw) {

        try {
            Value value = messagePack.read(raw);
            System.out.println("decompressBytes " + value.getType().name());
            if (value.getType() == ValueType.ARRAY) {
                ArrayValue arrayValue = value.asArrayValue();
                for (int i = 0; i < arrayValue.size(); i++) {
                    System.out.println("element: " + arrayValue.get(i));
                }
            }
            return new byte[]{};
        } catch (IOException e) {
            System.err.println("decompressBytes " + e);
            e.printStackTrace();
            return null;
        }
    }
}
