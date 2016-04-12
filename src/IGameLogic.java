

import java.io.File;

public interface IGameLogic {
	

	void setMap(File file);
	
	String hello();

	String move(String move, char direction);

	String pickup();

	String look();

	boolean gameRunning();
	
	String quitGame();


}
