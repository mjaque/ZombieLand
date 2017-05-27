package zombieLand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
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
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

public class ZombieLand extends Application implements EventHandler {

	// Datos del Programa y Tamaño de Pantalla
	double VERSION = 0.1;

	double ANCHO = 1024;// px
	double ALTO = 714;// px

	// Atributos del Escenario
	Group raiz = null;
	AnimationTimer anim = null;

	// Atributos del Jugador
	Group jugador = new Group();
	Polygon jugadorAreaContacto = new Polygon();
	double ANCHO_JUGADOR = 253;// px
	double ALTO_JUGADOR = 216;// px
	double ESCALA_JUGADOR = 0.5;// px
	double VELOCIDAD_JUGADOR = 3;
	double velXJugador = 0;
	double velYJugador = 0;
	double dirJugador = 0;
	Double[] ptosContacto = {84.0,195.0, 45.0,150.0, 110.0,66.0, 181.0, 104.0};
	// Posición de la boca de la pistola en la imagen del jugador
	double PISTOLA_X = 241; // px
	double PISTOLA_Y = 160; // px

	// Límites de movimiento
	double MAXX = ANCHO - ANCHO_JUGADOR * ESCALA_JUGADOR;
	double MAXY = ALTO - ALTO_JUGADOR * ESCALA_JUGADOR - 25;

	// (hay 25 px de barra de ventana)
	// Posición del ratón
	double posMouseX = 0;
	double posMouseY = 0;

	// Atributos de la bala
	ArrayList<ImageView> balas = new ArrayList<>();
	double ANCHO_BALA = 15;// px
	double VELOCIDAD_BALA = 5;

	// Atributos del Zombie
	ArrayList<Group> zombies = new ArrayList<>();
	Long tiempoSalidaUltimoZombie = null;
	Double intervaloSalida = 2.0; // empezamos saliendo cada 2 segundos
	Integer contadorZombies = 0; // cuenta los zombies que han salido
	Integer incrementarCada = 5; // Cada cinco zombies incrementamos la
									// velocidad de salida
	HashMap<Group, Integer> vidaZombies = new HashMap<>();
	double ANCHO_ZOMBIE = 318;// px
	double ALTO_ZOMBIE = 294;// px
	double ESCALA_ZOMBIE = ESCALA_JUGADOR;// px
	double VELOCIDAD_ZOMBIE = 1;
	int VIDA_ZOMBIE = 3;

	// Atributos de la partida
	int puntos = 0;
	Text textoPuntos = null;

	public static void main(String[] args) {
		launch(args);
	}

	private void actualizarPuntos() {
		textoPuntos.setText("Puntos: " + puntos);
	}

	public void animar(long now) {
		jugador.setTranslateX(jugador.getTranslateX() + velXJugador);
		jugador.setTranslateY(jugador.getTranslateY() + velYJugador);

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
		double jugadorCentroX = jugador.getTranslateX() + (ANCHO_JUGADOR / 2 * ESCALA_JUGADOR);
		double jugadorCentroY = jugador.getTranslateY() + (ALTO_JUGADOR / 2 * ESCALA_JUGADOR);

		// calculo de la corrección para apuntar con la pistola
		double h = (PISTOLA_Y - ALTO_JUGADOR / 2) * ESCALA_JUGADOR;
		double px = posMouseX - h * Math.cos(Math.toRadians(90 + jugador.getRotate()));
		double py = posMouseY - h * Math.sin(Math.toRadians(90 + jugador.getRotate()));
		dirJugador = Math.toDegrees(Math.atan2(py - jugadorCentroY, px - jugadorCentroX));
		jugador.setRotate(dirJugador);

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
		// for (ImageView bala : balas){ //Para cada bala de la lista
		Iterator<ImageView> itBalas = balas.iterator();
		while (itBalas.hasNext()) {
			ImageView bala = itBalas.next();
			bala.setX(bala.getX() + VELOCIDAD_BALA);

			// Control de límites (la bala está transformada por rotación)
			if (bala.getBoundsInParent().getMinX() < 0 || bala.getBoundsInParent().getMinY() < 0
					|| bala.getBoundsInParent().getMinX() > ANCHO || bala.getBoundsInParent().getMinY() > ALTO) {
				itBalas.remove();
				raiz.getChildren().remove(bala);
				continue; // no seguimos comprobando balas
			}

			// 6.5 Detectar colisión de disparo con zombies.
			Iterator<Group> itZombie = zombies.iterator();
			while (itZombie.hasNext()) {
				Group zombie = itZombie.next();
				Rectangle zombieAreaDisparo = (Rectangle) zombie.getChildren().get(1);
				Bounds limitesADZombie = zombieAreaDisparo.localToScene(zombieAreaDisparo.getBoundsInParent());
				if (limitesADZombie.contains(bala.getBoundsInParent())) {
					// 6.6 Controlamos vida Zombie y puntos.
					itBalas.remove();
					raiz.getChildren().remove(bala);
					int vida = vidaZombies.get(zombie);
					if (--vida <= 0) {
						// Quitamos el zombie
						raiz.getChildren().remove(zombie);
						vidaZombies.remove(zombie);
						itZombie.remove();
						puntos++;
						actualizarPuntos();
					} else
						vidaZombies.put(zombie, vida);
				}
			}
		}

		//Animación de los zombies
		Iterator<Group> itZombie = zombies.iterator();
		while (itZombie.hasNext()) {
			Group zombie = itZombie.next();
			Rectangle zombieAreaContacto = (Rectangle) zombie.getChildren().get(1);

			// 6.7 Detectar colisión del zombie con jugador
			Bounds limitesACJugador = jugadorAreaContacto.localToScene(jugadorAreaContacto.getBoundsInParent());
			Bounds limitesACZombie = zombieAreaContacto.localToScene(zombieAreaContacto.getBoundsInParent());
			if (limitesACZombie.intersects(limitesACJugador)) {
				gameOver();
			}

			// 6.3 Mover el zombie
			// Calculamos el ángulo de la dirección del zombie
			double dx = jugador.getTranslateX() - zombie.getTranslateX();
			double dy = jugador.getTranslateY() - zombie.getTranslateY();
			double angulo = Math.atan2(dy, dx);
			zombie.setTranslateX(zombie.getTranslateX() + VELOCIDAD_ZOMBIE * Math.cos(angulo));
			zombie.setTranslateY(zombie.getTranslateY() + VELOCIDAD_ZOMBIE * Math.sin(angulo));

			// 6.4 Rotamos el zombie hacia el jugador
			zombie.setRotate(Math.toDegrees(angulo));
		}

	}

