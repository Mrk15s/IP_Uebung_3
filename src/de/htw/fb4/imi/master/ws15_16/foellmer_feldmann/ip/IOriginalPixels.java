package de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip;

public interface IOriginalPixels {

	/**
	 * Set the original pixels by an 1-dimensional pixel array. 
	 * 
	 * Internally, the pixels are stored as 2-dimensional array. For conversion, hand in widht and height.
	 * @param width
	 * @param height
	 * @param originalPixels
	 */
	void setOriginalBinaryPixels(int width, int height, int[] originalPixels);

	/**
	 * Set the original pixels as 2d-array as it's internally expected.
	 * 
	 * @see setOriginalBinaryPixels(int width, int height, int[] originalPixels) if you want to hand in a 1d array
	 * @param originalPixels
	 */
	void setOriginalBinaryPixels(int[][] originalPixels);
	
	boolean isWithinImageBoundaries(int x, int y);
	
	boolean isWithinImageBoundaries(Vertex v);

}