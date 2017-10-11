/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ahorcadoserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import static java.nio.file.Files.readAllLines;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author diego
 */
public class Servidor {
	private static int TIEMPO = 15000;
	
	private int port = 1893;
	private ServerSocket serverSocket;
	private HashSet<String> nombres = new HashSet();
	private HashSet<Procesador> clientesSalaEspera = new HashSet();
	private HashSet<Procesador> clientesJugando = new HashSet();
	
	public Semaphore salaEspera = new Semaphore(0);
	
	// Variables del juego
	String[] diccionario;
	private String palabra;
	private String palabraDescubierta;
	private int vidas = 6;
	private String letrasPreguntadas;
	
	public void start(){
		try{
			// Abrir socket
			serverSocket = new ServerSocket(port);
			
			System.out.println("Servidor escuchando en el puerto " + port);
			
			Socket socketCliente;
			
			while (true){
				socketCliente = serverSocket.accept();

				System.out.println("Recibida conexión de un cliente");
				
				Procesador procesador = new Procesador(this, socketCliente);
				procesador.start();
			}
			
		} catch (IOException e){
			System.err.println("Error al escuchar en el puerto " + port);
		}
	}
	
	synchronized public boolean registrarJugador(String nombre, Procesador cliente){
		if (nombres.add(nombre)){
			// Si es el primer jugador, ponemos un temporizador para la sala de espera de 20 segundos
			if (clientesSalaEspera.size() == 0 && clientesJugando.size() == 0){
				Temporizador temporizador = new Temporizador(this, TIEMPO);
				temporizador.start();
			}
			
			// Guardar una referencia al hilo del jugador para más adelante
			clientesSalaEspera.add(cliente);
			
			return true;
		}
		else
			return false;
	}
	
	synchronized public void desconectarJugador(Procesador cliente, String nombre){
		// Sacarlo de clientesJugando
		clientesJugando.remove(cliente);
							
		// Borrar su nombre de la lista de nombres ocupados
		if (nombre != null)
			nombres.remove(nombre);
		
		// Terminar la partida si no quedan jugadores
		if (clientesJugando.isEmpty())
			terminarPartida();
	}
	
	public void seAcabóElTiempo(){
		// Comprobar que no se haya ido la gente
		if (clientesSalaEspera.size() == 0 && clientesJugando.size() == 0){
			Temporizador temporizador = new Temporizador(this, TIEMPO);
			temporizador.start();
		}
		else{
			// Pensar en una palabra
			inicializarPartida();
			
			int numClientes = clientesSalaEspera.size();
			
			for (int i = 0; i < numClientes; ++i){
				salaEspera.release();
			}
		}
	}
	
	synchronized public void salirDeSalaEspera(Procesador cliente){
		clientesSalaEspera.remove(cliente);
		clientesJugando.add(cliente);
	}
	
	private void inicializarPartida(){
		vidas = 6;
		
		// Elegir una palabra del diccionario
		Random generator = new Random();
		palabra = diccionario[generator.nextInt(diccionario.length)].toLowerCase();
				
		letrasPreguntadas = "";
		palabraDescubierta = "";
		for (int i = 0; i < palabra.length(); ++i)
			palabraDescubierta += "_";
	}
	
	public String getPalabra(){
		return palabraDescubierta;
	}
	
	synchronized public void preguntarLetra(String letra){
		if (palabra.contains(letra) && !letrasPreguntadas.contains(letra)){
			// La letra es correcta
			letrasPreguntadas += letra;
			StringBuilder nuevaPalabraDescubierta = new StringBuilder(palabraDescubierta);
			
			for (int i = 0; i < palabra.length(); ++i){
				if (palabra.charAt(i) == letra.charAt(0))
					nuevaPalabraDescubierta.setCharAt(i, letra.charAt(0));
			}
			
			palabraDescubierta = nuevaPalabraDescubierta.toString();
		}
		else{
			// Quitar vidas
			vidas--;
			
			if (vidas == 0){
				terminarPartida();
			}
		}
		
		for (Procesador cliente : clientesJugando){
			cliente.resultadoLetra(letra, vidas, palabraDescubierta);
		}
	}
	
	synchronized public void preguntarPalabra(String palabra, String adivinador){
		boolean correcta = palabra.equals(this.palabra);
		
		for (Procesador cliente : clientesJugando){
			cliente.resultadoPalabra(palabra, correcta, adivinador);
		}
		
		if (correcta)
			terminarPartida(); 
	}
	
	private void terminarPartida(){
		for (Procesador cliente : clientesJugando){
			cliente.terminarPartida(palabra);
		}
		
		clientesJugando.clear();
		
		System.out.println("Partida terminada");
		
		// Empezar otra sala de espera
		Temporizador temporizador = new Temporizador(this, TIEMPO);
		temporizador.start();
	}
	
	synchronized public void entrarEnSalaEspera(Procesador cliente){
		clientesSalaEspera.add(cliente);
	}
	
	// Dicccionario
	public Servidor(){
		diccionario = new String[] {
			"Actinio",
			"Aluminio",
			"Americio",
			"Antimonio",
			"Argon",
			"Arsenico",
			"Astato",
			"Azufre",
			"Bario",
			"Berilio",
			"Berkelio",
			"Bismuto",
			"Bohrio",
			"Boro",
			"Bromo",
			"Cadmio",
			"Calcio",
			"Californio",
			"Carbono",
			"Cerio",
			"Cesio",
			"Cinc",
			"Circonio",
			"Cloro",
			"Cobalto",
			"Cobre",
			"Copernicio",
			"Cromo",
			"Curio",
			"Darmstadtio",
			"Disprosio",
			"Dubnio",
			"Einsteinio",
			"Erbio",
			"Escandio",
			"Estano",
			"Estroncio",
			"Europio",
			"Fermio",
			"Flerovio",
			"Fluor",
			"Fosforo",
			"Francio",
			"Gadolinio",
			"Galio",
			"Germanio",
			"Hafnio",
			"Hassio",
			"Helio",
			"Hidrogeno",
			"Hierro",
			"Holmio",
			"Indio",
			"Iridio",
			"Kripton",
			"Lantano",
			"Lawrencio",
			"Litio",
			"Livermorio",
			"Lutecio",
			"Magnesio",
			"Manganeso",
			"Meitnerio",
			"Mendelevio",
			"Mercurio",
			"Molibdeno",
			"Neodimio",
			"Neon",
			"Neptunio",
			"Niobio",
			"Niquel",
			"Nitrogeno",
			"Nobelio",
			"Oro",
			"Osmio",
			"Oxigeno",
			"Paladio",
			"Plata",
			"Platino",
			"Plomo",
			"Plutonio",
			"Polonio",
			"Potasio",
			"Praseodimio",
			"Prometio",
			"Protactinio",
			"Radio",
			"Radon",
			"Renio",
			"Rodio",
			"Roentgenio",
			"Rubidio",
			"Rutenio",
			"Rutherfordio",
			"Samario",
			"Seaborgio",
			"Selenio",
			"Silicio",
			"Sodio",
			"Talio",
			"Tantalo",
			"Tecnecio",
			"Teluro",
			"Terbio",
			"Titanio",
			"Torio",
			"Tulio",
			"Ununoctio",
			"Ununpentio",
			"Ununseptio",
			"Ununtrio",
			"Uranio",
			"Vanadio",
			"Wolframio",
			"Xenon",
			"Yodo",
			"Yterbio",
			"Ytrio"
		};
	}
}
