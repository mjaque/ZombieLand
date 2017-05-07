package zombieLand;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ZombieLand extends Application{

	double VERSION = 0.1;
	double ANCHO = 1024;//px
	double ALTO = 768;//px
	
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage ventana) throws Exception {
		//1.1 Crear la ventana
		ventana.setWidth(ANCHO);
		ventana.setHeight(ALTO);
		ventana.setTitle("ZombieLand " + VERSION);
		ventana.setResizable(false);
		
		//1.2 Crear la escena
		Group raiz = new Group();
		Scene escena = new Scene(raiz, Color.DARKGRAY);
		ventana.setScene(escena);
		
		ventana.show();
	}

}
