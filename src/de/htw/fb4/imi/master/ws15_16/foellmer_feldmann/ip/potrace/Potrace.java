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

	private int oldY = -1;

	private OutlineSequenceSet outerOutlines;

	private Outline currentOuterOutline;

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
		OutlineSequenceSet outlineSequences = new OutlineSequenceSet();

		this.findOuterPathes(outlineSequences);
		// TODO fix inner pathes
		this.findInnerPathes(outlineSequences);

		return outlineSequences;
	}

	private void findOuterPathes(OutlineSequenceSet outlineSequences) {
		copyOriginalPixels();

		for (int x = 0; x < this.width; x++) {
			for (int y = 0; y < this.height; y++) {
				int pixel = originalPixels[x][y];
				Vertex pixelVertex = new Vertex(x, y);

				if (PROCESSED != this.processedPixels[x][y] && ImageUtil.isForegoundPixel(pixel)
						&& !outlineSequences.isSurroundedByAnExistingOutline(pixelVertex)) {
					Outline outerOutline = this.createPath(x, y, true);
					outerOutline.setOriginalPixels(this.originalPixels);
					outerOutline.finishOutline();
					outlineSequences.add(outerOutline);
				}
			}
		}
	}

	private void findInnerPathes(OutlineSequenceSet outlineSequences) {
		copyOriginalPixels();

		this.outerOutlines = new OutlineSequenceSet(outlineSequences);

		this.invertPixelsInOutlines(outerOutlines);

		for (Outline outerOutline : outerOutlines) {
			for (int y = outerOutline.getTopLimitY(); y <= outerOutline.getBottomLimitY(); y++) {
				Integer[] allXValues = outerOutline.getXValues(y);

				for (int startI = 0; startI < allXValues.length - 1; startI += 2) {
					int startX = allXValues[startI];
					int endX = allXValues[startI + 1];

					for (int x = startX; x < endX; x++) {
						int pixel = processingPixels[x][y];
						if (PROCESSED != this.processedPixels[x][y] && ImageUtil.isForegoundPixel(pixel)
						// &&
						// outerOutlines.isSurroundedByAnExistingOutline(pixelVertex)
						) {
							this.currentOuterOutline = outerOutline;
							try {
								Outline innerOutline = this.createPath(x, y, false);
								outlineSequences.add(innerOutline);
							} catch (IllegalStateException e) {

							}
						}
					}
				}
			}
		}
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

		// for (Outline outerOutline : outerOutlines) {
		// for (Edge edge : outerOutline.getEdges()) {
		// final int y = edge.getWhite().getY();
		//
		// if (y != oldY) {
		// int leftLimitX = edge.getWhite().getX();
		// int rightLimitX = this.width - 1;
		//
		// this.invertLineBetween(y, leftLimitX, rightLimitX, outerOutline);
		//
		// this.oldY = y;
		// }
		// }
		// }
	}

	private void invertLineBetween(int y, int leftLimitX, int rightLimitX, Outline outerOutline) {
		for (int x = leftLimitX; x <= rightLimitX; x++) {
			// if (!outerOutline.containsWhiteVertex(new Vertex(x, y))) {
			this.processingPixels[x][y] = ImageUtil.invertPixel(this.processingPixels[x][y]);
			// }
		}
	}

	private Outline createPath(int x, int y, boolean isOuter) {
		Outline sequence = new Outline(isOuter);

		Edge e = this.getInitialEdge(x, y);

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

	private Edge getInitialEdge(int x, int y) {
		Vertex black = new Vertex(x, y); // current vertex (black pixel)
		Vertex whiteLeft = new Vertex(x - 1, y); // left neighbor of current
													// vertex (white pixel)

		Edge newEdge = new Edge(whiteLeft, black);

		return newEdge;
	}

	private Edge findNextEdgeOnOutline(Edge startEdge, Outline sequence) {
		Edge potentialEdge = this.getFirstPatternEdge(startEdge);

		if (null != potentialEdge) {
			if (this.isNotAllowedInInnerMode(potentialEdge)) {
				return null;
			}

			return potentialEdge;
		}

		potentialEdge = this.getSecondPatternEdge(startEdge);

		if (null != potentialEdge) {
			if (this.isNotAllowedInInnerMode(potentialEdge)) {
				return null;
			}

			return potentialEdge;
		}

		potentialEdge = this.getThirdPatternEdge(startEdge);

		if (null != potentialEdge) {
			if (this.isNotAllowedInInnerMode(potentialEdge)) {
				return null;
			}

			return potentialEdge;
		}

		potentialEdge = this.getFourthPatternEdge(startEdge);

		if (null != potentialEdge) {
			if (this.isNotAllowedInInnerMode(potentialEdge)) {
				return null;
			}

			return potentialEdge;
		}

		return null;
	}

	private boolean isNotAllowedInInnerMode(Edge potentialEdge) {
		if (null != this.currentOuterOutline
				&& !this.currentOuterOutline.isSurroundedByAnExistingOutline(potentialEdge.getWhite())) {
			return true;
		}

		return false;
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
