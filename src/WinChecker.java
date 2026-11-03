public class WinChecker {

    boolean megaCheck;
    boolean twoSideCheck;
    boolean tunnelCheck;
    boolean portalCheck;
    boolean toggleCheck;
    boolean carryCheck;

    WinChecker(){
        megaCheck = false;
        twoSideCheck = false;
        tunnelCheck = false;
        portalCheck = false;
        toggleCheck = false;
        carryCheck = false;
    }

    WinChecker(boolean hasMega, boolean hasDouble, boolean hasTunnel, boolean hasPortal, boolean hasToggle, boolean hasCarry){
        megaCheck = hasMega;
        twoSideCheck = hasDouble;
        tunnelCheck = hasTunnel;    
        portalCheck = hasPortal;
        toggleCheck = hasToggle;
        carryCheck = hasCarry;
    }

    public boolean checkWin(boolean[] goals, LoxNode[][] rings) {
        boolean hitGoal = false;
        boolean canMega = megaCheck;
        boolean needMega = false;
        for (int g = 0; g < goals.length; g++) {
            if (goals[g]) {
                hitGoal = false;
                boolean hasLaser = false;
                for (int r = rings.length - 1; r > -1; r--) {
                    if (rings[r][g] != null && !rings[r][g].IsOn()){
                        System.out.println("Ring Off at first pass.");
                        break;
                    }
                    else if (rings[r][g] != null && rings[r][g].GetType() > 0) {
                        int tmp = rings[r][g].GetType();
                        // Double-sided laser with nothing blocking it
                        if (!needMega && tmp == 2) {
                            System.out.println("Double-Sided Laser found at goal " + g + ", goal is hit.");
                            hasLaser = true;
                            hitGoal = true;
                            break;
                        }
                        // First blocking node found
                        else if (canMega && tmp > 0 && tmp != 5) {
                            canMega = false;
                            needMega = true;
                        }
                        // Additional blocking node found, goal is blocked
                        else if (!canMega && tmp > 0 && tmp != 5) {
                            System.out.println("Too many obstacles at goal " + g + ", goal is blocked.");
                            needMega = false;
                            return false;
                        }
                    }
                }
                if (!hasLaser) {
                    for (int r = 0; r < rings.length; r++) {
                        // Checking nodes on the opposite side of the same ring as the goal.
                        int offset = g + 6;
                        if (offset > 11)
                            offset -= 12;
                        if (rings[r][offset] != null && !rings[r][offset].IsOn()){
                            System.out.println("Ring Off at second pass.");
                            break;
                        }

                        if (rings[r][offset] != null && rings[r][offset].GetType() > 0) {
                            int tmp = rings[r][offset].GetType();
                            if (!hasLaser && tmp < 4) {
                                // Laser found with nothing blocking it
                                if (!needMega) {
                                    System.out.println("Laser found at goal " + g + ", goal is hit.");
                                    hasLaser = true;
                                    hitGoal = true;
                                    break;
                                }
                                // There is one blocking node between this mega laser and the goal
                                else if (tmp == 3) {
                                    System.out.println("Mega Laser found at goal " + g + ", goal is hit.");
                                    hasLaser = true;
                                    needMega = false;
                                    hitGoal = true;
                                    break;
                                }
                                // there is one blocking node between this laser and the goal
                                else {
                                    System.out.println("Too many obstacles at goal " + g + ", goal is blocked.");
                                    needMega = false;
                                    return false;
                                }
                            }
                            // The first blocking node was hit. We now need the laser to be a mega laser
                            else if (canMega && tmp > 0 && tmp != 5) {
                                canMega = false;
                            }
                            // Another blocking node was found, goal is blocked
                            else if (!canMega && tmp > 0 && tmp != 5) {
                                System.out.println("Too many obstacles at goal " + g + ", goal is blocked.");
                                needMega = false;
                                return false;
                            }
                        }
                    }
                }
                if (g < goals.length && !hitGoal) {
                    System.out.println("No laser found at goal " + g + ".");
                    return false;
                }
            }
        }

        return true;
    }
}
