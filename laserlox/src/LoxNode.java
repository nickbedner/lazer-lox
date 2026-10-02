public interface LoxNode {
    public void Toggle();           //Toggles the on/off state of the LoxNode
    public int GetRing();           //Returns the Ring the LoxNode is in
    public int GetPos();            //Returns the position in the Ring Array the LoxNode is in
    public boolean IsOn();          //Returns whether this LoxNode is turned on
    public int[] GetPosAbsolute();  //Returns the literal position in screenspace of the LoxNode
    public int GetType();           //Returns the type of LoxNode, 0: Empty, 1-3: Lasers, 4: Blockers 
}
