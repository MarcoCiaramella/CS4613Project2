package project2;

import graphicslib3D.Vector3D;

/**
 * Auxiliary class for quickly generating a pentagonal prism.
 * <p>
 * Based on Program 6.1.1 - Sphere (Coded Version) from Gordon & Clevenger.
 *
 * @author Eric Peterson
 */
public class PentagonalPrism
{
	/* ********* *
	 * Constants *
	 * ********* */
	private static final int NUM_VERTICES = 10;
	private static final int NUM_INDICES = 30;
	
	/* **************** *
	 * Member Variables *
	 * **************** */
	private Vector3D[] m_vertices;
	private int[] m_indices;
	
	public PentagonalPrism()
	{
		m_vertices = new Vector3D[NUM_VERTICES];
		m_indices = new int[NUM_INDICES];
	}
}
