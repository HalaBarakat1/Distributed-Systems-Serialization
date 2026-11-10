/** Lab-specific exception used to make a schema/type mismatch explicit. */
public final class DeserializationException extends RuntimeException {
    public DeserializationException(String message) { super(message); }
}
