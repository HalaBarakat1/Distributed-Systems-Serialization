import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Offline verification of the protobuf wire format, WITHOUT external JARs.
 * This is NOT Google's protoc-generated stub. For the requested official
 * Builder/parseFrom experiment, run ProtobufLab with Maven instead.
 */
public final class ProtobufWireLab {
    private static void varint(ByteArrayOutputStream out, int number) {
        long value = Integer.toUnsignedLong(number);
        while ((value & ~0x7fL) != 0) {
            out.write(((int)value & 0x7f) | 0x80);
            value >>>= 7;
        }
        out.write((int)value);
    }
    private static void text(ByteArrayOutputStream out, int tag, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        varint(out, (tag << 3) | 2);
        varint(out, bytes.length);
        out.writeBytes(bytes);
    }
    private static void integer(ByteArrayOutputStream out, int tag, int value) {
        varint(out, tag << 3); varint(out, value);
    }
    public static byte[] encode() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        text(out, 1, "Ahmad Saleh");
        // Tag 2 (city) is optional, so deliberately omitted.
        text(out, 3, "Laptop"); text(out, 3, "Mouse");
        integer(out, 4, 1200); integer(out, 4, 25);
        integer(out, 5, 1);
        return out.toByteArray();
    }
    private static long readVarint(byte[] src, int[] pos) throws IOException {
        long value=0;
        for (int shift=0; shift<64; shift+=7) {
            if(pos[0]>=src.length) throw new IOException("Truncated varint");
            int b=src[pos[0]++]&0xff;
            value |= (long)(b & 0x7f) << shift;
            if ((b & 0x80)==0) return value;
        }
        throw new IOException("Varint too long");
    }
    public static void main(String[] args) throws Exception {
        byte[] encoded=encode();
        System.out.println("Offline protobuf wire-format payload: " + encoded.length + " bytes");
        System.out.println("Hex bytes: " + java.util.HexFormat.of().formatHex(encoded));
        int[] p={0}; String customer=null; String city="Damascus";
        List<String> items=new ArrayList<>(); List<Long> prices=new ArrayList<>();
        boolean eligible=false;
        while(p[0]<encoded.length) {
            int key=(int)readVarint(encoded,p);
            int tag=key>>>3, wire=key&7;
            if (wire==2) {
                int n=(int)readVarint(encoded,p);
                if(n<0 || n>encoded.length-p[0]) throw new IOException("Bad length");
                String val=new String(encoded,p[0],n,StandardCharsets.UTF_8);
                p[0]+=n;
                if (tag==1) customer=val; // Renamed receiver can call it client_full_name.
                else if(tag==2) city=val;
                else if(tag==3) items.add(val);
            } else if(wire==0) {
                long val=readVarint(encoded,p);
                if(tag==4) prices.add(val);
                else if(tag==5) eligible=val!=0;
            } else throw new IOException("Unexpected wire type: " + wire);
        }
        System.out.println("Recovered customer_name (tag 1): " + customer);
        System.out.println("Recovered client_full_name (renamed tag 1): " + customer);
        System.out.println("Recovered city (default): " + city);
        System.out.println("Purchased items: " + items);
        System.out.println("Prices: " + prices);
        System.out.println("Eligible: " + eligible);
        if (!"Ahmad Saleh".equals(customer) || items.size()!=2 || prices.size()!=2)
            throw new AssertionError("Wire roundtrip failed");
        System.out.println("PASS: tag-based decoding works with renamed field");
    }
}
