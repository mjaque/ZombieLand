package zombieLand;

import java.util.ArrayList;
import java.util.Iterator;

import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;

/**
 * Primera escena de combate libre.
 * En un recinto cerrado van saliendo zombies cada vez más deprisa.
 *
 */
public class Escena extends Scene implements EventHandler<Event> {
	
	// Atributos de la Escena
	static final Group RAIZ = new Group();
	static final Image FONDO = new Image(
			Escena.class.getClassLoader().getResourceAsStream("recursos/suelo_metalico.jpg"));
	static final Image PUNTO_MIRA = new Image(Escena.class.getClassLoader().getResourceAsStream("recursos/aim.png"));
	
	AnimationTimer anim = null;
	
	Jugador jugador = new Jugador();

	// Límites de movimiento
	double MAXX = ZombieLand.ANCHO - Jugador.ANCHO * Jugador.ESCALA;
	double MAXY = ZombieLand.ALTO - Jugador.ALTO * Jugador.ESCALA - 25;

	// (hay 25 px de barra de ventana)
	// Posición del ratón
	double posMouseX = 0;
	double posMouseY = 0;

	// Atributos de la bala
	ArrayList<Bala> balas = new ArrayList<>();

	// Atributos de Zombies
	ArrayList<Zombie> zombies = new ArrayList<>();
	Long tiempoSalidaUltimoZombie = null;
	Double intervaloSalida = 2.0; // empezamos saliendo cada 2 segundos
	Integer contadorZombies = 0; // cuenta los zombies que han salido
	Integer incrementarCada = 5; // Cada cinco zombies incrementamos la
									// velocidad de salida

	// Atributos de la partida
	int puntos = 0;
	Text textoPuntos = null;

	public Escena(){
		super(RAIZ);
		
		// 7.3 Poner fondo a la escena
		// new BackgroundSize(width, height, widthAsPercentage, heightAsPercentage, contain, cover)
		BackgroundSize backgroundSize = new BackgroundSize(100, 100, true, true, true, false);
		// new BackgroundImage(image, repeatX, repeatY, position, size)
		BackgroundImage backgroundImage = new BackgroundImage(FONDO, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.CENTER, backgroundSize);
		// new Background(images...)
		Background background = new Background(backgroundImage);
		Region reg = new Region();
		reg.setMinWidth(ZombieLand.ANCHO);
		reg.setMinHeight(ZombieLand.ALTO);
		reg.setBackground(background);
		RAIZ.getChildren().add(reg);

		//Añadimos el jugador al centro de la escena.
		RAIZ.getChildren().add(jugador);
		jugador.setTranslateX(ZombieLand.ANCHO / 2 - (Jugador.ANCHO / 2) * Jugador.ESCALA);
		jugador.setTranslateY(ZombieLand.ALTO / 2 - (Jugador.ALTO / 2) * Jugador.ESCALA);


		// 3.1 Recibir eventos
		this.setOnKeyPressed(this);
		this.setOnKeyReleased(this);
		this.setOnMouseMoved(this);
		this.setOnMouseClicked(this);

		// 4.1 Poner punto de mira
		this.setCursor(new ImageCursor(PUNTO_MIRA));

		// 6.6 Ponemos el Texto con los puntos
		textoPuntos = new Text();
		textoPuntos.setX(10);
		textoPuntos.setY(30);
		textoPuntos.setFill(Color.WHITE);
		textoPuntos.setFont(new Font(18));
		actualizarPuntos();
		RAIZ.getChildren().add(textoPuntos);

		// 3.2 Creación del Animador
		anim = new AnimationTimer() {

			@Override
			public void handle(long now) {
				animar(now);
			}
		};
		anim.start();

	}
	
	private void actualizarPuntos() {
		textoPuntos.setText("Puntos: " + puntos);
	}

