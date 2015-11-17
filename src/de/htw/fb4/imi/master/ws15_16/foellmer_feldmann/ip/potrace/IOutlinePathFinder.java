/**
 * Image Processing WiSe 2015/16
 *
 * Authors: Markus Föllmer, Sascha Feldmann
 */
package de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.potrace;

import java.util.Set;

import de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.IOriginalPixels;
import de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip.OutlineSequence;

/**
 * Interface for potrace path finding algorithms.
 *
 * @author Sascha Feldmann <sascha.feldmann@gmx.de>
 * @since 17.11.2015
 */
public interface IOutlinePathFinder extends IOriginalPixels {

	
	/**
	 * Find all outline paths within an image.
	 * 
	 * @return a set of {@link OutlineSequence}.
	 */
	Set<OutlineSequence> find();
	
}
