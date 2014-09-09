package com.horowitz.mickey;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

public interface ImageComparator {

	/**
	 * Compares two images with allowed roughness.
	 *
	 * @param image1
	 *          an image to compare.
	 * @param image2
	 *          an image to compare.
	 *
	 * @return true if images have the same sizes and number of unmatching pixels
	 *         less or equal to hmm
	 */
	public abstract boolean compare(BufferedImage image1, BufferedImage image2);

	public abstract boolean compareOld(BufferedImage image1, BufferedImage image2, Pixel[] indices);

	public abstract boolean compare(BufferedImage image1, BufferedImage image2, Map<Integer, Color[]> colors, Pixel[] indices);

	public abstract List<Pixel> findSimilarities(BufferedImage image1, BufferedImage image2, Pixel[] indices);

	public abstract boolean compare(BufferedImage image, Pixel[] mask, Color[] colors);

	public abstract boolean compareInt(BufferedImage image, Pixel[] mask, Color[] colors);

	public abstract Point findPoint(BufferedImage image, Pixel[] mask, Color[] colors);

}