	public void animar(long now) {
		jugador.setTranslateX(jugador.getTranslateX() + jugador.velX);
		jugador.setTranslateY(jugador.getTranslateY() + jugador.velY);

		// Establecemos los límites
		if (jugador.getTranslateX() < 0)
			jugador.setTranslateX(0);
		if (jugador.getTranslateY() < 0)
			jugador.setTranslateY(0);
		if (jugador.getTranslateX() > MAXX)
			jugador.setTranslateX(MAXX);
		if (jugador.getTranslateY() > MAXY)
			jugador.setTranslateY(MAXY);

		// Calculamos la dirección del jugador (hacia el puntero), respecto a la
		// boca de la pistola
		double jugadorCentroX = jugador.getTranslateX() + (Jugador.ANCHO / 2 * Jugador.ESCALA);
		double jugadorCentroY = jugador.getTranslateY() + (Jugador.ALTO / 2 * Jugador.ESCALA);

		// calculo de la corrección para apuntar con la pistola
		double h = (Jugador.PISTOLA_Y - Jugador.ALTO / 2) * Jugador.ESCALA;
		double px = posMouseX - h * Math.cos(Math.toRadians(90 + jugador.getRotate()));
		double py = posMouseY - h * Math.sin(Math.toRadians(90 + jugador.getRotate()));
		jugador.dirJugador = Math.toDegrees(Math.atan2(py - jugadorCentroY, px - jugadorCentroX));
		jugador.setRotate(jugador.dirJugador);

		// 7.2 Multiples zombies
		// salida de nuevos zombies
		if (tiempoSalidaUltimoZombie == null) {
			crearZombie();
			tiempoSalidaUltimoZombie = now;
		} else {
			if (now > tiempoSalidaUltimoZombie + intervaloSalida * 1E9) {
				crearZombie();
				tiempoSalidaUltimoZombie = now;
			}
		}
		if (contadorZombies % incrementarCada == 0) {
			intervaloSalida *= 0.9;
			crearZombie(); // para evitar que se repita
		}

		// 7.1 Animación de las balas
		Iterator<Bala> itBalas = balas.iterator();
		while (itBalas.hasNext()) {
			ImageView bala = itBalas.next();
			bala.setX(bala.getX() + Bala.VELOCIDAD);

			// Control de límites (la bala está transformada por rotación)
			if (bala.getBoundsInParent().getMinX() < 0 || bala.getBoundsInParent().getMinY() < 0
					|| bala.getBoundsInParent().getMinX() > ZombieLand.ANCHO || bala.getBoundsInParent().getMinY() > ZombieLand.ALTO) {
				itBalas.remove();
				RAIZ.getChildren().remove(bala);
				continue; // no seguimos comprobando balas
			}

			// 6.5 Detectar colisión de disparo con zombies.
			Iterator<Zombie> itZombie = zombies.iterator();
			while (itZombie.hasNext()) {
				Zombie zombie = itZombie.next();
				Rectangle zombieAreaDisparo = (Rectangle) zombie.getChildren().get(1);
				Bounds limitesADZombie = zombieAreaDisparo.localToScene(zombieAreaDisparo.getBoundsInParent());
				if (limitesADZombie.contains(bala.getBoundsInParent())) {
					// 6.6 Controlamos vida Zombie y puntos.
					itBalas.remove();
					RAIZ.getChildren().remove(bala);
					if (--zombie.vida <= 0) {
						// Quitamos el zombie
						RAIZ.getChildren().remove(zombie);
						itZombie.remove();
						puntos++;
						actualizarPuntos();
					} 
				}
			}
		}

		//Animación de los zombies
		Iterator<Zombie> itZombie = zombies.iterator();
		while (itZombie.hasNext()) {
			Group zombie = itZombie.next();
			Rectangle zombieAreaContacto = (Rectangle) zombie.getChildren().get(1);

			// 6.7 Detectar colisión del zombie con jugador
			Bounds limitesACJugador = jugador.areaContacto.localToScene(jugador.areaContacto.getBoundsInParent());
			Bounds limitesACZombie = zombieAreaContacto.localToScene(zombieAreaContacto.getBoundsInParent());
			if (limitesACZombie.intersects(limitesACJugador)) {
				gameOver();
			}

			// 6.3 Mover el zombie
			// Calculamos el ángulo de la dirección del zombie
			double dx = jugador.getTranslateX() - zombie.getTranslateX();
			double dy = jugador.getTranslateY() - zombie.getTranslateY();
			double angulo = Math.atan2(dy, dx);
			zombie.setTranslateX(zombie.getTranslateX() + Zombie.VELOCIDAD * Math.cos(angulo));
			zombie.setTranslateY(zombie.getTranslateY() + Zombie.VELOCIDAD * Math.sin(angulo));

			// 6.4 Rotamos el zombie hacia el jugador
			zombie.setRotate(Math.toDegrees(angulo));
		}
	}

