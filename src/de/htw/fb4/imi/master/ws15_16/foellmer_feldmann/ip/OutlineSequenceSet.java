/**
 * Image Processing WiSe 2015/16
 *
 * Authors: Markus Föllmer, Sascha Feldmann
 */
package de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip;

import java.util.HashSet;

/**
 * [SHORT_DESCRIPTION] 
 *
 * @author Sascha Feldmann <sascha.feldmann@gmx.de>
 * @since 17.11.2015
 */
public class OutlineSequenceSet extends HashSet<Outline> {

	protected Outline lastSurroundingOutline;

	/**
	 * 
	 */
	private static final long serialVersionUID = -1144198700593319286L;

	public OutlineSequenceSet(OutlineSequenceSet outlineSequences) {
		super(outlineSequences);
	}

	public OutlineSequenceSet() {
		super();
	}

	public boolean isSurroundedByAnExistingOutline(Vertex pixelVertex) {
		return this.isSurroundedByAnExistingOutline(pixelVertex, false);
	}
	
	public Outline getLastSurroundingOutline() {
		return lastSurroundingOutline;
	}

	public int getLeftLimitX(int y) {
		return this.lastSurroundingOutline.getLeftLimitX(y);
	}	
	
	public int getRightLimitX(int y) {
		return this.lastSurroundingOutline.getRightLimitX(y);
	}

	public boolean isSurroundedByAnExistingOutline(Vertex pixelVertex, boolean checkForOnlyBlackPixelsRightTillOutline) {
		for (Outline outline : this) {
			if (outline.isSurroundedByAnExistingOutline(pixelVertex, checkForOnlyBlackPixelsRightTillOutline)) {
				this.lastSurroundingOutline = outline;
				return true;
			}
		}
		
		return false;
	}
}
