package vista;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class ImagenConMarcoRedondo {

	// Método estático para crear un ImageIcon con la imagen redonda
	public static ImageIcon crearImagenConMarcoRedondo(URL recurso) {
	    if (recurso == null) return null;
	    try {
	        // ImageIO.read(URL) sabe leer recursos "file:" o "jar:file:…!/…"
	        BufferedImage original = ImageIO.read(recurso);
	        if (original == null) return null;
	        int width  = original.getWidth();
	        int height = original.getHeight();
	        BufferedImage rounded = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	        Graphics2D g2 = rounded.createGraphics();
	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        g2.setClip(new Ellipse2D.Double(0, 0, width, height));
	        g2.drawImage(original, 0, 0, null);
	        g2.dispose();
	        return new ImageIcon(rounded);
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }
	}

}