	private void crearBala() {
		// 5.1 Crear la bala
		Bala bala = new Bala();
		balas.add(bala); // La añadimos a la lista de balas
		// Colocamos la bala
		bala.setX(jugador.getTranslateX() + Jugador.PISTOLA_X * Jugador.ESCALA);
		bala.setY(jugador.getTranslateY() + Jugador.PISTOLA_Y * Jugador.ESCALA);
		bala.getTransforms().clear();
		// La giramos respecto al centro del jugador
		double jugadorCentroX = jugador.getTranslateX() + (Jugador.ANCHO / 2 * Jugador.ESCALA);
		double jugadorCentroY = jugador.getTranslateY() + (Jugador.ALTO / 2 * Jugador.ESCALA);
		bala.getTransforms().add(new Rotate(jugador.getRotate(), jugadorCentroX, jugadorCentroY));
		bala.setVisible(true);

		RAIZ.getChildren().add(bala);
	}

	public void crearZombie() {
		Zombie zombie = new Zombie();
		
		// Ponemos al zombie en posición inicial aleatoria
		double aleatorio = Math.random();
		if (aleatorio < 0.25) {
			zombie.setTranslateX(ZombieLand.ANCHO * Math.random());
			zombie.setTranslateY(0);
		} else if (aleatorio < 0.5) {
			zombie.setTranslateX(ZombieLand.ANCHO);
			zombie.setTranslateY(ZombieLand.ALTO * Math.random());
		} else if (aleatorio < 0.75) {
			zombie.setTranslateX(ZombieLand.ANCHO * Math.random());
			zombie.setTranslateY(ZombieLand.ALTO);
		} else {
			zombie.setTranslateX(0);
			zombie.setTranslateY(ZombieLand.ALTO * Math.random());
		}

		zombies.add(zombie);
		RAIZ.getChildren().add(zombie);
		contadorZombies++;
	}

	private void gameOver() {
		anim.stop();
		Text gameOver = new Text("¡GAME OVER!");
		gameOver.setFill(Color.DARKRED);
		gameOver.setFont(new Font("Monospace", 60));
		gameOver.setX(ZombieLand.ANCHO / 2 - 200);
		gameOver.setY(ZombieLand.ALTO / 2);
		RAIZ.getChildren().add(gameOver);
	}

	// Método para la gestión de eventos
	@Override
	public void handle(Event event) {
		if (event.getEventType().equals(KeyEvent.KEY_PRESSED)) {
			KeyEvent ke = (KeyEvent) event;

			if (ke.getCode().equals(KeyCode.A))
				jugador.velX = -Jugador.VELOCIDAD;
			if (ke.getCode().equals(KeyCode.D))
				jugador.velX = +Jugador.VELOCIDAD;
			if (ke.getCode().equals(KeyCode.W))
				jugador.velY = -Jugador.VELOCIDAD;
			if (ke.getCode().equals(KeyCode.S))
				jugador.velY = +Jugador.VELOCIDAD;
		}
		if (event.getEventType().equals(KeyEvent.KEY_RELEASED)) {
			KeyEvent ke = (KeyEvent) event;

			if (ke.getCode().equals(KeyCode.A) || ke.getCode().equals(KeyCode.D))
				jugador.velX = 0;
			if (ke.getCode().equals(KeyCode.W) || ke.getCode().equals(KeyCode.S))
				jugador.velY = 0;
		}
		if (event.getEventType().equals(MouseEvent.MOUSE_MOVED)) {
			MouseEvent me = (MouseEvent) event;
			posMouseX = me.getSceneX();
			posMouseY = me.getSceneY();
		}
		if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
			crearBala();
		}

	}


}
