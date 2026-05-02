import java.nio.file.*;
import java.io.*;
import java.util.stream.*;
import java.util.zip.*;

public class FindClass {
    public static void main(String[] args) throws Exception {
        Path start = Paths.get(System.getProperty("user.home"), ".gradle", "caches", "neoformruntime");
        try (Stream<Path> stream = Files.walk(start)) {
            stream.filter(p -> p.toString().endsWith(".jar")).forEach(p -> {
                try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(p))) {
                    ZipEntry entry;
                    while ((entry = zis.getNextEntry()) != null) {
                        String name = entry.getName();
                        if (name.endsWith("ResourceLocation.class") || name.endsWith("RenderType.class") || name.endsWith("BakedModel.class") || name.endsWith("ModelData.class") || name.endsWith("RenderHighlightEvent.class")) {
                            System.out.println("Found " + name + " in " + p.getFileName());
                        }
                    }
                } catch (Exception e) {}
            });
        }
    }
}
