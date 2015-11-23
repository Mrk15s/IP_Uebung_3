/**
 * Image Processing WiSe 2015/16
 *
 * Authors: Markus Föllmer, Sascha Feldmann
 */
package de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.potrace;

import java.util.Set;

import de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.Edge;
import de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.Outline;
import de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.OutlineSequenceSet;
import de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.Vertex;
import de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.util.ImageUtil;

/**
 * [SHORT_DESCRIPTION]
 *
 * @author Sascha Feldmann <sascha.feldmann@gmx.de>
 * @since 17.11.2015
 */
public class Potrace implements IOutlinePathFinder {
	private static final int PROCESSED = 1;

	private TurnPolicy turnPolicy = TurnPolicy.TURN_RIGHT;
	private int width;
	private int height;
	private int[][] originalPixels;
	private int[][] processingPixels;
	private int[][] processedPixels;

	private OutlineSequenceSet outlines;

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.IOriginalPixels#
	 * setOriginalBinaryPixels(int, int, int[])
	 */
	@Override
	public void setOriginalBinaryPixels(int width, int height, int[] originalPixels) {
		this.setOriginalBinaryPixels(ImageUtil.get2DFrom1DArray(width, height, originalPixels));
	}

	public TurnPolicy getTurnPolicy() {
		return turnPolicy;
	}

	public void setTurnPolicy(TurnPolicy turnPolicy) {
		this.turnPolicy = turnPolicy;
	}

