// Copyright (C) 2014 by Klaus Jung
// All rights reserved.
// Date: 2014-10-02
package de.htw.fb4.imi.master.ws15_16.jungk;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.Edge;
import de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.Factory;
import de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.Outline;
import de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.Vertex;
import de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.ff.AbstractFloodFilling.Mode;
import de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.potrace.IOutlinePathFinder;
import de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.potrace.IOutlinePathFinder.TurnPolicy;
import de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.potrace.Potrace;
import de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.util.Colors;
import de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.util.ImageUtil;

public class PotraceGui extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final int border = 10;
	private static final int maxWidth = 400;
	private static final int maxHeight = 400;
	private static final File openPath = new File(".");
	private static String title = "Potrace ";
	private static final String author = "Markus Föllmer & Sascha Feldmann";
	private static final String initalOpen = "klein.png";
	private static final int TEXTAREA_COLS = 30;
	private static final int TEXTAREA_ROWS = 5;
	
	private static final int windowWidth = 800;
	private static final int windowHeight = 400;
	
	private JSlider zoomSlider;

	private static JFrame frame;

	private ImageView srcView; // source image view
	private ImageView dstView; // binarized image view


	private JComboBox<String> turnPoliciesList; // the selected binarization
												// method
	private JTextArea statusArea; // to print some status text

	private String message;
	protected Mode mode;
	private JCheckBox displayInnerCheckbox;

	private JPanel imagesPanel;
	private IOutlinePathFinder potraceAlgorithm;

	public PotraceGui() {
		super(new BorderLayout(border, border));

		// load the default image
		File input = new File(initalOpen);

		if (!input.canRead())
			input = openFile(); // file not found, choose another image

		srcView = new ImageView(input);
		srcView.setMaxSize(new Dimension(maxWidth, maxHeight));

		// create an empty destination image
		dstView = new ImageView(maxWidth, maxHeight);
		dstView.setMaxSize(new Dimension(maxWidth, maxHeight));

		// load image button
		JButton load = new JButton("Bild öffnen");
		load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File input = openFile();
				if (input != null) {
					srcView.loadImage(input);
					runPotrace();
				}
			}
		});

		// selector for the binarization method
		JLabel turnPolicyText = new JLabel("Turn Policy:");

		turnPoliciesList = new JComboBox<String>(TurnPolicy.stringValues());
		turnPoliciesList.setSelectedIndex(0); // set initial method
		turnPoliciesList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				runPotrace();
			}
		});
		
        // slider for zoom in picture
        JLabel zoomLabel = new JLabel("Zoom");
        this.zoomSlider = new JSlider(ImageView.MIN_ZOOM, 255, ImageView.MIN_ZOOM);    
        zoomSlider.setMinorTickSpacing(25);
        zoomSlider.setMajorTickSpacing(50);
        zoomSlider.setPaintLabels(true);
        zoomSlider.setPaintTicks(true);
    	
    	// Change-Listener for zoom slider
        zoomSlider.addChangeListener(new ChangeListener() {
  			@Override
  			public void stateChanged(ChangeEvent e) {
  				dstView.setZoom(zoomSlider.getValue());  				
  			}
  		});

		// some status text
		statusArea = new JTextArea(TEXTAREA_ROWS, TEXTAREA_COLS);
		statusArea.setEditable(false);
		
		// Inner outlines checkbox
		this.displayInnerCheckbox = new JCheckBox("Inner outlines");

		JScrollPane scrollPane = new JScrollPane(statusArea);

		// arrange all controls
		JPanel controls = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, border, 0, 0);
		controls.add(load, c);
		controls.add(turnPolicyText, c);
		controls.add(turnPoliciesList, c);
		controls.add(zoomLabel, c);
		controls.add(zoomSlider, c);
		controls.add(displayInnerCheckbox, c);
		
		displayInnerCheckbox.addChangeListener(new ChangeListener() {
  			@Override
  			public void stateChanged(ChangeEvent e) {
  				dstView.setDisplayInner(displayInnerCheckbox.isSelected());		
  				repaint();
  			}
  		});

		this.imagesPanel = new JPanel(new FlowLayout());
		imagesPanel.setPreferredSize(new Dimension(windowWidth, windowHeight));
		imagesPanel.add(dstView);		

		add(controls, BorderLayout.NORTH);
		add(imagesPanel, BorderLayout.CENTER);
		add(scrollPane, BorderLayout.SOUTH);

		setBorder(BorderFactory.createEmptyBorder(border, border, border, border));

		this.runPotrace();
	}

	private File openFile() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Images (*.jpg, *.png, *.gif)", "jpg", "png",
				"gif");
		chooser.setFileFilter(filter);
		chooser.setCurrentDirectory(openPath);
		int ret = chooser.showOpenDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION) {
			frame.setTitle(title + chooser.getSelectedFile().getName());
			return chooser.getSelectedFile();
		}
		return null;
	}

	private static void createAndShowGUI() {
		// create and setup the window
		title = title + " - " + author + " - ";

		frame = new JFrame(title + initalOpen);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JComponent newContentPane = new PotraceGui();
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		// display the window.
		frame.pack();
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	protected void runPotrace() {
		TurnPolicy policy = TurnPolicy.values()[turnPoliciesList.getSelectedIndex()];

		// image dimensions
		int width = srcView.getImgWidth();
		int height = srcView.getImgHeight();

		// get pixels arrays
		int srcPixels[] = srcView.getPixels();
		int dstPixels[] = java.util.Arrays.copyOf(srcPixels, srcPixels.length);

		// this.message = "Binarisieren mit \"" + methodName + "\"";
		this.message = "Konturenerkennung mit Potrace und Turn Policy \"" + policy;

		statusArea.setText(message);

		long time = 0;

		// trigger potrace
		this.potraceAlgorithm = Factory.newPotraceAlgorithm();
		this.potraceAlgorithm.setTurnPolicy(policy);
		time = detectAndShowOutline(dstPixels);
//
		dstView.setPixels(srcPixels, width, height);
//		dstView.setPixels(ImageUtil.get1DFrom2DArray(width, height, ((Potrace) this.potraceAlgorithm).getProcessingPixels()), width, height);
		frame.pack();

		dstView.saveImage("out.png");

		statusArea.setText(message + "\nRequired time: " + time + " ms");
	}

	private long detectAndShowOutline(int[] dstPixels) {
		this.potraceAlgorithm.setOriginalBinaryPixels(this.srcView.getImgWidth(), this.srcView.getImgHeight(),
				this.srcView.getPixels());

		// only measure labeling algorithm runtime, not the time to color pixels
		long startTime = System.currentTimeMillis();
		Set<Outline> foundOutlines = this.potraceAlgorithm.find();
		long time = System.currentTimeMillis() - startTime;

		// mark outlines somehow
//		this.paintOutlines(foundOutlines, dstPixels);
		this.dstView.setOutlines(foundOutlines);

		return time;
	}

	/**
	 * TODO @Markus : Hier Kanten der Outlines zeichnen
	 * 
	 * @param foundOutlines
	 * @param dstPixels
	 */
	private void paintOutlines(Set<Outline> foundOutlines, int[] dstPixels) {
		for (int i = 0; i < dstPixels.length; i++) {
			dstPixels[i] = Colors.WHITE;
		}

		for (Outline outline : foundOutlines) {
			int color;

			if (outline.isOuter()) {
				color = Colors.BLUE;
			} else {
				// paint inner outline
				color = Colors.ORANGE;
			}

			for (Edge outlineEdge : outline.getEdges()) {
				Vertex white = outlineEdge.getWhite();

				if (this.potraceAlgorithm.isWithinImageBoundaries(white)) {
					int whitePos1D = ImageUtil.calc1DPosition(this.srcView.getImgWidth(), white.getX(), white.getY());
					dstPixels[whitePos1D] = Colors.WHITE;
				}

				Vertex black = outlineEdge.getBlack();
				if (this.potraceAlgorithm.isWithinImageBoundaries(black)) {
					// zeichne Kontur-Pixel TODO nur Linie am linken Rand des
					// Pixels zeichnen, abhängig von outlineEdge.getDirectionX()
					// und outlineEdge.getDirectionY()
					int blackPos1D = ImageUtil.calc1DPosition(this.srcView.getImgWidth(), black.getX(), black.getY());
					dstPixels[blackPos1D] = color;
				}
			}

		}
	}
	
	private void paintRaster(){
		
	}
	

	public static int calculateGrayValue(int pixelValue) {
		// greyValue = R + G + B / 3
		return ((pixelValue & 0xff) + ((pixelValue & 0xff00) >> 8) + ((pixelValue & 0xff0000) >> 16)) / 3;
	}
}
