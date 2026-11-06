package laserlox;

import javax.swing.*;

import core.InputManager;
import core.State;
import graphics.TextureManager;
import states.MenuState;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;

public class App extends JPanel {
    private static final int TARGET_FPS = 60;
    private static final int FRAME_TIME = 1000 / TARGET_FPS;

    private final TextureManager textureManager = new TextureManager();
    private final InputManager inputManager = new InputManager();
    private State currentState;

    public App() {
        setFocusable(true);
        setupInput();
        changeState(new MenuState(this));

        Timer gameLoop = new Timer(FRAME_TIME, e -> {
            currentState.update(1.0f / TARGET_FPS, inputManager);
            inputManager.endFrame();
            repaint();
        });
        gameLoop.start();
    }

    public TextureManager getTextureManager() {
        return textureManager;
    }

    public InputManager getInputManager() {
        return inputManager;
    }

    public State getCurrentState() {
        return currentState;
    }

    public void changeState(State newState) {
        currentState = Objects.requireNonNull(newState, "newState");
        // Clear input for transitining to new state
        inputManager.clearTransitions();
        repaint();
    }

    private void setupInput() {
        int[] keys = {
                KeyEvent.VK_ENTER,
                KeyEvent.VK_ESCAPE,
                KeyEvent.VK_UP,
                KeyEvent.VK_DOWN,
                KeyEvent.VK_LEFT,
                KeyEvent.VK_RIGHT,
                KeyEvent.VK_SPACE,
                KeyEvent.VK_L
        };

        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        for (int keyCode : keys) {
            String pressedAction = "pressed_" + keyCode;
            String releasedAction = "released_" + keyCode;

            inputMap.put(KeyStroke.getKeyStroke(keyCode, 0, false), pressedAction);
            inputMap.put(KeyStroke.getKeyStroke(keyCode, 0, true), releasedAction);

            actionMap.put(pressedAction, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    inputManager.keyPressed(keyCode);
                }
            });

            actionMap.put(releasedAction, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    inputManager.keyReleased(keyCode);
                }
            });
        }

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                inputManager.mousePressed(e.getButton(), e.getX(), e.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                inputManager.mouseReleased(e.getButton(), e.getX(), e.getY());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                inputManager.mouseMoved(e.getX(), e.getY());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                inputManager.mouseMoved(e.getX(), e.getY());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                inputManager.mouseExited();
            }
        };
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        try {
            currentState.render(g2d);
        } finally {
            g2d.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("LazerLox");
            App app = new App();

            app.textureManager.getTexture("Goal.png");
            app.textureManager.getTexture("lasperPointer.png");
            app.textureManager.getTexture("Blocker.png");

            frame.addWindowFocusListener(new WindowAdapter() {
                @Override
                public void windowLostFocus(WindowEvent e) {
                    app.inputManager.reset();
                }
            });

            frame.add(app);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(640, 480);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
