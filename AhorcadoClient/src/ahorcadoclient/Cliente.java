/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ahorcadoclient;

import GUI.Espera;
import GUI.VistaJuego;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Semaphore;

/**
 *
 * @author diego
 */
public class Cliente {
	private int port = 1893;
	private Socket socket;
	private BufferedReader inReader;
	private PrintWriter outPrinter;
	private boolean conectado = false;
	private VistaJuego vista;
	
	private boolean hayEscuchaPartida = false;
	public Semaphore esperaDesconexion = new Semaphore(0);
	
	private int numLetras;
	private int vidas;
	private String nombre;
	
	public void setVista(VistaJuego vista){
		this.vista = vista;
	}
	
	public boolean estaConectado(){
		return conectado;
	}
	
	public int getNumLetras(){
		return numLetras;
	}
	
	public int getVidas(){
		return vidas;
	}
	
	public String getNombre(){
		return nombre;
	}
	
	public boolean conectar(String host){
		try{
			socket = new Socket(host, port);
			
			inReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outPrinter = new PrintWriter(socket.getOutputStream(), true);
			
			conectado = true;
		}
		catch (Exception e){
			conectado = false;
		}
		
		return conectado;
	}
	
	public void iniciarPartida(){
		Espera espera = new Espera(vista, false);
		espera.setVisible(true);
		
		esperarInicioPartida();
		
		espera.dispose();
		
		vista.nuevoJuego();
		vista.setVisible(true);
	}
	
	public boolean solicitarNombre(String nombre){
		String respuesta;
		
		try{
			outPrinter.println("1100 NOMBRE " + nombre);
			respuesta = inReader.readLine();
			
		} catch (IOException e){
			conectado = false;
			return false;
		}
		
		if (respuesta.equals("300 OK"))
			this.nombre = nombre;
		
		return respuesta.equals("300 OK");
	}
	
	public void esperarInicioPartida(){
		String respuesta;
		try{
			do{
				respuesta = inReader.readLine();
			} while(!respuesta.startsWith("202 START "));
			
			numLetras = Integer.parseInt(respuesta.substring(10));
			vidas = 6;
			
		} catch (IOException e){
			conectado = false;
		}
		
	}
	
	/**
	 * Inicia un hilo que escucha mensajes del servidor con las palabras
	 * y letras que proponen otros jugadores y actualiza la vista de
	 * juego correspondientemente.
	 * 
	 * @param vista La vista de juego a actualizar.
	 */
	public void iniciarEscuchaPartida(VistaJuego vista){
		EscuchaPartida escucha = new EscuchaPartida(this, inReader, outPrinter, vista);
		escucha.start();
	}
	
	synchronized public void setHayEscuchaPartida(boolean hay){
		hayEscuchaPartida = hay;
	}
	
	public void proponerLetra(String letra){
		String respuesta;
		
		//try{
			//do{
				outPrinter.println("1200 LETRA " + letra.toLowerCase());
			//	respuesta = inReader.readLine();
			//} while(!respuesta.startsWith("300 OK"));
			
		//} catch (IOException e){
		//	conectado = false;
		//}
	}
	
	public void proponerPalabra(String palabra){
		String respuesta;
		
		//try{
			//do{
				outPrinter.println("1201 PALABRA " + palabra.toLowerCase());
			//	respuesta = inReader.readLine();
			//} while(!respuesta.startsWith("300 OK"));
			
		//} catch (IOException e){
		//	conectado = false;
		//}
	}
	
	public void terminarPartida(){
		// Enviar ENDOK
		outPrinter.println("1101 ENDOK");
		
		// Iniciar la siguiente partida
		vista.setVisible(false);
		iniciarPartida();
	}
	
	synchronized public void desconectarse(){
		try{
			if (conectado){
				outPrinter.println("1893 LOGOUT");
				
				if (!hayEscuchaPartida){
					String respuesta;

					do{
						respuesta = inReader.readLine();
					} while (!respuesta.startsWith("201 HADIÓS"));
				}
				else{
					// Esperar a que escucha partida lea el HADIÓS
					esperaDesconexion.acquire();
				}
				
				socket.close();
			}
		}catch (IOException e){
			System.err.println("Error al desconectar");
		}catch (InterruptedException e){
			System.err.println("Error al desconectar por semáforo");
		}
		
		conectado = false;
	}
}
