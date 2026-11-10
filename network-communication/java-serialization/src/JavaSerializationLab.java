import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.NotSerializableException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class JavaSerializationLab {
    public static byte[] serialize(Object obj) throws IOException {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(obj);
            output.flush();
            return bytes.toByteArray();
        }
    }

    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bytes = new ByteArrayInputStream(data);
             ObjectInputStream input = new ObjectInputStream(bytes)) {
            return input.readObject();
        }
    }

    public static void main(String[] args) throws Exception {
        CustomerProfile.systemVersion = "v2.4";
        CustomerProfile original = new CustomerProfile(
                "Ahmad Saleh", Arrays.asList("Laptop", "Mouse"),
                new int[]{1200, 25}, true, "SECRET-TOKEN-999");

        if (args.length > 0 && args[0].equalsIgnoreCase("address-error")) {
            original.setAddress(new Address("Damascus"));
            try {
                serialize(original);
                System.out.println("FAIL: Expected NotSerializableException");
                System.exit(1);
            } catch (NotSerializableException expected) {
                System.out.println("EXPECTED ERROR: " + expected.getClass().getName()
                        + ": " + expected.getMessage());
                return;
            }
        }

        System.out.println("BEFORE serialization:\n" + original);
        byte[] payload = serialize(original);
        System.out.println("\nJava Serialization size: " + payload.length + " bytes");

        CustomerProfile.systemVersion = "v3.0-UPDATED";
        CustomerProfile reconstructed = (CustomerProfile) deserialize(payload);
        System.out.println("\nAFTER deserialization:\n" + reconstructed);
        System.out.println("CHECK transient token == null: " + (reconstructed.toString().contains("tempDiscountToken='null'")));
        System.out.println("CHECK static version == updated value: "
                + (CustomerProfile.systemVersion.equals("v3.0-UPDATED")));

        if (args.length > 0 && args[0].equalsIgnoreCase("inspect")) {
            System.out.println("\nPayload as text with platform default charset (as requested in PDF):");
            System.out.println(new String(payload));
            String visible = new String(payload, StandardCharsets.ISO_8859_1);
            System.out.println("\nClass name found in payload: " + visible.contains("CustomerProfile"));
            System.out.println("Field name found in payload: " + visible.contains("customerName"));
            System.out.println("Field name found in payload: " + visible.contains("purchasedItems"));
            System.out.println("SECRET token found in payload: " + visible.contains("SECRET-TOKEN-999"));
        }
    }
}
