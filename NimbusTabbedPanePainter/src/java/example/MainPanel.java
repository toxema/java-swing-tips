// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.addTab("JTree", new JScrollPane(new JTree()));
    tabbedPane.addTab("JSplitPane", new JSplitPane());
    tabbedPane.addTab("JTextArea", new JScrollPane(new JTextArea()));
    add(tabbedPane);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
      NimbusTabbedPanePainterUtils.configureUI();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

final class NimbusTabbedPanePainterUtils {
  public static final int OVERPAINT = 6;
  public static final int STROKE_SIZE = 2;
  public static final int ARC = 10;
  public static final Color CONTENT_BACKGROUND = Color.LIGHT_GRAY;
  public static final Color CONTENT_BORDER = Color.ORANGE; // Color.GRAY;
  public static final Color TAB_TABAREA_MASK = Color.GREEN; // CONTENT_BACKGROUND;
  public static final Color TAB_BACKGROUND = Color.PINK; // CONTENT_BORDER;
  public static final Color TABAREA_BACKGROUND = Color.CYAN; // CONTENT_BACKGROUND;
  public static final Color TABAREA_BORDER = Color.RED; // CONTENT_BORDER;

  private NimbusTabbedPanePainterUtils() {
    /* HideUtilityClassConstructor */
  }

  public static void configureUI() {
    UIDefaults d = UIManager.getLookAndFeelDefaults();
    d.put("TabbedPane:TabbedPaneContent.contentMargins", new Insets(0, 5, 5, 5));
    // d.put("TabbedPane:TabbedPaneTab.contentMargins", new Insets(2, 8, 3, 8));
    // d.put("TabbedPane:TabbedPaneTabArea.contentMargins", new Insets(3, 10, 4, 10));
    d.put("TabbedPane:TabbedPaneTabArea.contentMargins", new Insets(3, 10, OVERPAINT, 10));

    Painter<JComponent> tabAreaPainter = new TabAreaPainter();
    d.put("TabbedPane:TabbedPaneTabArea[Disabled].backgroundPainter", tabAreaPainter);
    d.put("TabbedPane:TabbedPaneTabArea[Enabled].backgroundPainter", tabAreaPainter);
    d.put("TabbedPane:TabbedPaneTabArea[Enabled+MouseOver].backgroundPainter", tabAreaPainter);
    d.put("TabbedPane:TabbedPaneTabArea[Enabled+Pressed].backgroundPainter", tabAreaPainter);

    d.put("TabbedPane:TabbedPaneContent.backgroundPainter", new TabbedPaneContentPainter());

    Painter<JComponent> tabPainter = new TabPainter(false);
    d.put("TabbedPane:TabbedPaneTab[Enabled+MouseOver].backgroundPainter", tabPainter);
    d.put("TabbedPane:TabbedPaneTab[Enabled+Pressed].backgroundPainter", tabPainter);
    d.put("TabbedPane:TabbedPaneTab[Enabled].backgroundPainter", tabPainter);

    Painter<JComponent> selectedTabPainter = new TabPainter(true);
    d.put("TabbedPane:TabbedPaneTab[Focused+MouseOver+Selected].backgroundPainter", selectedTabPainter);
    d.put("TabbedPane:TabbedPaneTab[Focused+Pressed+Selected].backgroundPainter", selectedTabPainter);
    d.put("TabbedPane:TabbedPaneTab[Focused+Selected].backgroundPainter", selectedTabPainter);
    d.put("TabbedPane:TabbedPaneTab[MouseOver+Selected].backgroundPainter", selectedTabPainter);
    d.put("TabbedPane:TabbedPaneTab[Selected].backgroundPainter", selectedTabPainter);
    d.put("TabbedPane:TabbedPaneTab[Pressed+Selected].backgroundPainter", selectedTabPainter);
  }

  protected static class TabPainter implements Painter<JComponent> {
    private final Color color;
    private final boolean selected;

    protected TabPainter(boolean selected) {
      this.selected = selected;
      this.color = selected ? CONTENT_BACKGROUND : TAB_BACKGROUND;
    }

    @Override public void paint(Graphics2D g, JComponent c, int width, int height) {
      int a = selected ? OVERPAINT : 0;
      int r = 6;
      int x = 3;
      int y = 3;
      Graphics2D g2 = (Graphics2D) g.create(0, 0, width, height + a);
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      int w = width - x;
      int h = height + a;

      // Paint tab shadow
      if (selected) {
        g2.setPaint(new Color(0, 0, 0, 20));
        RoundRectangle2D rrect = new RoundRectangle2D.Double(0d, 0d, w, h, r, r);
        for (int i = 0; i < x; i++) {
          rrect.setFrame(x - i, y - i, w + i + i, h);
          g2.fill(rrect);
        }
      }

      // Fill tab background
      g2.setColor(color);
      g2.fill(new RoundRectangle2D.Double(x, y, w - 1d, h + a, r, r));

      if (selected) {
        // Draw a border
        g2.setStroke(new BasicStroke(STROKE_SIZE));
        g2.setPaint(TABAREA_BORDER);
        g2.draw(new RoundRectangle2D.Double(x, y, w - 1d, h + a, r, r));

        // Overpaint the overexposed area with the background color
        g2.setColor(TAB_TABAREA_MASK);
        g2.fill(new Rectangle2D.Double(0d, height + STROKE_SIZE, width, OVERPAINT));
      }
      g2.dispose();
    }
  }

  protected static class TabAreaPainter implements Painter<JComponent> {
    @Override public void paint(Graphics2D g, JComponent c, int w, int h) {
      Graphics2D g2 = (Graphics2D) g.create(0, 0, w, h);
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      Shape r = new RoundRectangle2D.Double(0d, h - OVERPAINT, w - STROKE_SIZE, h - STROKE_SIZE, ARC, ARC);
      g2.setPaint(TABAREA_BACKGROUND);
      g2.fill(r);
      g2.setColor(TABAREA_BORDER);
      g2.setStroke(new BasicStroke(STROKE_SIZE));
      g2.draw(r);
      g2.dispose();
    }
  }

  protected static class TabbedPaneContentPainter implements Painter<JComponent> {
    @Override public void paint(Graphics2D g, JComponent c, int w, int h) {
      Graphics2D g2 = (Graphics2D) g.create(0, 0, w, h);
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.translate(0, -OVERPAINT);

      Shape r = new RoundRectangle2D.Double(0d, 0d, w - STROKE_SIZE, h - STROKE_SIZE + OVERPAINT, ARC, ARC);
      g2.setPaint(CONTENT_BACKGROUND);
      g2.fill(r);
      g2.setColor(CONTENT_BORDER);
      g2.setStroke(new BasicStroke(STROKE_SIZE));
      g2.draw(r);
      g2.dispose();
    }
  }
}
