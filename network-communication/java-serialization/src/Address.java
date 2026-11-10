/** Deliberately does NOT implement Serializable: required error experiment. */
public final class Address {
    private final String city;
    public Address(String city) { this.city = city; }
    @Override public String toString() { return "Address{city='" + city + "'}"; }
}
