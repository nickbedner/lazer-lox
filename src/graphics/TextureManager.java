package graphics;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class TextureManager {

  private Map<String, BufferedImage> textureCache = new HashMap<>();
  private final String ASSETS_PATH = "assets/sprites/";

  public BufferedImage getTexture(String fileName) {
    if (textureCache.containsKey(fileName)) {
      return textureCache.get(fileName);
    }

    try {
      File file = new File(ASSETS_PATH + fileName);
      BufferedImage image = ImageIO.read(file);

      textureCache.put(fileName, image);
      System.out.println("[TextureManager] Loaded and cached: " + file.getPath());

      return image;
    } catch (IOException e) {
      System.err.println("[TextureManager] Error loading texture '" + fileName + "': " + e.getMessage());
      return null;
    }
  }

  public void clearCache() {
    textureCache.clear();
    System.out.println("[TextureManager] Cache cleared.");
  }
}