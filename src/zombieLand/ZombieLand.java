package zombieLand;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ZombieLand extends Application implements EventHandler {

	double VERSION = 0.1;
	double ANCHO = 1024;// px
	double ALTO = 714;// px
	double ANCHO_JUGADOR = 253;// px
	double ALTO_JUGADOR = 216;// px
	double ESCALA_JUGADOR = 0.5;// px
	double MAXX = ANCHO - ANCHO_JUGADOR * ESCALA_JUGADOR;	//Límite de movimiento
	double MAXY = ALTO - ALTO_JUGADOR * ESCALA_JUGADOR - 25;	//Límite de movimiento
	//(hay 25 px de barra de ventana)
	

	ImageView jugador = null;
	double VELOCIDAD_JUGADOR = 3;
	double velXJugador = 0;
	double velYJugador = 0;
	double dirJugador = 0;
	
	double posMouseX = 0;
	double posMouseY = 0;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage ventana) {
		// 1.1 Crear la ventana
		ventana.setWidth(ANCHO);
		ventana.setHeight(ALTO);
		ventana.setTitle("ZombieLand " + VERSION);
		ventana.setResizable(false);

		// 1.2 Crear la escena
		Group raiz = new Group();
		Scene escena = new Scene(raiz, Color.DARKGRAY);
		ventana.setScene(escena);

		// 2.1 Crear el jugador
		Image imgJugador = new Image(
				this.getClass().getClassLoader().getResourceAsStream("recursos/survivor-idle_handgun_0.png"));
		jugador = new ImageView(imgJugador);
		jugador.setFitWidth(ANCHO_JUGADOR * ESCALA_JUGADOR);
		jugador.setFitHeight(ALTO_JUGADOR * ESCALA_JUGADOR);
		jugador.setX(ANCHO / 2 - (ANCHO_JUGADOR / 2) * ESCALA_JUGADOR);
		jugador.setY(ALTO / 2 - (ALTO_JUGADOR / 2) * ESCALA_JUGADOR);
		raiz.getChildren().add(jugador);

		// 3.1 Recibir eventos
		escena.setOnKeyPressed(this);
		escena.setOnKeyReleased(this);
		escena.setOnMouseMoved(this);
		
		//4.1 Poner punto de mira
		Image imgAim = new Image("recursos/aim.png");
		escena.setCursor(new ImageCursor(imgAim));

		ventana.show();
		
		// 3.2 Creación del Animador
		AnimationTimer anim = new AnimationTimer() {
			
			@Override
			public void handle(long now) {
				animar(now);
			}
		};
		anim.start();
	}
	
	//Método para la animación
	public void animar(long now){
		jugador.setX(jugador.getX() + velXJugador);
		jugador.setY(jugador.getY() + velYJugador);
		
		// Establecemos los límites
		if (jugador.getX() < 0)
			jugador.setX(0);
		if (jugador.getY() < 0)
			jugador.setY(0);
		if (jugador.getX() > MAXX)
			jugador.setX(MAXX);
		if (jugador.getY() > MAXY)
			jugador.setY(MAXY);
		
		// Calculamos la dirección del jugador (hacia el puntero)
		double jugadorCentroX = jugador.getX() + (ANCHO_JUGADOR / 2 * ESCALA_JUGADOR);
		double jugadorCentroY = jugador.getY() + (ALTO_JUGADOR / 2 * ESCALA_JUGADOR);
		double dX = posMouseX - jugador.getX();
		double dY = posMouseY - jugador.getY();
		dirJugador = Math.toDegrees(Math.atan2(dY, dX));
		jugador.setRotate(dirJugador);

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

	}
	

}
