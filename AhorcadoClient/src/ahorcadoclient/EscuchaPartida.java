/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ahorcadoclient;

import GUI.VistaJuego;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author diego
 */
public class EscuchaPartida extends Thread{
	private Cliente cliente;
	private VistaJuego vista;
	private BufferedReader inReader;
	private PrintWriter outPrinter;
	
	public EscuchaPartida(Cliente cliente, BufferedReader inReader, PrintWriter outPrinter, VistaJuego vista){
		this.cliente = cliente;
		this.inReader = inReader;
		this.outPrinter = outPrinter;
		this.vista = vista;
	}
	
	@Override
	public void run(){
		String entrada;
		String[] respuesta;
		
		boolean terminada = false;
		boolean salir = false;
		boolean partidaGanada = false;
		String ganador = "", palabra = "";
		
		cliente.setHayEscuchaPartida(true);
		try{
			do{
				entrada = inReader.readLine();
				
				respuesta = entrada.split(" ");
				
				if (respuesta[0].equals("203")){
					// Una letra
					if (respuesta[6].contains(respuesta[2])){
						// La letra era correcta
						vista.actualizarPalabra(respuesta[6]);
					}
					else{
						// La letra no era correcta
						vista.nuevaLetraNoValida(respuesta[2]);
					}
					
					vista.actualizarVidas(respuesta[4]);
				}
				else if (respuesta[0].equals("204")){
					// Una palabra
					if (respuesta[4].equals("true")){
						// Avisar al jugador de quién ha ganado
						vista.actualizarPalabra(respuesta[2]);
						partidaGanada = true;
						ganador = respuesta[6];
						palabra = respuesta[2];
					}
					else
						vista.nuevaPalabraNoValida(respuesta[2]);
				}
				else if (respuesta[0].equals("205")){
					terminada = true;
					palabra = respuesta[3];
				}
				else if (respuesta[0].equals("201")){
					// HADIÓS
					salir = true;
				}
				
			} while (!terminada && !salir);
			
			if (!salir){
				cliente.setHayEscuchaPartida(false);
				if (partidaGanada)
					vista.terminarPartida(ganador, palabra);
				else
					vista.terminarPartida(palabra);
			}
			else{
				// Decirle al cliente que ya puede dejar de esperar
				cliente.esperaDesconexion.release();
				cliente.setHayEscuchaPartida(false);
			}
			
		}catch (IOException e){
			System.err.println("Error en la conexión con el servidor");
		}
	}
}
