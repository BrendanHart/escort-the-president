package escort.client.sound;

import java.io.BufferedInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import escort.client.main.Client;
import escort.client.properties.PropertyManager;
import escort.client.res.ResourceLoader;

/**
 * Use this SoundManager to play effect and bgm
 * 
 * @author Kwong Hei Tsang
 *
 */
public class SoundManager {

	private int effectplayercount;
	private float bgmvolume = 0;
	private float effectvolume = 0;
	private boolean bgmmuted = false;
	private boolean effectmuted = false;
	private final Clip dummyclip;
	private final SoundPlayer bgm;
	private final SoundPlayer[] effectplayers;
	private static final int SOUNDPLAYERS_PER_PLAYER = 3;
	private static final int MAX_PLAYERS = 15 + 5 + 5 + 5 + 1;
	private static final int MAX_EFFECT_PLAYERS = 1 + MAX_PLAYERS * SOUNDPLAYERS_PER_PLAYER;

	private String bgmfile;

	public static final String PISTOL_GUN_SHOT = "pistol_shot.wav";
	public static final String MG_GUN_SHOT = "mg_shot.wav";
	public static final String GRENADE_EXPLOSION = "grenade_explosion.wav";
	public static final String GRENADE_COLISSION = "grenade_coliision.wav";
	public static final String GRENADE_THROW = "grenade_throw.wav";
	public static final String GRENADE_COOK = "grenade_cook.wav";
	public static final String UNIT_RELOADING = "reload.wav";
	public static final String POWER_UP = "power_up_new.wav";

	private static final int UI_EFFECT_PLAYER = -1;
	public static final int SHOOTING_SOUND_PLAYER = 0;
	public static final int GRENADE_SOUND_PLAYER = 1;
	public static final int MISC_PLAYER = 2;

	private final Client client;

	/**
	 * Create a SoundManager
	 * 
	 * @param bgmvolume
	 * @param bgmmuted
	 * @param effectvolume
	 * @param effectmuted
	 * @throws SoundManagerException
	 */
	public SoundManager(Client client) throws SoundManagerException {
		// dummyclip for scaling formula
		this.client = client;
		Clip dummyclip = null;
		try {
			dummyclip = AudioSystem.getClip();
			dummyclip.open(AudioSystem.getAudioInputStream(
					new BufferedInputStream(ResourceLoader.class.getResourceAsStream("sound/bgmusic.wav"))));
		} catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
			throw new SoundManagerException();
		} finally {
			this.dummyclip = dummyclip;
		}

		// initialize attributes
		this.effectplayercount = 0;
		this.bgm = new SoundPlayer(this.convertScalarToDB(this.bgmvolume));

