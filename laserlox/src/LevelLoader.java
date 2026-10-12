import java.io.File;                  // Import the File class
import java.io.FileNotFoundException; // Import this class to handle errors
import java.util.Scanner;             // Import the Scanner class to read text files

public class LevelLoader {
    private String currentLevel;
    private final String levelPath = "src/levels/";
    
    public LevelLoader() {
        this.currentLevel = "Level 1|Efrice|110000101000|000010004400|000000010140|144444444100|00";
    }

    public LevelLoader(String levelName) {
        this.currentLevel = levelName;
    }

    public String GetCurrentLevel() {
        return currentLevel;
    }

    public boolean LoadLevel(LoxNode[][][] loxNodes) {
        try {
            File levelFile = new File(levelPath + "LevelPack1");
            Scanner myReader = new Scanner(levelFile);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                currentLevel = data;
            }
            myReader.close();
            return true;
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred while loading the level.");
            e.printStackTrace();
            return false;
        }
    }
}
