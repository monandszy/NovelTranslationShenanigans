package code.service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebScraper {

  public static void main(String[] args) {
//    String url = "https://booktoki464.com/novel/7339739?book=%EC%9D%BC%EB%B0%98%EC%86%8C%EC%84%A4";
//    while (true) {
//      url = fetchChapter(url);
//    }
    rename();
  }

  // extract the cloudflare cookie from the browser request manually
  @SneakyThrows
  private static String fetchChapter(String url) {
    Document doc = Jsoup.connect(url)
      .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:122.0) Gecko/20100101 Firefox/122.0")
      .header("Cookie", "e1192aefb64683cc97abb83c71057733=bm92ZWw%3D; PHPSESSID=bqtgg7m4t8sadbin5ml7ds3lal2h8qvn4oh1ochfhl2utrfbs88e432lkert21hoq; cf_clearance=Th4m7pfUvGfZzxgk64vEZ1KcNStI2bpPMSI0mb0yJD0-1729706501-1.2.1.1-bfX9nM4.WgImejExeAAPiZMNzJIts9UbXoWyOvxJnHc8yFCgkfpdDylAMYD9NS6djbU5qr_AfI_gyl0tUxKCJSbEBNuWmHCBR8vg2Rf6phZ5tnHEG2czitjJe0ZolfPZzfySLuZQyBPkPXRekFWi1qqSGZscT6F6.hA2SehSm_kolEdzKjkeQgBD5PZ5ky8hxdgftJfR1zeB6eHME7SIKSOD.9hOaTdcr32Ufy2BsaqjZ_gDjZJo2ftXEuGY3FmqAcFXO9yQm0A9ygPDXoKCmuFJvyWuO3qTq7W1mrINjHWXr.Au5xRrdo6fqi1u5npNWQHU3mOXeid_CJoRDJZI6gLlORXZZMGSb3J89XSuG_3i5ITeRD3JIhXUwCFAT23fQOAg7m.xDAB0xe3cyMCYZA")
      .get();
    Element titleDiv = doc.selectFirst("div.toon-title");
    String title = titleDiv.attr("title");
    System.out.println(title);
    Path path = Path.of("_scraped/%s.txt".formatted(title));

    Element novelContentDiv = doc.getElementById("novel_content");
    Elements paragraphs = novelContentDiv.select("p");
    Files.write(path, paragraphs.stream().map(Element::text).toList(), StandardCharsets.UTF_8);

    Element nextDiv = doc.selectFirst("div.btn-resource.btn-next.at-tip");
    Element link = nextDiv.selectFirst("a");
    return link.attr("abs:href");
  }

  @SneakyThrows
  public static void rename() {
    Path folderPath = Paths.get("_scraped");
    try (Stream<Path> walk = Files.walk(folderPath);) {
      walk
        .filter(Files::isRegularFile)
        .forEach(WebScraper::renameFile);
    }
  }

  @SneakyThrows
  private static void renameFile(Path file) {
    String filename = file.getFileName().toString();
    String newName = extractFirstNumber(filename);
    if (Objects.nonNull(newName)) {
//      if (Integer.parseInt(newName) < 10 or 100) {
//        newName = "0" + newName;
//      }
      Path newFilePath = file.getParent().resolve(newName + getFileExtension(filename));
      Files.move(file, newFilePath, StandardCopyOption.REPLACE_EXISTING);
      System.out.println("Renamed: " + filename + " to " + newName + getFileExtension(filename));
    }
  }

  private static String extractFirstNumber(String filename) {
    Pattern pattern = Pattern.compile("\\d+");
    Matcher matcher = pattern.matcher(filename);
    return matcher.find() ? matcher.group() : null;
  }

  private static String getFileExtension(String filename) {
    int lastIndexOfDot = filename.lastIndexOf('.');
    return lastIndexOfDot == -1 ? "" : filename.substring(lastIndexOfDot);
  }

}