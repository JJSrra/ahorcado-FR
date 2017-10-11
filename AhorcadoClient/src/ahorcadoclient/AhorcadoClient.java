/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ahorcadoclient;

import GUI.CapturadorNombreIP;
import GUI.Espera;
import GUI.VistaJuego;
import java.net.Socket;

/**
 *
 * @author diego
 */
public class AhorcadoClient {
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		// Mostrar GUI para obtener dirección del servidor
		Cliente cliente = new Cliente();
		VistaJuego vista = new VistaJuego(cliente);
		cliente.setVista(vista);
		
		CapturadorNombreIP capturadorNombreIP = new CapturadorNombreIP(vista, true);
		capturadorNombreIP.setVisible(true);
		
		// Mostrar espera
		cliente.iniciarPartida();
	}
	
}