	private void crearBala() {
		// 5.1 Crear la bala
		Image imgBala = new Image(this.getClass().getClassLoader().getResourceAsStream("recursos/bala.png"));
		ImageView bala = new ImageView(imgBala);
		balas.add(bala); // La añadimos a la lista de balas
		bala.setFitWidth(ANCHO_BALA);
		bala.setPreserveRatio(true);
		bala.setVisible(false);
		// Colocamos la bala
		bala.setX(jugador.getTranslateX() + PISTOLA_X * ESCALA_JUGADOR);
		bala.setY(jugador.getTranslateY() + PISTOLA_Y * ESCALA_JUGADOR);
		bala.getTransforms().clear();
		// La giramos respecto al centro del jugador
		double jugadorCentroX = jugador.getTranslateX() + (ANCHO_JUGADOR / 2 * ESCALA_JUGADOR);
		double jugadorCentroY = jugador.getTranslateY() + (ALTO_JUGADOR / 2 * ESCALA_JUGADOR);
		bala.getTransforms().add(new Rotate(jugador.getRotate(), jugadorCentroX, jugadorCentroY));
		bala.setVisible(true);

		raiz.getChildren().add(bala);
	}

	public void crearZombie() {
		Group zombie = null;
		ImageView ivZombie = null;
		Rectangle zombieAreaDisparo = new Rectangle(60, 100, 100, 120);

		// 2.1 Crear el zombie
		zombie = new Group();
		Image imgZombie = new Image(
				this.getClass().getClassLoader().getResourceAsStream("recursos/skeleton-attack_0.png"));
		ivZombie = new ImageView(imgZombie);
		// Ajustamos el ivZombie a la escala del zombie
		ivZombie.setFitWidth(ANCHO_ZOMBIE * ESCALA_ZOMBIE);
		ivZombie.setFitHeight(ALTO_ZOMBIE * ESCALA_ZOMBIE);
		// Ajustamos el área de disparo a la escala del zombie
		zombieAreaDisparo.setX(zombieAreaDisparo.getX() * ESCALA_ZOMBIE);
		zombieAreaDisparo.setY(zombieAreaDisparo.getY() * ESCALA_ZOMBIE);
		zombieAreaDisparo.setWidth(zombieAreaDisparo.getWidth() * ESCALA_ZOMBIE);
		zombieAreaDisparo.setHeight(zombieAreaDisparo.getHeight() * ESCALA_ZOMBIE);
		zombieAreaDisparo.setFill(Color.TRANSPARENT);
		zombie.getChildren().add(ivZombie);
		zombie.getChildren().add(zombieAreaDisparo);

		// Ponemos al zombie en posición inicial aleatoria
		double aleatorio = Math.random();
		if (aleatorio < 0.25) {
			zombie.setTranslateX(ANCHO * Math.random());
			zombie.setTranslateY(0);
		} else if (aleatorio < 0.5) {
			zombie.setTranslateX(ANCHO);
			zombie.setTranslateY(ALTO * Math.random());
		} else if (aleatorio < 0.75) {
			zombie.setTranslateX(ANCHO * Math.random());
			zombie.setTranslateY(ALTO);
		} else {
			zombie.setTranslateX(0);
			zombie.setTranslateY(ALTO * Math.random());
		}

		zombies.add(zombie);
		vidaZombies.put(zombie, VIDA_ZOMBIE);
		raiz.getChildren().add(zombie);
		contadorZombies++;

	}

