/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ahorcadoserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author diego
 */
public class Procesador extends Thread{
	private Servidor servidor;
	private Socket socket;
	private BufferedReader inReader;
	private PrintWriter outPrinter;
	
	// variables del juego
	private String nombre;
	private int puntuacion = 0;
	
	public Procesador(Servidor servidor, Socket socket)
	{
		this.servidor = servidor;
		this.socket = socket;
	}
	
	private void obtenerFlujos(){
		// Obtener los streams
		try{
			inReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outPrinter = new PrintWriter(socket.getOutputStream(), true);
			
		} catch (IOException e){
			System.err.println("Error al obtener los flujos de entrada/salida");
		} catch (Exception e){
			System.err.println("Error en conexión con cliente");
		}
	}
	
	public void resultadoLetra(String letra, int vidas, String palabraDescubierta){
		outPrinter.println("203 LETRA " + letra + " VIDAS " + vidas + " PALABRA " + palabraDescubierta);
	}
	
	public void resultadoPalabra(String palabra, boolean correcta, String adivinador){
		String c = correcta ? "true" : "false";
		outPrinter.println("204 PALABRA " + palabra + " CORRECTA " + c + " POR " + adivinador);
	}
	
	public void terminarPartida(String palabra){
		outPrinter.println("205 END PALABRA " + palabra);
	}
	
	@Override
	public void run(){
		try{
			String peticion, respuesta;

			// Obtener streams
			obtenerFlujos();

			if (inReader != null && outPrinter != null){
				// Obtener nombre de usuario
				boolean salir = false;
				
				do{
					peticion = inReader.readLine();
					
					if (peticion.startsWith("1100 NOMBRE")){
						String nombreSolicitado = peticion.substring(12);
						
						if (servidor.registrarJugador(nombreSolicitado, this)){
							nombre = nombreSolicitado;
							outPrinter.println("300 OK");
							
							System.out.println("Usuario conectado con nombre " + nombre);
						}
						else{
							outPrinter.println("500 ERROR El nombre de usuario ya está en uso");
						}
					}
					else if (peticion.startsWith("1893 LOGOUT")){
						System.out.println("Usuario desconectado");
						salir = true;
						
						outPrinter.println("201 HADIÓS");
					}
					else{
						outPrinter.println("501 ERROR Operación no admitida en este momento");
					}
					
				} while((nombre == null || nombre.length() == 0) && !salir);
				
				// Entramos en la sala de espera
				while (!salir){
					servidor.salaEspera.acquire();
					
					servidor.salirDeSalaEspera(this);

					// Estamos fuera de la sala de espera
					System.out.println("Terminada la espera!!!!!");

					// Mandar START al cliente
					outPrinter.println("202 START " + servidor.getPalabra().length());

					// Recibir letras o palabras del cliente
					boolean partidaTerminada = false;
					do{
						peticion = inReader.readLine();

						if (peticion.startsWith("1200 LETRA")){
							String letra = peticion.substring(11);

							System.out.println("Preguntando letra " + letra);

							// Procesar la letra
							servidor.preguntarLetra(letra);

							outPrinter.println("300 OK");
						}
						else if (peticion.startsWith("1201 PALABRA")){
							String palabra = peticion.substring(13);

							System.out.println("Preguntando palabra " + palabra);

							// Procesar la letra
							servidor.preguntarPalabra(palabra, nombre);

							outPrinter.println("300 OK");
						}
						else if (peticion.startsWith("1101 ENDOK")){
							// Terminar la partida
							partidaTerminada = true;
						}
						else if (peticion.startsWith("1893 LOGOUT")){
							System.out.println(nombre + " se ha desconectado");
							
							servidor.desconectarJugador(this, nombre);
							
							outPrinter.println("201 HADIÓS");
							
							salir = true;
						}
						else{
							outPrinter.println("501 ERROR Operación no admitida en este momento");
						}
					} while (!partidaTerminada && !salir);
					
					if (!salir)
						servidor.entrarEnSalaEspera(this);
				}
				
				socket.close();
			}
		} catch (IOException e){
			System.err.println("Error en la conexión con cliente");
			servidor.desconectarJugador(this, nombre);
		} catch (InterruptedException e){
			System.err.println("Ha pasado algo raro con el semáforo");
			servidor.desconectarJugador(this, nombre);
		}
	}
}
