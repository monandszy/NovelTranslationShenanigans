package code.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.util.stream.Stream;

public class SomeClass2 {
  public static void main(String[] args) throws IOException {
    Path dirPath = Path.of("C:\\Users\\Name\\Desktop\\BeamNG_Drive");
    try (Stream<Path> walk = Files.walk(dirPath)) {
      walk.forEach(path -> {
          if (Files.isRegularFile(path)) {
            switch (path.getFileName().toString()) {
              case "Read_Me_Important.txt":
              case "Download Full Games for Free.url":
              case "Request any Game.url":
              case "IPCGames.com.jpg":
              case "Help.url":
              case "ipcgames.com.dll":
                try {
                  // Access the DosFileAttributeView for the file
                  DosFileAttributeView attributeView =
                    Files.getFileAttributeView(path, DosFileAttributeView.class);
                  if (attributeView.readAttributes().isReadOnly()) {
                    attributeView.setReadOnly(false);
                  }
                  System.out.println(path);
                  Files.delete(path);
                } catch (IOException ex) {
                  throw new RuntimeException(ex);
                }
            }
          }
        }
      );
    }
  }

}