	private void gameOver() {
		anim.stop();
		Text gameOver = new Text("¡GAME OVER!");
		gameOver.setFill(Color.DARKRED);
		gameOver.setFont(new Font("Monospace", 60));
		gameOver.setX(ANCHO / 2 - 200);
		gameOver.setY(ALTO / 2);
		raiz.getChildren().add(gameOver);
	}

	// Método para la gestión de eventos
	@Override
	public void handle(Event event) {
		if (event.getEventType().equals(KeyEvent.KEY_PRESSED)) {
			KeyEvent ke = (KeyEvent) event;

			if (ke.getCode().equals(KeyCode.A))
				velXJugador = -VELOCIDAD_JUGADOR;
			if (ke.getCode().equals(KeyCode.D))
				velXJugador = +VELOCIDAD_JUGADOR;
			if (ke.getCode().equals(KeyCode.W))
				velYJugador = -VELOCIDAD_JUGADOR;
			if (ke.getCode().equals(KeyCode.S))
				velYJugador = +VELOCIDAD_JUGADOR;
		}
		if (event.getEventType().equals(KeyEvent.KEY_RELEASED)) {
			KeyEvent ke = (KeyEvent) event;

			if (ke.getCode().equals(KeyCode.A) || ke.getCode().equals(KeyCode.D))
				velXJugador = 0;
			if (ke.getCode().equals(KeyCode.W) || ke.getCode().equals(KeyCode.S))
				velYJugador = 0;
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

	@Override
	public void start(Stage ventana) {
		// 1.1 Crear la ventana
		ventana.setWidth(ANCHO);
		ventana.setHeight(ALTO);
		ventana.setTitle("ZombieLand " + VERSION);
		ventana.setResizable(false);

		// 1.2 Crear la escena
		raiz = new Group();
		Scene escena = new Scene(raiz, Color.DARKGRAY);
		ventana.setScene(escena);
		
		// 7.3 Poner fondo a la escena
//		raiz.setStyle("-fx-background-image: url('recursos/suelo_metalico.jpg'); " +
		Image fondo = new Image(
				this.getClass().getClassLoader().getResourceAsStream("recursos/suelo_metalico.jpg"));
		// new BackgroundSize(width, height, widthAsPercentage, heightAsPercentage, contain, cover)
		BackgroundSize backgroundSize = new BackgroundSize(100, 100, true, true, true, false);
		// new BackgroundImage(image, repeatX, repeatY, position, size)
		BackgroundImage backgroundImage = new BackgroundImage(fondo, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.CENTER, backgroundSize);
		// new Background(images...)
		Background background = new Background(backgroundImage);
		Region reg = new Region();
		reg.setMinWidth(ANCHO);
		reg.setMinHeight(ALTO);
		reg.setBackground(background);
		raiz.getChildren().add(reg);

		
		// 2.1 Crear el jugador
		Image imgJugador = new Image(
				this.getClass().getClassLoader().getResourceAsStream("recursos/survivor-idle_handgun_0.png"));
		ImageView ivJugador = new ImageView(imgJugador);
		ivJugador.setFitWidth(ANCHO_JUGADOR * ESCALA_JUGADOR);
		ivJugador.setFitHeight(ALTO_JUGADOR * ESCALA_JUGADOR);
		jugador.getChildren().add(ivJugador);
		ArrayList<Double> ptosContactoEscalados = new ArrayList<>();
		for (Double coords: ptosContacto)
			ptosContactoEscalados.add(coords * ESCALA_JUGADOR);
		jugadorAreaContacto.getPoints().addAll(ptosContactoEscalados);
		jugadorAreaContacto.setFill(Color.TRANSPARENT);
		jugador.getChildren().add(jugadorAreaContacto);
		jugador.setTranslateX(ANCHO / 2 - (ANCHO_JUGADOR / 2) * ESCALA_JUGADOR);
		jugador.setTranslateY(ALTO / 2 - (ALTO_JUGADOR / 2) * ESCALA_JUGADOR);
		
		raiz.getChildren().add(jugador);

		// 3.1 Recibir eventos
		escena.setOnKeyPressed(this);
		escena.setOnKeyReleased(this);
		escena.setOnMouseMoved(this);
		escena.setOnMouseClicked(this);

		// 4.1 Poner punto de mira
		Image imgAim = new Image("recursos/aim.png");
		escena.setCursor(new ImageCursor(imgAim));

		// 6.6 Ponemos el Texto con los puntos
		textoPuntos = new Text();
		textoPuntos.setX(10);
		textoPuntos.setY(30);
		textoPuntos.setFill(Color.WHITE);
		textoPuntos.setFont(new Font(18));
		actualizarPuntos();
		raiz.getChildren().add(textoPuntos);

		ventana.show();

		// 3.2 Creación del Animador
		anim = new AnimationTimer() {

			@Override
			public void handle(long now) {
				animar(now);
			}
		};
		anim.start();
	}

}
