package vista;

import java.awt.Font;
import java.awt.Image;
import java.io.IOException;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import conexionBD.ConexionSQLite;
import login.Authentification.Main;
import modelo.Comunidad;
import modelo.MatrixManager;
import modelo.Room;
import modelo.Usuario;
import net.miginfocom.swing.MigLayout;

public class DialogoDataRoom extends JDialog {
	Room room;
	String estado="";

	public DialogoDataRoom(Room room) {
		this.room=room;
	    setTitle("Información de sala");

	    Room sala=null;
		try {
			sala = MatrixManager.getRoomByAliasLiteral("#"+room.getAlias()+":matrixserver1.duckdns.org");
		} catch (IOException e) {
			e.printStackTrace();
		}

	    if (sala instanceof Comunidad) {
	        setTitle("Información Comunidad");
	        add(getPanelComunidad(sala));
	    } else {
	        setTitle("Información Proyecto");
	        add(getPanelProyecto(sala));
	    }

	    setModal(true);
	    pack();
	    setLocationRelativeTo(null);
	}

	private JScrollPane getPanelComunidad(Room sala) {
		JPanel panel = new JPanel(new MigLayout("inset 10 10 10 10", "[][]", ""));

		ImageIcon img = sala.getIcon();
		JLabel lblImg = new JLabel();
		lblImg.setIcon(new ImageIcon(img.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
		panel.add(lblImg, "gapbottom  20");

		JLabel lblNombre = new JLabel(sala.getName());
		lblNombre.setFont(new Font("Arial", Font.BOLD, 30));
		panel.add(lblNombre, "gapleft 30,wrap");

		JTextField txtCategoria, txtTecnologias, txtCreator;
		JTextArea txtReglas, txtPropuesta, txtDescripcion;
		String userCreator = null;

		// Creador sala
		panel.add(new JLabel("Creado por:"), "aligny center");
		for (Usuario user : ConexionSQLite.getListaUsers()) {
			if (user.getId().equals(sala.getUserCreator())) {
				userCreator = user.getName();
				break;
			}
		}
		txtCreator = new JTextField(userCreator, 40);
		txtCreator.setEnabled(false);
		panel.add(txtCreator, "wrap");
		// Categoría
		panel.add(new JLabel("Categoría:"), "aligny center");
		txtCategoria = new JTextField(sala.getCategoria(), 40);
		txtCategoria.setEnabled(false);
		panel.add(txtCategoria, "wrap");

		// Descripción
		panel.add(new JLabel("Descripción:"), "aligny top");
		txtDescripcion = new JTextArea(3, 40);
		txtDescripcion.setText(sala.getDescripcion());
		txtDescripcion.setEnabled(false);
		panel.add(new JScrollPane(txtDescripcion), "wrap, growx");

		// Reglas del grupo
		panel.add(new JLabel("Reglas del grupo:"), "aligny top");
		txtReglas = new JTextArea(3, 40);
		txtReglas.setText(sala.getReglas());
		txtReglas.setEnabled(false);
		panel.add(new JScrollPane(txtReglas), "wrap, growx");

		panel.add(new JLabel("Propuesta de proyecto:"), "aligny top");
		txtPropuesta = new JTextArea(3, 40);
		txtPropuesta.setText(sala.getPropuesta());
		txtPropuesta.setEnabled(false);
		panel.add(new JScrollPane(txtPropuesta), "wrap, growx");

		if (room.getUserCreator().equals(Main.getUserMain().getId())) {
			txtCategoria.setEnabled(true);
			txtReglas.setEnabled(true);
			txtPropuesta.setEnabled(true);
			txtDescripcion.setEnabled(true);
			
			JButton btnGuardarCambios=new JButton("Guardar cambios");
			panel.add(btnGuardarCambios, "growx");
			btnGuardarCambios.addActionListener(e->{
				String[] options = { "CONFIRMAR", "CANCELAR" };
				int option = JOptionPane.showOptionDialog(null, "¿Estás seguro de que quieres actualizar los datos de la sala?",
						"Actualizar datos sala", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options,
						options[1]);
				if (option==0) {
					if(MatrixManager.updateRoomData(room)) {
						JOptionPane.showMessageDialog(null, "Datos actualizados correctamente");
					}else {
						JOptionPane.showMessageDialog(null, "Actualizar datos", "Error al actualizar los datos", JOptionPane.WARNING_MESSAGE);
					}
					dispose();
				}else {
					dispose();
				}
			});
		}

		return new JScrollPane(panel);
	}

	private JScrollPane getPanelProyecto(Room sala) {
		JPanel panel = new JPanel(new MigLayout("inset 10 10 10 10", "[][]", ""));

		ImageIcon img = sala.getIcon();
		JLabel lblImg = new JLabel(sala.getIcon());
		lblImg.setIcon(new ImageIcon(img.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
		panel.add(lblImg, "gapbottom  20");

		JLabel lblNombre = new JLabel(sala.getName());
		lblNombre.setFont(new Font("Arial", Font.BOLD, 30));
		panel.add(lblNombre, "gapleft 30,wrap");

		JTextField txtCategoria, txtTecnologias, txtCreator;
		JTextArea txtReglas, txtPropuesta, txtDescripcion;
		String userCreator = null;

		// Creador sala
		panel.add(new JLabel("Creado por:"), "aligny center");
		for (Usuario user : ConexionSQLite.getListaUsers()) {
			if (user.getId().equals(sala.getUserCreator())) {
				userCreator = user.getName();
				break;
			}
		}
		txtCreator = new JTextField(userCreator, 40);
		txtCreator.setEnabled(false);
		panel.add(txtCreator, "wrap");

		// Categoría
		panel.add(new JLabel("Categoría:"), "aligny center");
		txtCategoria = new JTextField(sala.getCategoria(), 40);
		txtCategoria.setEnabled(false);
		panel.add(txtCategoria, "wrap");

		// Descripción
		panel.add(new JLabel("Descripción:"), "aligny top");
		txtDescripcion = new JTextArea(3, 40);
		txtDescripcion.setText(sala.getDescripcion());
		txtDescripcion.setEnabled(false);
		panel.add(new JScrollPane(txtDescripcion), "wrap, growx");

		// Tecnologías/Herramientas
		panel.add(new JLabel("Tecnologías/Herramientas:"), "aligny top");
		txtTecnologias = new JTextField(40);
		txtTecnologias.setText(sala.getTecnologias());
		txtTecnologias.setEnabled(false);
		panel.add(txtTecnologias, "wrap, growx");

		// Estado del proyecto
		panel.add(new JLabel("Estado del proyecto:"), "aligny top");

		ButtonGroup group = new ButtonGroup();

		JRadioButton rbtnInicio = new JRadioButton("Inicio");
		rbtnInicio.addActionListener(e -> estado = "Inicio");
		group.add(rbtnInicio);

		JRadioButton rbtnPlanificación = new JRadioButton("Planificación");
		rbtnPlanificación.addActionListener(e -> estado = "Planificación");
		group.add(rbtnPlanificación);

		JRadioButton rbtnDesarrollo = new JRadioButton("Desarrollo");
		rbtnDesarrollo.addActionListener(e -> estado = "Desarrollo");
		group.add(rbtnDesarrollo);

		JRadioButton rbtnMonitoreo_Control = new JRadioButton("Monitoreo/Control");
		rbtnMonitoreo_Control.addActionListener(e -> estado = "Monitoreo/Control");
		group.add(rbtnMonitoreo_Control);

		JRadioButton rbtnCierre = new JRadioButton("Cierre");
		rbtnCierre.addActionListener(e -> estado = "Cierre");
		group.add(rbtnCierre);

		switch (sala.getEstado()) {
		case "Inicio":
			rbtnInicio.setSelected(true);
			break;
		case "Planificación":
			rbtnPlanificación.setSelected(true);
			break;
		case "Desarrollo":
			rbtnDesarrollo.setSelected(true);
			break;
		case "Monitoreo/Control":
			rbtnMonitoreo_Control.setSelected(true);
			break;
		case "Cierre":
			rbtnCierre.setSelected(true);
		}

		JPanel panelEstadosProyecto = new JPanel();
		panelEstadosProyecto.add(rbtnInicio);
		panelEstadosProyecto.add(rbtnPlanificación);
		panelEstadosProyecto.add(rbtnDesarrollo);
		panelEstadosProyecto.add(rbtnMonitoreo_Control);
		panelEstadosProyecto.add(rbtnCierre);

		rbtnInicio.setEnabled(false);
		rbtnPlanificación.setEnabled(false);
		rbtnDesarrollo.setEnabled(false);
		rbtnMonitoreo_Control.setEnabled(false);
		rbtnCierre.setEnabled(false);

		panel.add(panelEstadosProyecto, "wrap, growx");

		// Reglas del grupo
		panel.add(new JLabel("Reglas del grupo:"), "aligny top");
		txtReglas = new JTextArea(3, 40);
		txtReglas.setText(sala.getReglas());
		txtReglas.setEnabled(false);
		panel.add(new JScrollPane(txtReglas), "wrap, growx");

		panel.add(new JLabel("Propuesta de proyecto:"), "aligny top");
		txtPropuesta = new JTextArea(3, 40);
		txtPropuesta.setText(sala.getPropuesta());
		txtPropuesta.setEnabled(false);
		panel.add(new JScrollPane(txtPropuesta), "wrap, growx");
		
		if (room.getUserCreator().equals(Main.getUserMain().getId())) {
			txtCategoria.setEnabled(true);
			txtReglas.setEnabled(true);
			txtTecnologias.setEnabled(true);
			txtPropuesta.setEnabled(true);
			txtDescripcion.setEnabled(true);
			rbtnInicio.setEnabled(true);
			rbtnPlanificación.setEnabled(true);
			rbtnDesarrollo.setEnabled(true);
			rbtnMonitoreo_Control.setEnabled(true);
			rbtnCierre.setEnabled(true);
			
			JButton btnGuardarCambios=new JButton("Guardar cambios");
			panel.add(btnGuardarCambios, "growx");
			btnGuardarCambios.addActionListener(e->{
				String[] options = { "CONFIRMAR", "CANCELAR" };
				int option = JOptionPane.showOptionDialog(null, "¿Estás seguro de que quieres actualizar los datos de la sala?",
						"Actualizar datos sala", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options,
						options[1]);
				if (option==0) {
					sala.setEstado(estado);
					if(MatrixManager.updateRoomData(sala)) {
						JOptionPane.showMessageDialog(null, "Datos actualizados correctamente");
					}else {
						JOptionPane.showMessageDialog(null, "Actualizar datos", "Error al actualizar los datos", JOptionPane.WARNING_MESSAGE);
					}
				}else {
					dispose();
				}
			});
		}
		
		return new JScrollPane(panel);
	}

}
