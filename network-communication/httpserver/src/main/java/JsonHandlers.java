import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/** Two JSON endpoints added to the existing WebServer without changing /task. */
public final class JsonHandlers {
    private JsonHandlers() {}

    public static void analyze(HttpExchange exchange) throws IOException {
        if (!postOnly(exchange)) return;
        try {
            Map<String, Object> root = requireObject(MiniJson.parse(read(exchange)), "JSON document");
            String name = requireString(root.get("customerName"), "customerName");
            String city = requireString(root.get("city"), "city");
            List<?> orders = requireList(root.get("pastOrders"), "pastOrders");
            for (Object entry : orders) {
                Map<String, Object> order = requireObject(entry, "pastOrders element");
                BigDecimal id = requireNumber(order.get("orderId"), "orderId");
                try { id.intValueExact(); }
                catch (ArithmeticException ex) { throw new DeserializationException("orderId must be an integer"); }
                requireString(order.get("storeBranch"), "storeBranch");
            }
            List<?> items = requireList(root.get("purchasedItems"), "purchasedItems");
            List<?> prices = requireList(root.get("itemPrices"), "itemPrices");
            if (items.size() != prices.size())
                throw new DeserializationException("purchasedItems and itemPrices must have equal length");
            BigDecimal total = BigDecimal.ZERO;
            for (int i = 0; i < items.size(); i++) {
                requireString(items.get(i), "purchasedItems[" + i + "]");
                total = total.add(requireNumber(prices.get(i), "itemPrices[" + i + "]"));
            }
            Object eligible = root.get("isHolidayDealEligible");
            if (!(eligible instanceof Boolean))
                throw new DeserializationException("isHolidayDealEligible must be a boolean");
            String body = "{\"status\":\"ok\",\"customerName\":\"" + escape(name)
                    + "\",\"city\":\"" + escape(city) + "\",\"pastOrderCount\":" + orders.size()
                    + ",\"itemCount\":" + items.size() + ",\"totalSpent\":"
                    + total.stripTrailingZeros().toPlainString() + ",\"isHolidayDealEligible\":" + eligible + "}";
            respond(exchange, 200, body);
        } catch (DeserializationException error) {
            respond(exchange, 400, "{\"error\":\"DeserializationException\",\"detail\":\""
                    + escape(error.getMessage()) + "\"}");
        }
    }

    public static void multiplyPrices(HttpExchange exchange) throws IOException {
        if (!postOnly(exchange)) return;
        try {
            Map<String, Object> root = requireObject(MiniJson.parse(read(exchange)), "JSON document");
            List<?> prices = requireList(root.get("prices"), "prices");
            BigDecimal product = BigDecimal.ONE;
            for (int i = 0; i < prices.size(); i++) {
                Object price = prices.get(i);
                BigDecimal decimal = requireNumber(price, "prices[" + i + "]");
                // This receiver requires exactly two fractional decimal places.
                if (decimal.scale() != 2)
                    throw new DeserializationException("prices[" + i + "] requires exactly 2 decimal places");
                product = product.multiply(decimal);
            }
            respond(exchange, 200, "{\"status\":\"ok\",\"product\":"
                    + product.stripTrailingZeros().toPlainString() + "}");
        } catch (DeserializationException error) {
            respond(exchange, 400, "{\"error\":\"DeserializationException\",\"detail\":\""
                    + escape(error.getMessage()) + "\"}");
        }
    }

    private static String read(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static boolean postOnly(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) return true;
        respond(exchange, 405, "{\"error\":\"Use POST\"}");
        return false;
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var output = exchange.getResponseBody()) { output.write(bytes); }
        exchange.close();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> requireObject(Object value, String field) {
        if (!(value instanceof Map)) throw new DeserializationException(field + " must be a JSON object");
        return (Map<String, Object>) value;
    }

    private static List<?> requireList(Object value, String field) {
        if (!(value instanceof List)) throw new DeserializationException(field + " must be a JSON array");
        return (List<?>) value;
    }

    private static String requireString(Object value, String field) {
        if (!(value instanceof String)) throw new DeserializationException(field + " must be a string");
        return (String) value;
    }

    private static BigDecimal requireNumber(Object value, String field) {
        if (!(value instanceof BigDecimal))
            throw new DeserializationException(field + " must be a JSON number; found "
                    + (value == null ? "null" : value.getClass().getSimpleName()));
        return (BigDecimal) value;
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