		// Initialize effect players
		this.effectplayers = new SoundPlayer[MAX_EFFECT_PLAYERS];
		while (getNewEffectPlayer() != -1)
			;
		this.bgmfile = null;
	}

	/**
	 * Get new effect player
	 * 
	 * @return The effect index, -1 for exceeding maximum number of players
	 */
	private synchronized int getNewEffectPlayer() {
		if (this.effectplayercount >= MAX_EFFECT_PLAYERS) {
			// maximum players reached
			return -1;
		}

		// Initialize player at next available index
		this.effectplayers[this.effectplayercount] = new SoundPlayer(this.effectvolume);
		return this.effectplayercount++;
	}

	/**
	 * Get the volume of the BGM
	 * 
	 * @return The volume of the BGM
	 */
	public synchronized float getBGMVolume() {
		return this.bgmvolume;
	}

	/**
	 * Set the BGM volume
	 * 
	 * @param bgmvolume
	 *            The BGM volume
	 */
	public synchronized void setBGMVolume(float bgmvolume) {
		this.bgmvolume = bgmvolume;
		if (this.bgm != null) {
			this.bgm.setVolume(this.convertScalarToDB(this.bgmvolume));
		}
	}

	/**
	 * Get effect volume
	 * 
	 * @return The volume of the effects
	 */
	public synchronized float getEffectVolume() {
		return this.effectvolume;
	}

	/**
	 * Set the effect volume, taking effect on next play
	 * 
	 * @param effectvolume
	 *            The new effect volume
	 */
	public synchronized void setEffectVolume(float effectvolume) {
		this.effectvolume = effectvolume;
	}

	/**
	 * Play a BGM
	 * 
	 * @param file
	 *            The file to be played
	 */
	public synchronized void playBGM(String file) {
		this.bgmfile = file;
		if (!this.bgmmuted && this.bgmfile != null && this.bgm != null) {
			try {
				this.bgm.play(file, 0);
				this.bgm.getClip().loop(Clip.LOOP_CONTINUOUSLY);
			} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			}

		}
	}

	/**
	 * Stop the current playing BGM
	 */
	public synchronized void stopBGM() {
		this.bgmfile = null;
		if (this.bgm != null) {
			this.bgm.stop();
		}
	}

	/**
	 * Play GUI effect
	 * 
	 * @param file
	 * @param pan
	 */
	public synchronized void playUIEffect(String file) {
		playEffect(0, file, 0, UI_EFFECT_PLAYER, 100);
	}

	/**
	 * Play effect from specific file with specific volume
	 * 
	 * @param playerid
	 *            The sound player ID
	 * @param file
	 *            The file to be played
	 * @param effectType
	 *            The type of the effect
	 * @param volume
	 *            A custom percentage relative to configured volume
	 */
	public synchronized void playEffect(int playerid, String file, float pan, int effectType, float volume) {
		if (this.effectmuted) {
			return;
		}

		final SoundPlayer player = this.effectplayers[1 + SOUNDPLAYERS_PER_PLAYER * playerid + effectType];
		player.setVolume(this.convertScalarToDB((this.effectvolume * volume) / 100));
		new Thread(() -> {
			try {
				player.play(file, pan);
			} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			}
		}).start();
	}

	/**
	 * Mute BGM
	 */
	public synchronized void muteBGM() {
		this.bgmmuted = true;
		this.bgm.stop();
	}

	/**
	 * Unmute BGM
	 * 
	 * @throws LineUnavailableException
	 * @throws IOException
	 * @throws UnsupportedAudioFileException
	 */
	public synchronized void unmuteBGM() throws IOException {
		this.bgmmuted = false;

		if (this.bgmfile == null || this.bgm == null) {
			// Should not be null
			return;
		}

		try {
			this.bgm.play(this.bgmfile, 0);
		} catch (UnsupportedAudioFileException | LineUnavailableException e) {
		}
	}

	/**
	 * Mute effects
	 */
	public synchronized void muteEffect() {
		this.effectmuted = true;
		for (int i = 0; i < this.effectplayercount; i++) {
			this.effectplayers[i].stop();
		}
	}

	/**
	 * Unmute the effects
	 */
	public synchronized void unmuteEffect() {
		this.effectmuted = false;
	}

	/**
	 * Convert from scalar to dB,so that it can be applied to the settings
	 * 
	 * @param scalar
	 *            The scalar
	 * @return The volume in dB
	 */
	private synchronized float convertScalarToDB(double scalar) {
		validateScalar(scalar);

		if (this.dummyclip == null || !this.dummyclip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
			// BGM object not exist or does not support master gain control
			return 0;
		}

		// Obtain maximum and minimum
		FloatControl control = (FloatControl) this.dummyclip.getControl(FloatControl.Type.MASTER_GAIN);
		double max = Math.pow(10.0, control.getMaximum() / 20.0);
		double min = Math.pow(10.0, control.getMinimum() / 20.0);

		// Calculate the result
		double result = scalar * (max - min) / 100 + min;
		return (float) (20 * Math.log10(result));
	}

	/**
	 * Validate volume
	 * 
	 * @param volume
	 *            The volume
	 */
	private void validateScalar(double volume) {
		if (volume > 100 || volume < 0) {
			throw new IllegalArgumentException("Invalid volume");
		}
	}

	public void loadFromProperties() {
		PropertyManager properties = client.getProperties();
		this.setBGMVolume(properties.getInt(PropertyManager.BGM_VOLUME));
		this.setEffectVolume(properties.getInt(PropertyManager.EFFECT_VOLUME));
		
		if(Boolean.parseBoolean(properties.getProperty(PropertyManager.BGM_MUTED))){
			this.muteBGM();
		}else{
			try {
				unmuteBGM();
			} catch (IOException e) {
			}
		}
		
		if(Boolean.parseBoolean(properties.getProperty(PropertyManager.EFFECT_MUTED))){
			this.muteEffect();
		}else{
			unmuteEffect();
		}
	}

	/**
	 * The main method for testing the SoundManager
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		SoundManager manager = new SoundManager(null);
		manager.playBGM("bgmusic.wav");
		Thread.sleep(2000);

		manager.muteBGM();
		Thread.sleep(2000);

		manager.unmuteBGM();
		Thread.sleep(2000);

		manager.stopBGM();
		Thread.sleep(2000);

		manager.playEffect(0, "bgmusic.wav", -1, UI_EFFECT_PLAYER, 100);
		Thread.sleep(2000);

		manager.playEffect(0, "bgmusic.wav", 1, UI_EFFECT_PLAYER, 50);
		Thread.sleep(2000);
	}
}