/**
 * Image Processing WiSe 2015/16
 *
 * Authors: Markus Föllmer, Sascha Feldmann
 */
package de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.potrace;

import java.util.Set;

import de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.Edge;
import de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.IOriginalPixels;
import de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.Outline;
import de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.Vertex;

/**
 * Interface for potrace path finding algorithms.
 *
 * @author Sascha Feldmann <sascha.feldmann@gmx.de>
 * @since 17.11.2015
 */
public interface IOutlinePathFinder extends IOriginalPixels {
	public enum TurnPolicy {
		TURN_RIGHT, 
		/**
		 * Policy to force turn to left on pattern WHITE BLACK BLACK WHITE
		 */
		TURN_LEFT 
		;

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

		public static String[] stringValues() {
			String[] values = new String[TurnPolicy.values().length];
			
			for (int i=0; i < TurnPolicy.values().length; i++) {
				values[i] = TurnPolicy.values()[i].toString();
			}
			
			return values;
		}
	}
	
	/**
	 * Find all outline paths within an image.
	 * 
	 * @return a set of {@link Outline}.
	 */
	Set<Outline> find();
	
	void setTurnPolicy(TurnPolicy turnPolicy);	
}
