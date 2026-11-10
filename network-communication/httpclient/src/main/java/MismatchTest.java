import java.util.Arrays;
import java.util.List;

public class MismatchTest {
    public static void main(String[] args) {
        Aggregator aggregator = new Aggregator();

        List<String> addresses = Arrays.asList(
                "http://localhost:8080/task",
                "http://localhost:8081/task"
        );

        List<String> tasks = Arrays.asList(
                "10,200",
                "2,3,4",
                "5,6"
        );

        try {
            aggregator.sendTasksToWorkers(addresses, tasks);
            System.out.println("FAIL: Expected an exception.");
        } catch (IllegalArgumentException e) {
            System.out.println("PASS: Mismatch was detected.");
            System.out.println(e.getMessage());
        }
    }
}