/**
 * Image Processing WiSe 2015/16
 *
 * Authors: Markus Föllmer, Sascha Feldmann
 */
package de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip;

import java.util.ArrayList;
import java.util.List;

/**
 * [SHORT_DESCRIPTION] 
 *
 * @author Sascha Feldmann <sascha.feldmann@gmx.de>
 * @since 10.11.2015
 */
public class Sequence {
	public static final int TYPE_INNER = 0;
	public static final int TYPE_OUTER = 1;
	
	protected List<Vertex> vertices = new ArrayList<>();
	protected boolean isClosed = false;
	
	protected int type;
	
	public boolean isClosed() {
		return isClosed;
	}
	
	public boolean isOuter() {
		return TYPE_OUTER == this.type;
	}

	public void addVertex(Vertex vertex)
	{
		if (this.vertices.get(0).equals(vertex)) {
			// path is completed
			this.isClosed = true;			
		}
		
		this.vertices.add(vertex);
	}

	public Vertex[] getVertices() {
		return vertices.toArray(new Vertex[this.vertices.size()]);
	}
}
