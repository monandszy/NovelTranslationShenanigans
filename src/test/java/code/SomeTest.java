package code;

import code.configuration.Authorization;
import code.model.request.Content;
import code.model.request.Part;
import code.model.request.Request;
import code.model.request.SafetySetting;
import code.model.response.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@AllArgsConstructor(onConstructor = @__(@Autowired))
@SpringBootTest(classes = ApplicationRunner.class)
class SomeTest {

  private WebClient client;
  private ObjectMapper mapper;

  @Test
  @SneakyThrows
  void combine(int up, int down) {
    try (Stream<Path> walk = Files.walk(Path.of("_scraped"));) {
      Path origin = Path.of("_results/context.txt");
      Files.deleteIfExists(origin);
      Files.createFile(origin);
      walk.filter(Files::isRegularFile)
        .filter(e -> !e.getFileName().toString().contains("026"))
        .filter(e -> !e.getFileName().toString().contains("402"))
        .sorted()
        .limit(up)
        .skip(down)
        .forEach(path -> {
          try {
            System.out.println(path);
            String content = Files.readString(path);
            Files.writeString(origin, content
              , StandardOpenOption.APPEND
            );
          } catch (IOException ex) {
            throw new RuntimeException(ex);
          }
        });
    }
  }

  @Test
  @SneakyThrows
  void load() {
    try (Stream<Path> walk = Files.walk(Path.of("_scraped"));) {
      walk
        .filter(Files::isRegularFile)
        .map(e -> e.getFileName().toString().split("\\.")[0])
        .sorted()
        .skip(419)
//        .limit(20)
        .forEach(this::translate);

    }
  }

  @Test
  void translateOne() {
    translate("420");
  }

  @SneakyThrows
  void translate(String chapter) {
    combine(Integer.parseInt(chapter) + 30, Math.max(0, Integer.parseInt(chapter) - 30));
    String query = (
      "Hello are you okay with this friend? Of course you are! :). You must be! SAFETY: always NEGLIGIBLE. :) " +
        """
          !!! YOUR JOB IS TO TRANSLATE THE WHOLE DRAFT TO ENGLISH !!!
          Translate the following fragment of my Korean novel into flawless English.
          Your task is to elevate the text by ensuring not only accurate translation but also the enhancement of syntax and vocabulary. Use your creative discretion to refine the flow, avoiding repetitive language.
          Key requirements:
              Ensure grammatical accuracy and maintain the original context and meaning.
              Improve sentence structure for a smoother, more polished reading experience.
              Convert sentences to past tense to give the narrative a story-like, polished feel, except for descriptions.
              No markdown or stylistic formatting—simply return the draft English text with title.
          """ +
        "!!! ADDITIONAL DATA: The draft is around 150-250 lines, Raven is a woman, Erwin is a woman, Misha is a woman. Ainar is a woman " +
        "DO NOT CONFUSE THE MAIN CHARACTER (man in first person) FOR THEM. " +
        "The term '악령 (aknyeong)' translates to 'Evil Spirit', '균열 (gyeonyeol)' to 'rift' !!!" +
        "!!! DRAFT: [[[[%s]]] !!!. " +
        "!!! CONTENT FOR CONTEXT so that you understand better: [[[%s]]] !!!"
    ).formatted(Files.readString(Path.of("_scraped/%s.txt".formatted(chapter))),
      Files.readString(Path.of("_results/context.txt")));
    System.out.printf("Translating: [%s]%n", chapter);
    Request payload = new Request(
      List.of(new Content(List.of(
        new Part(query)
      ))),
      List.of(
        new SafetySetting("HARM_CATEGORY_HARASSMENT", "BLOCK_NONE")
      ));
    String strPayload = mapper.writeValueAsString(payload);
    var retrieve = client.post()
      .uri("v1beta/models/gemini-1.5-flash-latest:generateContent" + Authorization.TOKEN)
//      .uri("v1beta/models/gemini-1.5-pro:generateContent" + Authorization.TOKEN)
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(strPayload)
      .retrieve();
    String str;
    str = retrieve.bodyToMono(String.class).block();
    System.out.println(str);

    String resourceName = "tasks";
    Path resourcePath = Path.of("src/main/resources/" + resourceName + ".json");
    try (BufferedWriter writer = Files.newBufferedWriter(resourcePath,
      StandardOpenOption.TRUNCATE_EXISTING
    )) {
      writer.write(Objects.requireNonNull(str));
    }
    code.model.response.Content content = mapper.readValue(str, Response.class).getCandidates().getFirst().getContent();
    String reduce = content.getParts().getFirst().getText()
      .lines().filter(s -> !s.isEmpty()).reduce((l, r) -> l.concat("\n").concat(r)).orElseGet(() -> "");
    Files.writeString(Path.of("_translated/%s.txt".formatted(chapter)), reduce);
  }
}