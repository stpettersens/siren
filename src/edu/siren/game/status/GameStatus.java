package edu.siren.game.status;

import java.io.IOException;

import edu.siren.game.Player;
import edu.siren.game.profile.GameStats;
import edu.siren.game.profile.Profile;
import edu.siren.gui.GuiContainer;
import edu.siren.gui.Image;
import edu.siren.gui.Text;
import edu.siren.renderer.Font;
import edu.siren.renderer.Screen;

public class GameStatus extends GuiContainer {

	// Window Elements 
	private Screen screen;
	
	// Status Information
	private String timeOfDay;
	private int experience;
	private int coins;
	
	// Components
	private Text coinsText;
	private Text timeOfDayText;
	private Text timeOfDayValue;
	private Text experienceText;
	private Text coordinatesText;
	private Image[] experienceImages;
	
	private Profile profile;
	
	public GameStatus(Screen screen, Profile profile) throws IOException {
		
		// Call Super
		super();
		
		// Save the Screen
		this.screen = screen;
		
		// Save the Profile
		this.profile = profile;
		
		// Set Dimensions
		this.dimensions(screen.width, screen.height);
				
		// Build the Game Status
		buildComponents();
		
	}
	
	private void buildComponents() {
		
		try {

			// Set the Font Sprite 
			font = new Font("res/tests/fonts/proggy.png");
			
			// Set Character Name
			Text characterName = new Text(profile.getName());
			characterName.position(2, 430);
			characterName.fontScaling(2);
			add(characterName);
			
			// Set Coins
			Text coinTextLabel = new Text("Coins: ");
			coinTextLabel.position(2, 415);
			coinTextLabel.fontScaling(2);
			add(coinTextLabel);
			coinsText = new Text("0000000");
			coinsText.fontScaling(2);
			coinsText.position(80, 415);
			add(coinsText);
			
			// Create the Status Bottom Panel
			Image statusPanel = new Image("res/game/menu/status-panel.png", "");
			statusPanel.position(2, 4);
			add(statusPanel);
			
			// Create Time of Day Text
			timeOfDayText = new Text("Time of Day");
			timeOfDayText.position(6, 25);
			timeOfDayText.fontScaling(2);
			add(timeOfDayText);
			
			// Create Time of Day
			timeOfDayValue = new Text(getTimeOfDay());
			timeOfDayValue.position(10, 5);
			timeOfDayValue.fontScaling(2);
			add(timeOfDayValue);
			
			// Coordinates
			coordinatesText = new Text("Location");
			coordinatesText.position(screen.width - 225, screen.height - 30);
			coordinatesText.fontScaling(2);
			add(coordinatesText);
			
			// Create Player Level
			experienceText = new Text("Experience");
			experienceText.position(screen.width -125 -2, 25);
			experienceText.fontScaling(2);
			add(experienceText);
			
			experienceImages = new Image[GameStats.MAX_EXPERIENCE];
			int x = 373;
			int y = 2;
			for(int i=0; i<experienceImages.length; i++) {
				
				// Create experience Image
				experienceImages[i] = new Image("res/game/menu/experience.png", "");
				experienceImages[i].position(x, y);
				
				add(experienceImages[i]);
				
				// Increment x Position
				x += 12;
				
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
	}
	
	public void update(GameStats gameStats) {

		// Update Time of Day
		timeOfDayValue.text(getTimeOfDay());

		// Update Experiece
		this.experience = gameStats.getExperience();
		
		// Update Coins
		String coinString = String.format("%07d", profile.getGameStats().getCoins());
		coinsText.text(coinString);
		
		
		Player player = profile.getPlayer();
		this.coordinatesText.text(String.format("(%.02f, %.02f)",player.getRect().x,player.getRect().y));
		
		// Update experience Status
		updateexperience();
		
		
		
		// Draw the World
		draw();

	}
	
	
	private void updateexperience() {
		
		for (int i=0; i<GameStats.MAX_EXPERIENCE; i++) {
			
			if (i < getExperience()) {
				experienceImages[i].show();
			} else {
				experienceImages[i].hide();
			}
			
		}
		
		
	}
	
	
	/**
	 * @return the timeOfDay
	 */
	public String getTimeOfDay() {
		return timeOfDay;
	}

	/**
	 * @param timeOfDay the timeOfDay to set
	 */
	public void setTimeOfDay(String timeOfDay) {
		this.timeOfDay = timeOfDay;
	}

	/**
	 * @return the experience
	 */
	public int getExperience() {
		return experience;
	}

	/**
	 * @param experience the experience to set
	 */
	public void setExperience(int experience) {
		if (experience >= 0 && experience <= GameStats.MAX_EXPERIENCE) {
			this.experience = experience;
		}
	}

	/**
	 * @return the coins
	 */
	public int getCoins() {
		return coins;
	}

	/**
	 * @param coins the coins to set
	 */
	public void setCoins(int coins) {
		this.coins = coins;
	}

	/**
	 * @return the profile
	 */
	public Profile getProfile() {
		return profile;
	}

	/**
	 * @param profile the profile to set
	 */
	public void setProfile(Profile profile) {
		this.profile = profile;
	}
	
	
	

}
