package zombieLand;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Bala extends ImageView{
	static final double ANCHO = 15;// px
	static final double VELOCIDAD = 5;
	static final Image IMG = new Image(Bala.class.getClassLoader().getResourceAsStream("recursos/bala.png"));

	public Bala(){
		this.setImage(IMG);
		this.setFitWidth(ANCHO);
		this.setPreserveRatio(true);
		this.setVisible(false);
	}
}
