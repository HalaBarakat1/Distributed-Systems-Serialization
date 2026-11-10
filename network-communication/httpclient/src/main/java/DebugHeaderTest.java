
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DebugHeaderTest {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/task"))
                .header("X-Debug", "true")
                .POST(HttpRequest.BodyPublishers.ofString("10,200"))
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        System.out.println("Response body:");
        System.out.println(response.body());

        System.out.println("X-Debug-Info:");
        System.out.println(
                response.headers()
                        .firstValue("X-Debug-Info")
                        .orElse("Header not found")
        );
    }
}
