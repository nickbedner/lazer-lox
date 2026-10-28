import java.io.File;                  // Import the File class
import java.io.FileNotFoundException; // Import this class to handle errors
import java.util.Scanner;             // Import the Scanner class to read text files

public class LevelLoader {
    private String currentLevel;
    private final String levelPath = "lvlfiles\\";
    
    public LevelLoader() {
        this.currentLevel = "Level 1|Efrice|110000101000|000010004400|000000010140|144444444100|00";
    }

    public LevelLoader(String levelName) {
        this.currentLevel = levelName;
    }

    public String GetCurrentLevel() {
        return currentLevel;
    }

    public boolean LoadLevel(int levelNumber) {
        if (levelNumber < 0 || levelNumber > 99) {
            throw new IllegalArgumentException("Level number must be between 0 and 99. Level Packs are limited to 100 levels each.");
        }
        levelNumber++; // Increment level number to match the line number in the file (1-indexed)
        try {
            File levelFile = new File(levelPath + "LevelPack1.txt");
            Scanner myReader = new Scanner(levelFile);
            while (myReader.hasNextLine() && levelNumber > 1) {
                myReader.nextLine();
                levelNumber--;
            }
            String tmpLevel = myReader.nextLine();
            myReader.close();
            if (tmpLevel != null && !tmpLevel.isEmpty()) {
                System.out.println("Found Level: " + tmpLevel);
                currentLevel = tmpLevel;
                return true;
            }
            System.out.println("Attempted to find a level that does not exist.");
            return false;
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred while loading the level.");
            e.printStackTrace();
            return false;
        }
    }
}
