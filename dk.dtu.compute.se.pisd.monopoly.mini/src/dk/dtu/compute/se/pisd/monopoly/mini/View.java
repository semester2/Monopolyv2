package dk.dtu.compute.se.pisd.monopoly.mini;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import dk.dtu.compute.se.pisd.designpatterns.Observer;
import dk.dtu.compute.se.pisd.designpatterns.Subject;
import dk.dtu.compute.se.pisd.monopoly.mini.model.Game;
import dk.dtu.compute.se.pisd.monopoly.mini.model.Player;
import dk.dtu.compute.se.pisd.monopoly.mini.model.Space;
import gui_fields.GUI_Car;
import gui_fields.GUI_Car.Pattern;
import gui_fields.GUI_Car.Type;
import gui_fields.GUI_Field;
import gui_fields.GUI_Player;
import gui_main.GUI;

/**
 * This class implements a view on the Monopoly game based
 * on the original Matador GUI; it serves as a kind of
 * adapter to the Matador GUI. This class realizes the
 * MVC-principle on top of the Matador GUI. In particular,
 * the view implements an observer of the model in the
 * sense of the MVC-principle, which updates the GUI when
 * the state of the game (model) changes.
 * 
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 */
public class View implements Observer {

	private Game game;
	private GUI gui;
	
	private Map<Player,GUI_Player> player2GuiPlayer = new HashMap<Player,GUI_Player>();
	private Map<Player,Integer> player2position = new HashMap<Player,Integer>();
	private Map<Space,GUI_Field> space2GuiField = new HashMap<Space,GUI_Field>();
	
	private boolean disposed = false;
	
	/**
	 * Constructor for the view of a game based on a game and an already
	 * running Matador GUI.
	 * 
	 * @param game the game
	 * @param gui the GUI
	 */
	public View(Game game, GUI gui) {
		this.game = game;
		this.gui = gui;
		GUI_Field[] guiFields = gui.getFields();
		
		int i = 0;
		for (Space space: game.getSpaces()) {
			// TODO, here we assume that the games fields fit to the GUI's fields;
			// the GUI fields should actually be created according to the games
			// fields
			space2GuiField.put(space, guiFields[i++]);
			
			// TODO we should also register with the properties as observer; but
			// the current version does not update anything for the spaces, so we do not
			// register the view as an observer for now
		}
		
		// create the players in the GUI
		for (Player player: game.getPlayers()) {
			GUI_Car car = new GUI_Car(player.getColor(), Color.black, Type.CAR, Pattern.FILL);
			GUI_Player guiPlayer = new GUI_Player(player.getName(), player.getBalance(), car);
			player2GuiPlayer.put(player, guiPlayer);
			gui.addPlayer(guiPlayer);
			// player2position.put(player, 0);
			
			// register this view with the player as an observer, in order to update the
			// player's state in the GUI
			player.attach(this);
			
			updatePlayer(player);
		}
	}
	
	@Override
	public void update(Subject subject) {
		if (!disposed) {
			if (subject instanceof Player) {
				updatePlayer((Player) subject);
			}
			
			// TODO update other subjects in the GUI
			//      in particular properties (sold, houses, ...)
			
		}
	}
	
	/**
	 * This method updates a player's state in the GUI. Right now, this
	 * concerns the players position and balance only. But, this should
	 * also include other information (being i prison, available cards,
	 * ...)
	 * 
	 * @param player the player who's state is to be updated
	 */
	private void updatePlayer(Player player) {
		GUI_Player guiPlayer = this.player2GuiPlayer.get(player);
		if (guiPlayer != null) {
			guiPlayer.setBalance(player.getBalance());

			GUI_Field[] guiFields = gui.getFields();
			Integer oldPosition = player2position.get(player);
			if (oldPosition != null && oldPosition < guiFields.length) {
				guiFields[oldPosition].setCar(guiPlayer, false);
			}
			int pos = player.getCurrentPosition().getIndex();
			if (pos < guiFields.length) {
				player2position.put(player,pos);
				guiFields[pos].setCar(guiPlayer, true);
			}
			 
			if (player.isBroke()) {
				guiPlayer.setName(player.getName() + " (broke)");
			} else if (player.isInPrison()) {
				guiPlayer.setName(player.getName() + " (in prison)");
			} else {
				guiPlayer.setName(player.getName());
			}
		}
	}
	
	void dispose() {
		if (!disposed) {
			disposed = true;
			for (Player player: game.getPlayers()) {
				// unregister from the player as observer
				player.detach(this);
			}
		}
	}

}
