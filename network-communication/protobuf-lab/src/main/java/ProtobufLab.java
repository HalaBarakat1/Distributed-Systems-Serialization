import com.distributed.lab.CustomerProfileProto;
import com.distributed.lab.RenamedCustomerProfileProto;
import com.distributed.lab.CustomerProfileProto.CustomerProfile;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** Generated message classes live in src/main/java/com/distributed/lab. */
public final class ProtobufLab {
    public static void main(String[] args) throws Exception {
        CustomerProfile profile = CustomerProfile.newBuilder()
                .setCustomerName("Ahmad Saleh")
                .addAllPurchasedItems(Arrays.asList("Laptop", "Mouse"))
                .addAllItemPrices(Arrays.asList(1200, 25))
                .setIsHolidayDealEligible(true)
                .build();

        byte[] payload = profile.toByteArray();
        System.out.println("Protobuf size: " + payload.length + " bytes");
        CustomerProfile reconstructed = CustomerProfile.parseFrom(payload);
        System.out.println("Recovered customer: " + reconstructed.getCustomerName());
        System.out.println("Recovered city (default): " + reconstructed.getCity());
        System.out.println("Was city sent? " + reconstructed.hasCity());
        System.out.println("Purchased items: " + reconstructed.getPurchasedItemsList());
        System.out.println("Prices: " + reconstructed.getItemPricesList());
        System.out.println("Eligible: " + reconstructed.getIsHolidayDealEligible());

        if (args.length > 0 && args[0].equalsIgnoreCase("evolution")) {
            // Sender stays unchanged; only receiver uses renamed generated schema.
            RenamedCustomerProfileProto.CustomerProfile receiver =
                    RenamedCustomerProfileProto.CustomerProfile.parseFrom(payload);
            System.out.println("\nReceiver with RENAMED field client_full_name = 1:");
            System.out.println("Recovered client_full_name: " + receiver.getClientFullName());
            System.out.println("Recovered items: " + receiver.getPurchasedItemsList());
            System.out.println("Schema evolution PASS: "
                    + receiver.getClientFullName().equals("Ahmad Saleh"));
        }
    }
}
