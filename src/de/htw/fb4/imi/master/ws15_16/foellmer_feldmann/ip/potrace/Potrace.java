/**
 * Image Processing WiSe 2015/16
 *
 * Authors: Markus Föllmer, Sascha Feldmann
 */
package de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.potrace;

import java.util.HashSet;
import java.util.Set;

import de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.Edge;
import de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.OutlineSequence;
import de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.Vertex;
import de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.util.ImageUtil;

/**
 * [SHORT_DESCRIPTION]
 *
 * @author Sascha Feldmann <sascha.feldmann@gmx.de>
 * @since 17.11.2015
 */
public class Potrace implements IOutlinePathFinder {
	public enum TurnPolicy {
		/**
		 * Policy to force turn to left on pattern WHITE BLACK BLACK WHITE
		 */
		TURN_LEFT, 
		TURN_RIGHT;

		/**
		 * Get the next edge according to the turn policy.
		 * 
		 * @return
		 * @throws IllegalStateException
		 */
		public Edge getNextEdge(Edge currentEdge, Vertex leftWhiteAhead, Vertex rightBlackAhead) {
			switch (this) {
			case TURN_LEFT:
				return this.getLeftEdge(currentEdge, leftWhiteAhead);
			case TURN_RIGHT:
				return this.getRightEdge(currentEdge, rightBlackAhead);
			default:
				throw new IllegalStateException("No turn policy implemented for " + this + "!");
			}
		}
		
		private Edge getLeftEdge(Edge currentEdge, Vertex leftWhiteAhead) {
			return new Edge(leftWhiteAhead, currentEdge.getBlack());			
		}
		
		private Edge getRightEdge(Edge currentEdge, Vertex rightBlackAhead) {
			return new Edge(currentEdge.getWhite(), rightBlackAhead);
		}
	}

	private static final int PROCESSED = 1;

