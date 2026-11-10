/** Local schema demo (no HTTP necessary): compare the JSON payloads. */
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class JsonSchemaTest {
    public static void main(String[] args) throws Exception {
        test("json-tests/prices_as_strings.json");
        test("json-tests/prices_as_decimals.json");
    }
    @SuppressWarnings("unchecked")
    private static void test(String file) throws Exception {
        String json = Files.readString(Path.of("../../../../", file), StandardCharsets.UTF_8).trim();
        System.out.println("Payload: " + file + " = " + json);
        System.out.println("UTF-8 JSON size: " + json.getBytes(StandardCharsets.UTF_8).length + " bytes");
        try {
            Map<String, Object> object = (Map<String, Object>) MiniJson.parse(json);
            List<?> prices = (List<?>) object.get("prices");
            BigDecimal product = BigDecimal.ONE;
            for (Object price : prices) {
                if (!(price instanceof BigDecimal n) || n.scale() != 2)
                    throw new DeserializationException("Receiver requires JSON numbers with exactly 2 decimal places, not strings/integers");
                product = product.multiply(n);
            }
            System.out.println("PASS: " + product.stripTrailingZeros().toPlainString());
        } catch (DeserializationException e) {
            System.out.println("EXPECTED SCHEMA ERROR: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        System.out.println();
    }
}
