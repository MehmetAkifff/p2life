import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;

public class P2Life {

    private static final int GRID_SIZE = 20; 
    private static final int CELL_SIZE = 20; 
    private static final int DELAY = 200;   

    private int[][] redGrid;    
    private int[][] whiteGrid; 
    private JFrame frame;      
    private JPanel panel;      
    private boolean settingMode = true; 

    private ArrayList<Integer> redCounts = new ArrayList<>();
    private ArrayList<Integer> whiteCounts = new ArrayList<>();
    private JPanel graphPanel;

    public P2Life() {
        redGrid = new int[GRID_SIZE][GRID_SIZE];
        whiteGrid = new int[GRID_SIZE][GRID_SIZE];
        setupGUI();
        setupGraph();
    }

    // Arayüzü kur
    private void setupGUI() {
        frame = new JFrame("P2Life - Two Player Game of Life");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGrid(g);
            }
        };

        panel.setPreferredSize(new Dimension(GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE));
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!settingMode) return;

                int col = e.getX() / CELL_SIZE;
                int row = e.getY() / CELL_SIZE;

                if (SwingUtilities.isLeftMouseButton(e)) {
                    whiteGrid[row][col] = 1;
                    redGrid[row][col] = 0;
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    redGrid[row][col] = 1;
                    whiteGrid[row][col] = 0;
                }
                panel.repaint();
            }
        });

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && settingMode) {
                    settingMode = false;
                    startSimulation();
                }
            }
        });

        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    private void setupGraph() {
        JFrame graphFrame = new JFrame("Population Graph");
        graphFrame.setSize(400, 400);

        graphPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGraph((Graphics2D) g);
            }
        };
        
        graphPanel.setPreferredSize(new Dimension(400, 400)); 

        graphFrame.add(graphPanel);
        graphFrame.pack();
        graphFrame.setVisible(true);
    }

    private void drawGrid(Graphics g) {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (redGrid[i][j] == 1) {
                    g.setColor(Color.RED);
                } else if (whiteGrid[i][j] == 1) {
                    g.setColor(Color.WHITE);
                } else {
                    g.setColor(Color.BLACK);
                }
                g.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                g.setColor(Color.GRAY);
                g.drawRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
    }

    private void drawGraph(Graphics2D g) {
        int width = graphPanel.getWidth();
        int height = graphPanel.getHeight();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
    
        if (redCounts.isEmpty() || whiteCounts.isEmpty()) return;
    
        int maxSteps = Math.min(redCounts.size(), width);
    
        int maxPopulation = Math.max(
            redCounts.stream().max(Integer::compare).orElse(1), 
            whiteCounts.stream().max(Integer::compare).orElse(1)
        );
    
        g.setColor(Color.RED);
        for (int i = 1; i < maxSteps; i++) {
            int x1 = (i - 1) * width / maxSteps;
            int y1 = height - (redCounts.get(i - 1) * height / maxPopulation);
            int x2 = i * width / maxSteps;
            int y2 = height - (redCounts.get(i) * height / maxPopulation);
            g.drawLine(x1, y1, x2, y2);
        }
    
        g.setColor(Color.WHITE);
        for (int i = 1; i < maxSteps; i++) {
            int x1 = (i - 1) * width / maxSteps;
            int y1 = height - (whiteCounts.get(i - 1) * height / maxPopulation);
            int x2 = i * width / maxSteps;
            int y2 = height - (whiteCounts.get(i) * height / maxPopulation);
            g.drawLine(x1, y1, x2, y2);
        }
    
        g.setColor(Color.RED);
        g.drawString("Red Count: " + redCounts.get(redCounts.size() - 1), 10, 20);
    
        g.setColor(Color.WHITE);
        g.drawString("White Count: " + whiteCounts.get(whiteCounts.size() - 1), 10, 40);
    }
    

    private void updateGrids() {
        int[][] newRedGrid = new int[GRID_SIZE][GRID_SIZE];
        int[][] newWhiteGrid = new int[GRID_SIZE][GRID_SIZE];
        int redCount = 0;
        int whiteCount = 0;

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                int redNeighbors = countNeighbors(redGrid, i, j);
                int whiteNeighbors = countNeighbors(whiteGrid, i, j);

                if (redGrid[i][j] == 1) {
                    if (redNeighbors == 2 || redNeighbors == 3) {
                        newRedGrid[i][j] = 1;
                        redCount++;
                    }
                } else {
                    if (redNeighbors == 3 && redNeighbors > whiteNeighbors) {
                        newRedGrid[i][j] = 1;
                        redCount++;
                    }
                }

                if (whiteGrid[i][j] == 1) {
                    if (whiteNeighbors == 2 || whiteNeighbors == 3) {
                        newWhiteGrid[i][j] = 1;
                        whiteCount++;
                    }
                } else {
                    if (whiteNeighbors == 3 && whiteNeighbors > redNeighbors) {
                        newWhiteGrid[i][j] = 1;
                        whiteCount++;
                    }
                }

                if (newRedGrid[i][j] == 1 && newWhiteGrid[i][j] == 1) {
                    if (redNeighbors > whiteNeighbors) {
                        newWhiteGrid[i][j] = 0;
                        whiteCount--;
                    } else if (whiteNeighbors > redNeighbors) {
                        newRedGrid[i][j] = 0;
                        redCount--;
                    } else {
                        newRedGrid[i][j] = 0;
                        newWhiteGrid[i][j] = 0;
                    }
                }
            }
        }

        redCounts.add(redCount);
        whiteCounts.add(whiteCount);

        redGrid = newRedGrid;
        whiteGrid = newWhiteGrid;
    }

    private int countNeighbors(int[][] grid, int row, int col) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                int r = (row + i + GRID_SIZE) % GRID_SIZE;
                int c = (col + j + GRID_SIZE) % GRID_SIZE;
                count += grid[r][c];
            }
        }
        return count;
    }

    public void startSimulation() {
        new Timer(DELAY, e -> {
            updateGrids();
            panel.repaint();
            graphPanel.repaint();
        }).start();
    }

    public static void main(String[] args) {
        new P2Life();
    }
}