	private TurnPolicy turnPolicy = TurnPolicy.TURN_LEFT;
	private int width;
	private int height;
	private int[][] originalPixels;
	private int[][] processedPixels;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.IOriginalPixels#
	 * setOriginalBinaryPixels(int[][])
	 */
	@Override
	public void setOriginalBinaryPixels(int[][] originalPixels) {
		int width = originalPixels.length;
		int height = originalPixels[0].length;

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
	public Set<OutlineSequence> find() {
		Set<OutlineSequence> outlineSequences = new HashSet<OutlineSequence>();

		this.findPathes(outlineSequences);

		return outlineSequences;
	}

	private void findPathes(Set<OutlineSequence> outlineSequences) {
		for (int x = 0; x < this.width; x++) {
			for (int y = 0; y < this.height; y++) {
				int pixel = originalPixels[x][y];

				if (PROCESSED != this.processedPixels[x][y] && ImageUtil.isForegoundPixel(pixel)) {
					OutlineSequence outlineSequence = this.createPath(x, y);
					outlineSequences.add(outlineSequence);
				}
			}
		}
	}

	private OutlineSequence createPath(int x, int y) {
		OutlineSequence sequence = new OutlineSequence();

		Edge e = this.getInitialEdge(x, y);

		while (null != e) {
			e = findNextEdgeOnOutline(e, sequence);
			sequence.addEdge(e);
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

	private Edge findNextEdgeOnOutline(Edge startEdge, OutlineSequence sequence) {
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

		// no pattern matched at all
		return null;
	}		

	/**
	 * Check for "pattern" 
	 * BLACK BLACK 
	 * BLACK WHITE
	 * 
	 * @param startEdge
	 * @return null if the pattern didn't match, otherwise turn right and return next edge
	 *         next edge
	 */
	private Edge getFirstPatternEdge(Edge currentEdge) {
		Vertex leftBlackAhead = new Vertex(currentEdge.getBlack().getX() + currentEdge.getDirectionX(),
				currentEdge.getBlack().getY() + currentEdge.getDirectionY());

		Vertex rightBlackAhead = new Vertex(currentEdge.getWhite().getX() + currentEdge.getDirectionX(),
				currentEdge.getWhite().getY() + currentEdge.getDirectionY());

		if (ImageUtil.isForegoundPixel(this.originalPixels[leftBlackAhead.getX()][leftBlackAhead.getY()])
				&& ImageUtil.isForegoundPixel(this.originalPixels[rightBlackAhead.getX()][rightBlackAhead.getY()])) {
			// pattern matches
			return new Edge(currentEdge.getWhite(), rightBlackAhead);
		}

		return null;
	}

	/**
	 * Check for "pattern" 
	 * BLACK WHITE 
	 * BLACK WHITE
	 * 
	 * @param startEdge
	 * @return null if the pattern didn't match, otherwise go ahead and return next edge
	 */
	private Edge getSecondPatternEdge(Edge currentEdge) {
		Vertex leftBlackAhead = new Vertex(currentEdge.getBlack().getX() + currentEdge.getDirectionX(),
				currentEdge.getBlack().getY() + currentEdge.getDirectionY());

		Vertex rightWhiteAhead = new Vertex(currentEdge.getWhite().getX() + currentEdge.getDirectionX(),
				currentEdge.getWhite().getY() + currentEdge.getDirectionY());

		if (ImageUtil.isForegoundPixel(this.originalPixels[leftBlackAhead.getX()][leftBlackAhead.getY()])
				&& !ImageUtil.isForegoundPixel(this.originalPixels[rightWhiteAhead.getX()][rightWhiteAhead.getY()])) {
			// pattern matches
			return new Edge(rightWhiteAhead, leftBlackAhead);
		}

		return null;
	}
	
	/**
	 * Check for "pattern" 
	 * WHITE WHITE 
	 * BLACK WHITE
	 * 
	 * @param startEdge
	 * @return null if the pattern didn't match, otherwise turn left and return next edge
	 */
	private Edge getThirdPatternEdge(Edge currentEdge) {
		Vertex leftWhiteAhead = new Vertex(currentEdge.getBlack().getX() + currentEdge.getDirectionX(),
				currentEdge.getBlack().getY() + currentEdge.getDirectionY());

		Vertex rightWhiteAhead = new Vertex(currentEdge.getWhite().getX() + currentEdge.getDirectionX(),
				currentEdge.getWhite().getY() + currentEdge.getDirectionY());

		if (!ImageUtil.isForegoundPixel(this.originalPixels[leftWhiteAhead.getX()][leftWhiteAhead.getY()])
				&& !ImageUtil.isForegoundPixel(this.originalPixels[rightWhiteAhead.getX()][rightWhiteAhead.getY()])) {
			// pattern matches
			return new Edge(leftWhiteAhead, currentEdge.getBlack());
		}

		return null;
	}
	
	/**
	 * Check for "pattern" 
	 * WHITE BLACK 
	 * BLACK WHITE
	 * 
	 * @param startEdge
	 * @return null if the pattern didn't match, otherwise turn left and return next edge
	 */
	private Edge getFourthPatternEdge(Edge currentEdge) {
		Vertex leftWhiteAhead = new Vertex(currentEdge.getBlack().getX() + currentEdge.getDirectionX(),
				currentEdge.getBlack().getY() + currentEdge.getDirectionY());

		Vertex rightBlackAhead = new Vertex(currentEdge.getWhite().getX() + currentEdge.getDirectionX(),
				currentEdge.getWhite().getY() + currentEdge.getDirectionY());

		if (!ImageUtil.isForegoundPixel(this.originalPixels[leftWhiteAhead.getX()][leftWhiteAhead.getY()])
				&& ImageUtil.isForegoundPixel(this.originalPixels[rightBlackAhead.getX()][rightBlackAhead.getY()])) {
			// pattern matches, so delegate to configured turn policy
			return this.turnPolicy.getNextEdge(currentEdge, leftWhiteAhead, rightBlackAhead);
		}

		return null;
	}
}
