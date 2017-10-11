# ahorcado-FR
Juego del ahorcado desarrollado para la asignatura Fundamentos de Redes de la Universidad de Granada (UGR)

Realizado por Juan José Sierra y Diego Iáñez.

Compuesto por código del cliente y del servidor. Los jugadores, que ejecutarán el cliente, deben introducir letras
para adivinar la palabra. Cada vez que un jugador introduce una letra incorrecta, se resta una vida A TODOS los jugadores.
Si un jugador cree que ha adivinado la palabra deberá escribirla en lugar de introducir una sola letra.
  - En caso de acierto, el jugador se lleva un punto.
  - En caso de error, la partida se da por perdida y se reinicia con una nueva palabra.

Para que un jugador se una a la partida, debe hacerlo durante el tiempo de espera que ocurre entre la primera
solicitud para jugar y que finalice el timeout. Si un jugador entra cuando ya se está dando lugar una partida, este
deberá esperar a que la partida termine y entrará a participar en la siguiente.
