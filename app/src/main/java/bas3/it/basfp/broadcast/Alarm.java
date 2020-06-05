package bas3.it.basfp.broadcast;

import android.text.format.Time;

import java.io.Serializable;

public class Alarm implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private Time heure;
	private boolean active;
	
	public Time getHeure() {
		return heure;
	}
	public void setHeure(Time heure) {
		this.heure = heure;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
}
