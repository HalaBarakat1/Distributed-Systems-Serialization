
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class WebClient {

    private HttpClient client;

    /* Instantiate web client */
    public WebClient() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    /* Send task asynchronously without custom headers */
    public CompletableFuture<String> sendTask(
            String url, byte[] requestPayload) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestPayload))
                .build();

        return client.sendAsync(
                request,
                HttpResponse.BodyHandlers.ofString()
        ).thenApply(HttpResponse::body);
    }

    /* Send task asynchronously with custom headers */
    public CompletableFuture<String> sendTask(
            String url, byte[] requestPayload, String headers) {

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestPayload));

        if (headers != null && !headers.isBlank()) {
            int separator = headers.indexOf(':');

            if (separator <= 0) {
                throw new IllegalArgumentException(
                        "Invalid header format. Expected Name: Value"
                );
            }

            String headerName = headers.substring(0, separator).trim();
            String headerValue = headers.substring(separator + 1).trim();

            builder.header(headerName, headerValue);
        }

        HttpRequest request = builder.build();

        return client.sendAsync(
                request,
                HttpResponse.BodyHandlers.ofString()
        ).thenApply(HttpResponse::body);
    }
}
