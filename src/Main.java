import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.net.URI;
import java.util.Random;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main implements NativeKeyListener {
    private boolean isActivated = false; // One activation flag for all commands
    private String MUSIC_FOLDER_PATH = "mood";
    private Clip currentClip = null;
    private JFrame musicWindow;
    private JLabel songLabel;
    private boolean isPlaying = false;
    private static String ROSE_IMAGE_PATH = "rose.jpg";
    private String LOVE_STORIES_FOLDER_PATH = "love_stories";

    public static void main(String[] args) {
        try {
            GlobalScreen.registerNativeHook();
            System.out.println("JNativeHook initialized successfully.");
            GlobalScreen.addNativeKeyListener(new Main());
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            ex.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        int keyCode = e.getKeyCode();

        // Single activation/deactivation (Ctrl + 2 and Ctrl + 3)
        if (isCtrlPressed(e) && keyCode == NativeKeyEvent.VC_2 && !isActivated) {
            isActivated = true;
            System.out.println("Activated!");
        }

        if (isCtrlPressed(e) && keyCode == NativeKeyEvent.VC_3 && isActivated) {
            isActivated = false;
            System.out.println("Deactivated!");
        }

        // Commands when activated
        if (isActivated) {
            switch (keyCode) {
                case NativeKeyEvent.VC_ENTER: // Command 1: "I love you" window
                    createNewWindow();
                    playSound("soft-notice-146623.wav");
                    break;
                case NativeKeyEvent.VC_SPACE: // Command 2: Toggle music
                    toggleMusic();
                    break;
                case NativeKeyEvent.VC_SHIFT: // Command 3: Change wallpaper
                    changeWallpaper(ROSE_IMAGE_PATH);
                    break;
                case NativeKeyEvent.VC_ALT: // Command 4: Open love stories folder
                    openLoveStoriesFolder();
                    break;
                case NativeKeyEvent.VC_TAB: // Command 5: Open browser search for today's love quote
                    searchLoveFacts();
                    break;
            }
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
    }

    private boolean isCtrlPressed(NativeKeyEvent e) {
        return (e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0;
    }

    private void toggleMusic() {
        try {
            if (currentClip != null && currentClip.isRunning()) {
                currentClip.stop();
                currentClip.close();
                System.out.println("Music Stopped.");
            }

            File folder = new File(MUSIC_FOLDER_PATH);
            if (!folder.exists() || !folder.isDirectory()) {
                System.err.println("Invalid folder path.");
                return;
            }

            File[] files = folder.listFiles((dir, name) -> name.endsWith(".wav"));
            if (files == null || files.length == 0) {
                System.err.println("No music files found in the folder.");
                return;
            }

            Random rand = new Random();
            File randomFile = files[rand.nextInt(files.length)];

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(randomFile);
            currentClip = AudioSystem.getClip();
            currentClip.open(audioInputStream);
            currentClip.start();
            System.out.println("Playing: " + randomFile.getName());

            createMusicWindow(randomFile.getName());

        } catch (Exception e) {
            System.err.println("Error playing random music.");
            e.printStackTrace();
        }
    }

    private void createMusicWindow(String songName) {
        if (musicWindow == null || !musicWindow.isVisible()) {
            musicWindow = new JFrame("Now Playing");
            musicWindow.setSize(500, 200);
            musicWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            musicWindow.setUndecorated(true);
            musicWindow.setShape(new RoundRectangle2D.Float(0, 0, 500, 500, 50, 50));

            JPanel panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    Color topColor = new Color(244, 188, 6, 218);
                    Color bottomColor = new Color(234, 138, 15);
                    GradientPaint gradient = new GradientPaint(0, 0, topColor, 0, getHeight(), bottomColor);

                    g2d.setPaint(gradient);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            };

            panel.setLayout(new BorderLayout());

            songLabel = new JLabel("Now playing: " + songName, SwingConstants.CENTER);
            songLabel.setFont(new Font("Quintessential", Font.BOLD, 14));
            songLabel.setForeground(new Color(149, 0, 144));
            panel.add(songLabel, BorderLayout.CENTER);

            musicWindow.add(panel);
            musicWindow.setLocationRelativeTo(null);
            musicWindow.setAlwaysOnTop(true);
            musicWindow.setVisible(true);

            musicWindow.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    if (currentClip != null && currentClip.isRunning()) {
                        currentClip.stop();
                        currentClip.close();
                        System.out.println("Music stopped due to window close.");
                    }
                }
            });

            isPlaying = true;
        } else {
            // If the window exists, update the song name
            songLabel.setText("Now playing: " + songName);
        }
    }

    private void createNewWindow() {
        String message = "I love you";

        int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        int randomX = (int) (Math.random() * (screenWidth - 300)); // 300 is the window width
        int randomY = (int) (Math.random() * (screenHeight - 150)); // 150 is the window height

        JFrame frame = new JFrame("Love Note");
        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setFont(new Font("Quintessential", Font.BOLD, 20));
        label.setForeground(Color.WHITE);

        // Set frame properties
        frame.setSize(300, 150);
        frame.setLocation(randomX, randomY);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setUndecorated(true);
        frame.setResizable(false);
        frame.setOpacity(1f);
        frame.setShape(new RoundRectangle2D.Float(0, 0, 300, 150, 50, 50));

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(0, 0, Color.PINK, getWidth(), getHeight(), Color.MAGENTA);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        frame.add(panel);
        panel.setLayout(new BorderLayout());
        panel.add(label, BorderLayout.CENTER);

        frame.setAlwaysOnTop(true);
        frame.setVisible(true);
    }

    private void playSound(String soundFileName) {
        try {
            InputStream audioSource = getClass().getResourceAsStream("/" + soundFileName);
            if (audioSource == null) {
                System.err.println("Sound file not found: " + soundFileName);
                return;
            }
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioSource);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception e) {
            System.err.println("Error playing sound.");
            e.printStackTrace();
        }
    }

    private void changeWallpaper(String imagePath) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            setWindowsWallpaper(imagePath);
        } else {
            System.out.println("Wallpaper change is not supported for this OS yet.");
        }
    }

    private void setWindowsWallpaper(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                System.err.println("Image file not found: " + imagePath);
                return;
            }

            String absolutePath = imageFile.getAbsolutePath();
            String command = "powershell.exe Set-ItemProperty -Path 'HKCU:\\Control Panel\\Desktop' -Name Wallpaper -Value '" + absolutePath + "'; RUNDLL32.EXE user32.dll,UpdatePerUserSystemParameters";

            Runtime.getRuntime().exec(command);
            System.out.println("Wallpaper changed successfully to " + absolutePath);
        } catch (IOException e) {
            System.err.println("Error changing wallpaper.");
            e.printStackTrace();
        }
    }

    private void openLoveStoriesFolder() {
        try {
            File folder = new File(LOVE_STORIES_FOLDER_PATH);
            if (folder.exists() && folder.isDirectory()) {
                System.out.println("Opening love stories folder...");
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    Runtime.getRuntime().exec("explorer.exe " + folder.getAbsolutePath());
                } else {
                    System.out.println("Folder opening is only supported for Windows.");
                }
            } else {
                System.out.println("Love stories folder not found.");
            }
        } catch (IOException ex) {
            System.err.println("Error opening the folder.");
            ex.printStackTrace();
        }
    }

    public void searchLoveFacts() {
        try {
            // Get today's month and day
            String date = new SimpleDateFormat("MM_dd").format(new Date());

            // Construct the URL for today's love quote
            String url = "https://links2love.com/" + date + ".htm";

            // Open the browser with the constructed URL
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                // Windows
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                // macOS
                Runtime.getRuntime().exec(new String[]{"open", url});
            } else {
                System.out.println("Browser opening is only supported for Windows or macOS.");
            }
        } catch (IOException e) {
            System.err.println("Error opening the browser.");
            e.printStackTrace();
        }
    }
}
