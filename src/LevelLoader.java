import java.io.File;                  // Import the File class
import java.io.FileNotFoundException; // Import this class to handle errors
import java.util.Scanner;             // Import the Scanner class to read text files

public class LevelLoader {
    //Level as a string
    private String currentLevel;
    
    // Level Win Check Info
    private int tunnelCount = 0;
    private boolean megaLaser = false;
    private boolean portalPuzzle = false;
    private boolean togglePuzzle = false;
    private boolean carryPuzzle = false;

    //Prebuilt level datapath
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
            String tmpLevel = "";

            if (!myReader.hasNextLine()) {
                System.out.println("Level number exceeds the number of levels in the file.");
                tmpLevel = "\"Level 1|Efrice|110000101000|000010004400|000000010140|144444444100|00\"";
            }
            else {
                tmpLevel = myReader.nextLine();
            }
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

    public boolean[] SetupGoals() {
        String goalParts = currentLevel.split("\\|")[2];
        String toggleNcarry = currentLevel.split("\\|")[6];
        togglePuzzle = false;
        carryPuzzle = false;

        if (toggleNcarry.charAt(0) == '1') {
            togglePuzzle = true;
        }

        if (toggleNcarry.charAt(1) == '1') {
            carryPuzzle = true;
        }

        //All levels have 12 positions for goals
        boolean goalRing[] = new boolean[12];

        for (int i = 0; i < 12; i++) {
            if (i < goalParts.length() && goalParts.charAt(i) == '1') {
                goalRing[i] = true;
            } else
                goalRing[i] = false;
        }
        
        return goalRing;
    }

    public LoxNode[][] SetupNodes() {
        String[] levelParts = currentLevel.split("\\|");

        //All levels are three rings with 12 nodes.
        LoxNode[][] rings = new LoxNode[3][12];

        for (int ring = 0; ring < 3; ring++) {
            for (int i = 0; i < 12; i++) {
                if (i >= levelParts[ring + 3].length() || levelParts[ring + 3].charAt(i) == '0') {
                    rings[ring][i] = null;
                } else {
                    char nodeType = levelParts[ring + 3].charAt(i);
                    switch (nodeType) {
                        // Regular Laser
                        case '1':
                            rings[ring][i] = new LaserBasic(ring + 1, true, i, new int[] { 0, 0 }, 1);
                            break;
                        // Double-Sided Laser
                        case '2':
                            rings[ring][i] = new LaserBasic(ring + 1, true, i, new int[] { 0, 0 }, 2);
                            break;
                        // Mega Laser
                        case '3':
                            rings[ring][i] = new LaserBasic(ring + 1, true, i, new int[] { 0, 0 }, 3);
                            megaLaser = true;
                            break;
                        // Regular Blocker
                        case '4':
                            rings[ring][i] = new BlockerBasic(ring + 1, true, i, new int[] { 0, 0 }, 4);
                            break;
                        // Tunnel
                        case '5':
                            rings[ring][i] = new BlockerBasic(ring + 1, true, i, new int[] { 0, 0 }, 5);
                            tunnelCount++;
                            break;
                        // Portal
                        case '6':
                            rings[ring][i] = new BlockerBasic(ring + 1, true, i, new int[] { 0, 0 }, 6);
                            portalPuzzle = true;
                            break;
                        // Unknown Node means just put nothing
                        default:
                            System.out.println("Unknown node type: " + nodeType);
                            rings[ring][i] = null;
                            break;
                    }
                }
            }
        }
        return rings;
    }

    public boolean hasMegaLaser() {
        return megaLaser;
    }

    public boolean isPortalPuzzle() {
        return portalPuzzle;
    }

    public int getTunnelCount() {
        return tunnelCount;
    }
    
    public boolean isTogglePuzzle() {
        return togglePuzzle;
    }
}
