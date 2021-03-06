package no.ntnu.imt3281.ludo.logic;

import java.util.Objects;

/**
 * An event that holds information about a player
 * change. (the player changed / left / won)
 */
public class PlayerEvent extends java.util.EventObject {

	/** Indicates if a player is playing */
	public static final int PLAYING = 0;
	/** Indicates if a player is waiting */
	public static final int WAITING = 1;
	/** Indicates that a player has left*/
	public static final int LEFTGAME = 2;
	/** Indicates that a player has won */
	public static final int WON = 3;
	private int activePlayer;
	private int state;
	
	
	/**
	 * Constructs a PlayerEvent with given object
	 * @param obj object that calls this event
	 */
	public PlayerEvent(Object obj) 
	{
		super(obj);
	}
	
	/**
	 * Constructs a PlayerEvent with given object and
	 * integers activePlayer and state
	 * @param obj object that calls the event
	 * @param active
	 * @param state
	 */
	public PlayerEvent(Object obj, int active, int state) {
		super(obj);
		setActivePlayer(active);
		setState(state);
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		
		if(obj != null && obj instanceof PlayerEvent) {
			PlayerEvent temp = (PlayerEvent) obj;
			return (this.activePlayer == temp.getActivePlayer() && this.state == temp.getState());
		
		} else {
			return false;
		}
	}
	
	@Override
    public int hashCode() {
		// makes a hash put of the given paramenters
        return Objects.hash(activePlayer, state);
    }

	/**
	 * gets current active player
	 * @return integer value of current active player. (ref. Ludo class)
	 */
	public int getActivePlayer() {
		return activePlayer;
	}
	
	/**
	 * sets current active player
	 * @param active index(color) of the player that is to be set as active
	 */
	public void setActivePlayer(int active) {
		this.activePlayer = active;
	}
	
	/**
	 * gets the current state of an active player (Playing, waiting..)
	 * @return integer, current state
	 */
	public int getState() {
		return state;
	}

	/**
	 * sets current state of an active player.
	 * @param state index of current state
	 */
	public void setState(int state) {
		this.state = state;
	}
	

	@Override
	public String toString() {
		StringBuilder playerstring = new StringBuilder();
		playerstring.append("Active Player: " + getActivePlayer() + " State: " + getState());
		return playerstring.toString();
	}

	
}
