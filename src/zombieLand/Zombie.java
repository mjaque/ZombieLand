package zombieLand;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Zombie extends Group{
	static double ANCHO = 318;// px
	static double ALTO = 294;// px
	static double ESCALA = Jugador.ESCALA;// px
	static double VELOCIDAD = 1;
	static Image IMG = new Image(
			Zombie.class.getClassLoader().getResourceAsStream("recursos/skeleton-attack_0.png"));
	
	ImageView ivZombie = null;
	Rectangle areaContacto = new Rectangle(60, 100, 100, 120);
	int vida = 3;

	public Zombie(){
		ivZombie = new ImageView(IMG);
		// Ajustamos el ivZombie a la escala del zombie
		ivZombie.setFitWidth(ANCHO * ESCALA);
		ivZombie.setFitHeight(ALTO * ESCALA);
		// Ajustamos el área de disparo a la escala del zombie
		areaContacto.setX(areaContacto.getX() * ESCALA);
		areaContacto.setY(areaContacto.getY() * ESCALA);
		areaContacto.setWidth(areaContacto.getWidth() * ESCALA);
		areaContacto.setHeight(areaContacto.getHeight() * ESCALA);
		areaContacto.setFill(Color.TRANSPARENT);
		this.getChildren().add(ivZombie);
		this.getChildren().add(areaContacto);
	}
}