	public int[][] getProcessingPixels() {
		return processingPixels;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.IOriginalPixels#
	 * setOriginalBinaryPixels(int[][])
	 */
	@Override
	public void setOriginalBinaryPixels(int[][] originalPixels) {
		this.width = originalPixels.length;
		this.height = originalPixels[0].length;

		this.originalPixels = originalPixels;
		this.processedPixels = new int[width][height];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.potrace.
	 * IOutlinePathFinder#find()
	 */
	@Override
	public Set<Outline> find() {
		this.outlines = new OutlineSequenceSet();

		this.findOuterPathes();
		this.findInnerPathes();

		return outlines;
	}

	private void findOuterPathes() {
		copyOriginalPixels();

		for (int x = 0; x < this.width; x++) {
			for (int y = 0; y < this.height; y++) {
				int pixel = originalPixels[x][y];

				if (PROCESSED != this.processedPixels[x][y] && ImageUtil.isForegoundPixel(pixel)) {
					Outline outerOutline = this.createPath(x, y, true);
					
					if (null != outerOutline) {
						outerOutline.setOriginalPixels(this.originalPixels);
						outerOutline.finishOutline();
				
						outlines.add(outerOutline);
					}		
				}
			}
		}
	}

	private void findInnerPathes() {
		copyOriginalPixels();

		OutlineSequenceSet outerOutlines = new OutlineSequenceSet(outlines);
		OutlineSequenceSet innerOutlines = new OutlineSequenceSet();

		this.invertPixelsInOutlines(outlines);

		for (Outline outerOutline : outerOutlines) {
			for (int y = outerOutline.getTopLimitY(); y <= outerOutline.getBottomLimitY(); y++) {
				Integer[] allXValues = outerOutline.getXValues(y);

				for (int x : allXValues) {
					int pixel = processingPixels[x][y];
		
					if (PROCESSED != this.processedPixels[x][y] && ImageUtil.isForegoundPixel(pixel)) {
						Outline innerOutline = this.createPath(x, y, false);
						
						if (null != innerOutline) {
							innerOutlines.add(innerOutline);
						}
					}
				}
			}
		}
		
		outlines.addAll(innerOutlines);
	}

	protected void copyOriginalPixels() {
		// copy the original pixels since we don't want to change them on
		// reversion
		this.processingPixels = java.util.Arrays.copyOf(this.originalPixels, this.originalPixels.length);
	}

	private void invertPixelsInOutlines(OutlineSequenceSet outerOutlines) {
		for (Outline outerOutline : outerOutlines) {
			for (int y = outerOutline.getTopLimitY(); y <= outerOutline.getBottomLimitY(); y++) {
				Integer[] allXValues = outerOutline.getXValues(y);

				for (Integer x : allXValues) {
					this.processingPixels[x][y] = ImageUtil.invertPixel(this.processingPixels[x][y]);
				}
			}
		}
	}

	private Outline createPath(int x, int y, boolean isOuter) {
		Outline sequence = new Outline(isOuter);

		Edge e = this.getInitialEdge(x, y, isOuter);

		if (null == e) {
			// initial edge doesn't match pattern (left black, right white)
			return null;
		}
		
		sequence.addEdge(e);

		while (null != e) {
			e = findNextEdgeOnOutline(e, sequence);

			if (sequence.hasEdge(e)) {
				// we reached the beginning
				e = null;
			} else if (null != e) {
				sequence.addEdge(e);

				if (this.isWithinImageBoundaries(e.getBlack())) {
					this.processedPixels[e.getBlack().getX()][e.getBlack().getY()] = PROCESSED;
				}
			}
		}

		return sequence;
	}

	private Edge getInitialEdge(int x, int y, boolean isOuter) {
		Vertex black = new Vertex(x, y); // current vertex (black pixel)
		Vertex whiteLeft = new Vertex(x - 1, y); // left neighbor of current
													// vertex (white pixel)
		if (this.isWithinImageBoundaries(black) && this.isWithinImageBoundaries(whiteLeft)
				&& ImageUtil.isForegoundPixel(this.processingPixels[black.getX()][black.getY()])
				&& !ImageUtil.isForegoundPixel(this.processingPixels[whiteLeft.getX()][whiteLeft.getY()])
				&& (!outlines.isSurroundedByAnExistingOutline(black, true) || !isOuter)
				) {
			return new Edge(whiteLeft, black);
		}
		
		return null;
	}

	private Edge findNextEdgeOnOutline(Edge startEdge, Outline sequence) {
		Edge potentialEdge = this.getFirstPatternEdge(startEdge);

		if (null != potentialEdge) {
			return potentialEdge;
		}

		potentialEdge = this.getSecondPatternEdge(startEdge);

		if (null != potentialEdge) {
			return potentialEdge;
		}

		potentialEdge = this.getThirdPatternEdge(startEdge);

		if (null != potentialEdge) {
			return potentialEdge;
		}

		potentialEdge = this.getFourthPatternEdge(startEdge);

		if (null != potentialEdge) {
			return potentialEdge;
		}

		return null;
	}

	/**
	 * Check for "pattern" BLACK BLACK BLACK WHITE
	 * 
	 * @param startEdge
	 * @return null if the pattern didn't match, otherwise turn right and return
	 *         next edge next edge
	 */
	private Edge getFirstPatternEdge(Edge currentEdge) {
		Vertex leftBlackAhead = new Vertex(currentEdge.getBlack().getX() + currentEdge.getDirectionX(),
				currentEdge.getBlack().getY() + currentEdge.getDirectionY());

		Vertex rightBlackAhead = new Vertex(currentEdge.getWhite().getX() + currentEdge.getDirectionX(),
				currentEdge.getWhite().getY() + currentEdge.getDirectionY());

		if (this.isWithinImageBoundaries(leftBlackAhead) && this.isWithinImageBoundaries(rightBlackAhead)
				&& ImageUtil.isForegoundPixel(this.processingPixels[leftBlackAhead.getX()][leftBlackAhead.getY()])
				&& ImageUtil.isForegoundPixel(this.processingPixels[rightBlackAhead.getX()][rightBlackAhead.getY()])) {
			// pattern matches
			return new Edge(currentEdge.getWhite(), rightBlackAhead);
		}

		return null;
	}

	/**
	 * Check for "pattern" BLACK WHITE BLACK WHITE
	 * 
	 * @param startEdge
	 * @return null if the pattern didn't match, otherwise go ahead and return
	 *         next edge
	 */
	private Edge getSecondPatternEdge(Edge currentEdge) {
		Vertex leftBlackAhead = new Vertex(currentEdge.getBlack().getX() + currentEdge.getDirectionX(),
				currentEdge.getBlack().getY() + currentEdge.getDirectionY());

		Vertex rightWhiteAhead = new Vertex(currentEdge.getWhite().getX() + currentEdge.getDirectionX(),
				currentEdge.getWhite().getY() + currentEdge.getDirectionY());

		if (this.isWithinImageBoundaries(leftBlackAhead)
				&& ImageUtil.isForegoundPixel(this.processingPixels[leftBlackAhead.getX()][leftBlackAhead.getY()])
				&& (!this.isWithinImageBoundaries(rightWhiteAhead) || !ImageUtil
						.isForegoundPixel(this.processingPixels[rightWhiteAhead.getX()][rightWhiteAhead.getY()]))) {
			// pattern matches
			return new Edge(rightWhiteAhead, leftBlackAhead);
		}

		return null;
	}

	/**
	 * Check for "pattern" WHITE WHITE BLACK WHITE
	 * 
	 * @param startEdge
	 * @return null if the pattern didn't match, otherwise turn left and return
	 *         next edge
	 */
	private Edge getThirdPatternEdge(Edge currentEdge) {
		Vertex leftWhiteAhead = new Vertex(currentEdge.getBlack().getX() + currentEdge.getDirectionX(),
				currentEdge.getBlack().getY() + currentEdge.getDirectionY());

		Vertex rightWhiteAhead = new Vertex(currentEdge.getWhite().getX() + currentEdge.getDirectionX(),
				currentEdge.getWhite().getY() + currentEdge.getDirectionY());

		if ((!this.isWithinImageBoundaries(leftWhiteAhead)
				|| !ImageUtil.isForegoundPixel(this.processingPixels[leftWhiteAhead.getX()][leftWhiteAhead.getY()]))
				&& (!this.isWithinImageBoundaries(rightWhiteAhead) || !ImageUtil
						.isForegoundPixel(this.processingPixels[rightWhiteAhead.getX()][rightWhiteAhead.getY()]))) {
			// pattern matches
			return new Edge(leftWhiteAhead, currentEdge.getBlack());
		}

		return null;
	}

	/**
	 * Check for "pattern" WHITE BLACK BLACK WHITE
	 * 
	 * @param startEdge
	 * @return null if the pattern didn't match, otherwise turn left and return
	 *         next edge
	 */
	private Edge getFourthPatternEdge(Edge currentEdge) {
		Vertex leftWhiteAhead = new Vertex(currentEdge.getBlack().getX() + currentEdge.getDirectionX(),
				currentEdge.getBlack().getY() + currentEdge.getDirectionY());

		Vertex rightBlackAhead = new Vertex(currentEdge.getWhite().getX() + currentEdge.getDirectionX(),
				currentEdge.getWhite().getY() + currentEdge.getDirectionY());

		if ((!this.isWithinImageBoundaries(leftWhiteAhead)
				|| !ImageUtil.isForegoundPixel(this.processingPixels[leftWhiteAhead.getX()][leftWhiteAhead.getY()]))
				&& this.isWithinImageBoundaries(rightBlackAhead)
				&& ImageUtil.isForegoundPixel(this.processingPixels[rightBlackAhead.getX()][rightBlackAhead.getY()])) {
			// pattern matches, so delegate to configured turn policy
			return this.turnPolicy.getNextEdge(currentEdge, leftWhiteAhead, rightBlackAhead);
		}

		return null;
	}

	public boolean isWithinImageBoundaries(int x, int y) {
		return (x >= 0) && (x < width) && (y >= 0) && (y < height);
	}

	public boolean isWithinImageBoundaries(Vertex v) {
		return this.isWithinImageBoundaries(v.getX(), v.getY());
	}
}
