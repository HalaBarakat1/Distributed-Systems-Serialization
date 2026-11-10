
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Aggregator {

    private WebClient webClient;

    public Aggregator() {
        this.webClient = new WebClient();
    }

    /* Send tasks to workers without custom headers */
    public List<String> sendTasksToWorkers(
            List<String> workersAddresses,
            List<String> tasks) {

        return sendTasksToWorkers(workersAddresses, tasks, null);
    }

    /* Send tasks to workers with custom headers */
    public List<String> sendTasksToWorkers(
            List<String> workersAddresses,
            List<String> tasks,
            String headers) {

        if (workersAddresses == null || tasks == null) {
            throw new IllegalArgumentException(
                    "Worker addresses and tasks must not be null"
            );
        }

        if (workersAddresses.size() != tasks.size()) {
            throw new IllegalArgumentException(
                    "The number of workers must equal the number of tasks"
            );
        }

        List<CompletableFuture<String>> futures = new ArrayList<>();

        for (int i = 0; i < tasks.size(); i++) {
            String address = workersAddresses.get(i);
            byte[] payload = tasks.get(i)
                    .getBytes(StandardCharsets.UTF_8);

            CompletableFuture<String> future;

            if (headers == null || headers.isBlank()) {
                future = webClient.sendTask(address, payload);
            } else {
                future = webClient.sendTask(address, payload, headers);
            }

            futures.add(future);
        }

        CompletableFuture<Void> allTasks =
                CompletableFuture.allOf(
                        futures.toArray(new CompletableFuture<?>[0])
                );

        allTasks.join();

        List<String> results = new ArrayList<>();

        for (CompletableFuture<String> future : futures) {
            results.add(future.join());
        }

        return results;
    }
}
