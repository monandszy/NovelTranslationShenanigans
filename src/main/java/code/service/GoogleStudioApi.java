package code.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@AllArgsConstructor
@Slf4j
public class GoogleStudioApi {

  private WebClient client;

  public static void main(String[] args) {
    attempt();
  }

  @SneakyThrows
  public static void attempt() {
    String apiKey = "AIzaSyAkgsAk-fpUDvZUG0U0qA1NS5eXQhpzukU";
    String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=" + apiKey;

    try (HttpClient client = HttpClient.newBuilder()
      .version(HttpClient.Version.HTTP_2)
      .connectTimeout(Duration.ofSeconds(10))
      .build();) {

      String jsonInputString = "{\"contents\":[{\"parts\":[{\"text\":\"Explain how AI works\"}]}]}";

      HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(endpoint))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(jsonInputString))
        .build();

      client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        .thenApply(HttpResponse::body)
        .thenAccept(System.out::println)
        .join(); // Wait for completion
    }
  }
}