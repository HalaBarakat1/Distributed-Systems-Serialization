import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class CustomerProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    public static String systemVersion = "v2.4"; // static: not part of object state
    private transient String tempDiscountToken;   // transient: not serialized

    private String customerName;
    private List<String> purchasedItems;
    private int[] itemPrices;
    private boolean isHolidayDealEligible;

    // Used only in the non-Serializable Address experiment. Normally null.
    private Address address;

    public CustomerProfile() { }

    public CustomerProfile(String customerName, List<String> purchasedItems,
                           int[] itemPrices, boolean isHolidayDealEligible, String token) {
        this.customerName = customerName;
        this.purchasedItems = purchasedItems;
        this.itemPrices = itemPrices;
        this.isHolidayDealEligible = isHolidayDealEligible;
        this.tempDiscountToken = token;
    }

    public void setAddress(Address address) { this.address = address; }

    @Override public String toString() {
        return "CustomerProfile{" +
                "systemVersion='" + systemVersion + '\'' +
                ", tempDiscountToken='" + tempDiscountToken + '\'' +
                ", customerName='" + customerName + '\'' +
                ", purchasedItems=" + purchasedItems +
                ", itemPrices=" + Arrays.toString(itemPrices) +
                ", isHolidayDealEligible=" + isHolidayDealEligible +
                ", address=" + address + '}';
    }
}
