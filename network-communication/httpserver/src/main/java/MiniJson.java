import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A small dependency-free JSON parser for the lab, not a production JSON library. */
public final class MiniJson {
    private final String input;
    private int pos;

    private MiniJson(String input) {
        this.input = input;
    }

    public static Object parse(String input) {
        if (input == null) throw new DeserializationException("JSON body is null");
        MiniJson parser = new MiniJson(input);
        Object result = parser.value();
        parser.whitespace();
        if (parser.pos != input.length()) parser.fail("Trailing characters");
        return result;
    }

    private Object value() {
        whitespace();
        if (pos >= input.length()) { fail("Missing value"); }
        char c = input.charAt(pos);
        if (c == '{') return object();
        if (c == '[') return array();
        if (c == '"') return string();
        if (c == 't') return literal("true", Boolean.TRUE);
        if (c == 'f') return literal("false", Boolean.FALSE);
        if (c == 'n') return literal("null", null);
        if (c == '-' || Character.isDigit(c)) return number();
        fail("Unexpected character '" + c + "'");
        return null;
    }

    private Map<String, Object> object() {
        consume('{');
        Map<String, Object> map = new LinkedHashMap<>();
        whitespace();
        if (take('}')) return map;
        do {
            whitespace();
            if (pos >= input.length() || input.charAt(pos) != '"') fail("Expected string key");
            String key = string();
            whitespace(); consume(':');
            if (map.containsKey(key)) fail("Duplicate object field: " + key);
            map.put(key, value());
            whitespace();
            if (take('}')) return map;
            consume(',');
        } while (true);
    }

    private List<Object> array() {
        consume('[');
        List<Object> list = new ArrayList<>();
        whitespace();
        if (take(']')) return list;
        do {
            list.add(value());
            whitespace();
            if (take(']')) return list;
            consume(',');
        } while (true);
    }

    private String string() {
        consume('"');
        StringBuilder sb = new StringBuilder();
        while (pos < input.length()) {
            char c = input.charAt(pos++);
            if (c == '"') return sb.toString();
            if (c == '\\') {
                if (pos >= input.length()) fail("Unfinished escape sequence");
                char escape = input.charAt(pos++);
                switch (escape) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'u':
                        if (pos + 4 > input.length()) fail("Incomplete unicode escape");
                        try {
                            int unicode = Integer.parseInt(input.substring(pos, pos + 4), 16);
                            sb.append((char) unicode);
                            pos += 4;
                        } catch (NumberFormatException ex) { fail("Invalid unicode escape"); }
                        break;
                    default: fail("Invalid escape sequence");
                }
            } else {
                if (c < 32) fail("Control character in JSON string");
                sb.append(c);
            }
        }
        fail("Unclosed string");
        return null;
    }

    private BigDecimal number() {
        int start = pos;
        take('-');
        if (take('0')) {
            if (pos < input.length() && Character.isDigit(input.charAt(pos))) fail("Leading zero");
        } else {
            if (pos >= input.length() || input.charAt(pos) < '1' || input.charAt(pos) > '9') fail("Bad number");
            while (pos < input.length() && input.charAt(pos) >= '0' && input.charAt(pos) <= '9') pos++;
        }
        if (take('.')) {
            int first = pos;
            while (pos < input.length() && input.charAt(pos) >= '0' && input.charAt(pos) <= '9') pos++;
            if (first == pos) fail("Missing digits after decimal point");
        }
        if (take('e') || take('E')) {
            if (!take('+')) take('-');
            int first = pos;
            while (pos < input.length() && input.charAt(pos) >= '0' && input.charAt(pos) <= '9') pos++;
            if (first == pos) fail("Bad exponent");
        }
        try { return new BigDecimal(input.substring(start, pos)); }
        catch (NumberFormatException ex) { fail("Bad numeric value"); return null; }
    }

    private Object literal(String token, Object result) {
        if (!input.startsWith(token, pos)) fail("Invalid literal");
        pos += token.length();
        return result;
    }

    private void whitespace() {
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (c != ' ' && c != '\n' && c != '\r' && c != '\t') return;
            pos++;
        }
    }

    private void consume(char expected) {
        whitespace();
        if (!take(expected)) fail("Expected '" + expected + "'");
    }

    private boolean take(char c) {
        if (pos < input.length() && input.charAt(pos) == c) { pos++; return true; }
        return false;
    }

    private void fail(String message) {
        throw new DeserializationException(message + " at position " + pos);
    }
}
