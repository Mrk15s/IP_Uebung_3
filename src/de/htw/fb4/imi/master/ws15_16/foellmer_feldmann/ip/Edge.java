/**
 * Image Processing WiSe 2015/16
 *
 * Authors: Markus Föllmer, Sascha Feldmann
 */
package de.htw.fb4.imi.master.ws15_16.foellmer_feldmann.ip;

/**
 * An edge in our case is a connection of two {@link Vertex}
 * - which are neighbours
 * - where the connection/edge vw separates both Vertex so that, foreground (black) is left and background (white) is right
 *
 * @author Sascha Feldmann <sascha.feldmann@gmx.de>
 * @since 17.11.2015
 */
public class Edge {
	private Vertex white;
	private Vertex black;
	
	private Integer directionX;
	private Integer directionY;	
	
	public Edge(Vertex white, Vertex black) {
		super();
		
		this.white = white;
		this.black = black;
	}

	public Vertex getWhite() {
		return white;
	}
	
	public void setWhite(Vertex v) {
		this.white = v;
	}
	
	public Vertex getBlack() {
		return black;
	}
	
	public void setBlack(Vertex w) {
		this.black = w;
	}

	/**
	 * Get the x direction of this edge from v to w.
	 * @return
	 */
	public int getDirectionX() {
		if (null == directionX) {
			this.calculateDirections();
		}
		
		return directionX;
	}
	
	/**
	 * Get the Y direction of this edge from v to w.
	 * @return
	 */
	public int getDirectionY() {
		if (null == directionY) {
			this.calculateDirections();
		}
		
		return directionY;
	}

	/**
	 * S. Foliensatz S. 14
	 */
	private void calculateDirections() {
		if (vertexIsLeftNeighbourOf(white, black)) {
			this.directionX = 0;
			this.directionY = 1;
		} else if (vertexIsBelowNeighbourOf(white, black)) {
			this.directionX = 1;
			this.directionY = 0;
		} else if (vertexIsAboveNeighbourOf(white, black)) {
			this.directionX = -1;
			this.directionY = 0;
		} else if (vertexIsRightNeighbourOf(white, black)) {
			this.directionX = 0;
			this.directionY = -1;
		}
	}	

	public boolean vertexIsLeftNeighbourOf(Vertex white, Vertex black) {		
		return (black.getX() - 1 == white.getX()
				&& black.getY() == white.getY()
				);
	}
	
	public boolean vertexIsBelowNeighbourOf(Vertex white, Vertex black) {
		return (white.getY() - 1 == black.getY()
				&& white.getX() == black.getX());
	}
	
	public boolean vertexIsAboveNeighbourOf(Vertex white, Vertex black) {
		return (black.getY() - 1 == white.getY()
				&& black.getX() == white.getX());
	}
	
	public boolean vertexIsRightNeighbourOf(Vertex white, Vertex black) {
		return (white.getX() - 1 == black.getX()
				&& white.getY() == black.getY());
	}	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((white == null) ? 0 : white.hashCode());
		result = prime * result + ((black == null) ? 0 : black.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Edge other = (Edge) obj;
		if (white == null) {
			if (other.white != null)
				return false;
		} else if (!white.equals(other.white))
			return false;
		if (black == null) {
			if (other.black != null)
				return false;
		} else if (!black.equals(other.black))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Edge [getV()=");
		builder.append(getWhite());
		builder.append(", getW()=");
		builder.append(getBlack());
		builder.append("]");
		return builder.toString();
	}	
}
