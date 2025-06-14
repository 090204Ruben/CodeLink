package vista;

import java.awt.CardLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import net.miginfocom.swing.MigLayout;

public class Home extends JPanel {
	private static Home main;
	public static CardLayout layout = new CardLayout();
	public static JPanel panel = new JPanel(layout);

	private Home() {
		setLayout(new MigLayout("fillx, filly", "0[100%]0", "0[100%]0"));

		// Crear los paneles
		PanelHome panelHome= new PanelHome();		
		panel.add(panelHome, "Panel Home");
		PanelLeft panelLeft = new PanelLeft(this);
//		PanelRight panelRight = new PanelRight();
		
		// Configurar el JSplitPane para PanelLeft y PanelChat
		JSplitPane leftAndChatSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelLeft, panel);
		leftAndChatSplitPane.setDividerLocation(300); // Fija el ancho inicial del panel izquierdo
		leftAndChatSplitPane.setResizeWeight(0.3); // Ajusta la proporción al redimensionar
		leftAndChatSplitPane.setContinuousLayout(true);
		leftAndChatSplitPane.setOneTouchExpandable(true);

		leftAndChatSplitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, e -> {
			int dividerLocation = leftAndChatSplitPane.getDividerLocation();
			if (dividerLocation > 300) { // Límite máximo del panel izquierdo
				leftAndChatSplitPane.setDividerLocation(300);
			}
		});

		// Panel intermedio para agregar el JSplitPane y el PanelRightA
		JPanel mainPanel = new JPanel(new MigLayout("fill,insets 0", "0[grow, push]0[pref]0", "0[grow, push]0"));
		mainPanel.add(leftAndChatSplitPane, "grow, push");
//		mainPanel.add(panelRight, "grow,push,span");

		// Añadir el contenedor intermedio al diseño principal
		add(mainPanel, "grow, push");
	}
	
	public static Home getInstance() {
		if (main==null) {
			main=new Home();
		}
		return main;
	}
}
