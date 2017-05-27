package zombieLand;

import java.util.ArrayList;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class Jugador extends Group{
	// Atributos del Jugador
	static Image IMAGEN = new Image(
			Jugador.class.getClassLoader().getResourceAsStream("recursos/survivor-idle_handgun_0.png"));
	static final double ANCHO = 253;// px
	static final double ALTO = 216;// px
	static final double ESCALA = 0.5;// px
	static final double VELOCIDAD = 3;
	
	Polygon areaContacto = new Polygon();
	double velX = 0;
	double velY = 0;
	double dirJugador = 0;
	Double[] ptosContacto = { 84.0, 195.0, 45.0, 150.0, 110.0, 66.0, 181.0, 104.0 };
	static final double PISTOLA_X = 241; // px
	static final double PISTOLA_Y = 160; // px
	
	public Jugador(){
		ImageView ivJugador = new ImageView(IMAGEN);
		ivJugador.setFitWidth(ANCHO * ESCALA);
		ivJugador.setFitHeight(ALTO * ESCALA);
		this.getChildren().add(ivJugador);
		ArrayList<Double> ptosContactoEscalados = new ArrayList<>();
		for (Double coords: ptosContacto)
			ptosContactoEscalados.add(coords * ESCALA);
		areaContacto.getPoints().addAll(ptosContactoEscalados);
		areaContacto.setFill(Color.TRANSPARENT);
		this.getChildren().add(areaContacto);
	}
}
