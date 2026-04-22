import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Arrays;
import java.util.Comparator;
import javax.imageio.ImageIO;

public class VideoImagePlaybackTest extends JPanel
{
    private Image[] videoFrames;
    private int currentFrame = 0;
    private final int FPS = 5;
    private Timer timer;
    private boolean isLooping = false;

    public VideoImagePlaybackTest()
    {
        loadFrames();

        int delay = 1000 / FPS;
        timer = new Timer(delay, e ->
        {
            if (videoFrames == null || videoFrames.length == 0) return;

            if (currentFrame < videoFrames.length - 1)
            {
                currentFrame++;
            }
            else if (isLooping)
            {
                currentFrame = 0;
            }
            else
            {
                timer.stop();
            }
            repaint();
        });
        timer.start();
    }

    private void loadFrames()
    {
        File folder = new File("frames");
        File[] files = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png"));

        if (files == null || files.length == 0)
        {
            System.err.println("No frames found in /frames directory!");
            videoFrames = new Image[0];
            return;
        }

        Arrays.sort(files, Comparator.comparingInt(f ->
                Integer.parseInt(f.getName().replaceAll("\\D", ""))));

        videoFrames = new Image[files.length];
        try
        {
            for (int i = 0; i < files.length; i++)
            {
                videoFrames[i] = ImageIO.read(files[i]);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    // Logic for the Player Controls
    public void togglePlay()
    {
        if (timer.isRunning()) timer.stop();
        else timer.start();
    }

    public void skipFrames(int amount)
    {
        currentFrame += amount;
        // Keep within bounds
        if (currentFrame < 0) currentFrame = 0;
        if (currentFrame >= videoFrames.length) currentFrame = videoFrames.length - 1;
        repaint();
    }

    public void toggleLoop()
    {
        isLooping = !isLooping;
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        if (videoFrames != null && currentFrame < videoFrames.length)
        {
            g.drawImage(videoFrames[currentFrame], 0, 0, getWidth(), getHeight(), this);
        }
    }

    public static void main(String[] args)
    {
        JFrame frame = new JFrame("Bad Apple Player");
        frame.setLayout(new BorderLayout());

        VideoImagePlaybackTest videoPanel = new VideoImagePlaybackTest();
        
        // --- Control Panel Setup ---
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(1, 5)); // 1 row, 5 buttons

        JButton btnRewind = new JButton("<<");
        JButton btnPlayPause = new JButton("Pause");
        JButton btnForward = new JButton(">>");
        JButton btnLoop = new JButton("Loop: OFF");

        // Rewind 30 frames
        btnRewind.addActionListener(e -> videoPanel.skipFrames(-30));

        // Play/Pause
        btnPlayPause.addActionListener(e -> {
            videoPanel.togglePlay();
            btnPlayPause.setText(videoPanel.timer.isRunning() ? "Pause" : "Play");
        });

        // Forward 30 frames
        btnForward.addActionListener(e -> videoPanel.skipFrames(30));

        // Loop Toggle
        btnLoop.addActionListener(e -> {
            videoPanel.toggleLoop();
            btnLoop.setText("Loop: " + (videoPanel.isLooping ? "ON" : "OFF"));
        });

        controlPanel.add(btnRewind);
        controlPanel.add(btnPlayPause);
        controlPanel.add(btnForward);
        controlPanel.add(btnLoop);

        frame.add(videoPanel, BorderLayout.CENTER);
        frame.add(controlPanel, BorderLayout.SOUTH);

        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
