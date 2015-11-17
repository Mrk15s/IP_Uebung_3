/**
 * Image Processing WiSe 2015/16
 *
 * Authors: Markus Föllmer, Sascha Feldmann
 */
package de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.outline;

import de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.IOriginalPixels;
import de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.Vertex;
import de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.util.ImageUtil;

/**
 * Implementation of an outline algorithm including all necessary steps such as eroding, reverting and reflecting pixels.
 *
 * @since 20.10.2015
 */
public class Outline implements IOriginalPixels {	
	private int[][] originalBinaryPixels;
	private int[][] erodedPixels;
	private int[][] outlinePixels;

	protected int width;
	protected int height;

	private int[][] structureElement;

	public Outline() {
		this.create4NeighboursStructureElement();
	}

	private void create4NeighboursStructureElement() {
		this.structureElement = new int[3][3];

		this.structureElement[0][0] = 0;
		this.structureElement[0][1] = ImageUtil.FOREGROUND_VALUE;
		this.structureElement[0][2] = 0;
		this.structureElement[1][0] = ImageUtil.FOREGROUND_VALUE;
		this.structureElement[1][1] = ImageUtil.FOREGROUND_VALUE;
		this.structureElement[1][2] = ImageUtil.FOREGROUND_VALUE;
		this.structureElement[2][0] = 0;
		this.structureElement[2][1] = ImageUtil.FOREGROUND_VALUE;
		this.structureElement[2][2] = 0;
	}

	public int[][] getOriginalPixels() {
		return originalBinaryPixels;
	}

	/* (non-Javadoc)
	 * @see de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.outline.IOriginalPixels#setOriginalBinaryPixels(int, int, int[])
	 */
	@Override
	public void setOriginalBinaryPixels(int width, int height, int[] originalPixels) {
		this.width = width;
		this.height = height;
		this.originalBinaryPixels = ImageUtil.get2DFrom1DArray(width, height, originalPixels);
	}	
	
	/* (non-Javadoc)
	 * @see de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.outline.IOriginalPixels#setOriginalBinaryPixels(int[][])
	 */
	@Override
	public void setOriginalBinaryPixels(int[][] originalPixels) {
		this.originalBinaryPixels = originalPixels;
	}

	/**
	 * Main algorithm.
	 * 
	 * Make sure to have set the original/input pixels.
	 * 
	 * @return 2d array: the outline pixels.
	 */
	public int[][] executeOutline() {
		this.ensureThatOriginalWasSet();

		this.erodePixel();
		this.outlinePixel();

		return this.outlinePixels;
	}
	
	private void ensureThatOriginalWasSet() {
		if (null == this.originalBinaryPixels) {
			throw new IllegalStateException(
					"originalPixels wasn't set. Please set it before calling executeOutline().");
		}
	}
	
	/**
	 * Take original pixel, do erosion
	 */
	protected void erodePixel() {
		int[][] invertedOriginal = this.invertPixels(this.originalBinaryPixels);
		int[][] reflectedStructureElement = this.reflectPixels(this.structureElement);

		this.erodedPixels = this.invertPixels(this.dilatePixel(reflectedStructureElement, invertedOriginal));
	}
	
	protected int[][] invertPixels(int pixels[][]) {
		int[][] invertedPixels = new int[pixels.length][pixels[0].length];

		for (int i = 0; i < pixels.length; i++) {
			for (int j = 0; j < pixels[i].length; j++) {
				invertedPixels[i][j] = pixels[i][j] == 0xff000000 ? 0xffffffff : 0xff000000;
			}
		}

		return invertedPixels;
	}

	private int[][] reflectPixels(int[][] pixels) {
		int[][] reflectedPixels = new int[pixels.length][pixels[0].length];

		for (int i = 0; i < pixels.length; i++) {
			for (int j = 0; j < pixels[i].length; j++) {
				reflectedPixels[j][i] = pixels[i][j];
			}
		}

		return reflectedPixels;
	}	
	
	protected int[][] dilatePixel(int[][] structureElement, int binaryPixels[][]) {
		int[][] dilatedPixels = new int[this.width][this.height];
		
		for (int i = -1; i < structureElement.length - 1; i++) {
			for (int j = -1; j < structureElement[i + 1].length - 1; j++) {
				for (int x = 0; i + x < binaryPixels.length - 1; x++) {
					for (int y = 0; j + y < binaryPixels[x].length - 1; y++) {
						if (structureElement[i + 1][j + 1] == 0xff000000
								&& i + x >= 0
								&& j + y >= 0
								&& binaryPixels[x][y] == structureElement[i + 1][j + 1]
								) {
							dilatedPixels[i + x][j + y] = structureElement[i + 1][j + 1];
						}
					}
				}
			}
		}

		return dilatedPixels;
	}	

	protected void outlinePixel() {
		int[][] invertedEroded = this.invertPixels(this.erodedPixels);
		this.outlinePixels = new int[this.erodedPixels.length][this.erodedPixels[0].length];

		for (int i = 0; i < invertedEroded.length; i++) {
			for (int j = 0; j < invertedEroded[i].length; j++) {
				if (invertedEroded[i][j] == this.originalBinaryPixels[i][j]) {
					this.outlinePixels[i][j] = invertedEroded[i][j];
				} else {
					this.outlinePixels[i][j] = 0xffffffff; // "unmarked" pixels have to be white
				}
			}
		}
	}

	@Override
	public boolean isWithinImageBoundaries(int x, int y) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isWithinImageBoundaries(Vertex v) {
		// TODO Auto-generated method stub
		return false;
	}
}
