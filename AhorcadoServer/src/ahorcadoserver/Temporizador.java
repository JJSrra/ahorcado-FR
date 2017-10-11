/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ahorcadoserver;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author diego
 */
public class Temporizador extends Thread{
	private Servidor servidor;
	private int tiempo;
	
	public Temporizador(Servidor servidor, int tiempo){
		this.servidor = servidor;
		this.tiempo = tiempo;
	}
	
	public void run(){
		try {
			Thread.sleep(tiempo);
		} catch (InterruptedException ex) {
			System.err.println("Tengo insomnio");
		}
		servidor.seAcabóElTiempo();
	}
}
