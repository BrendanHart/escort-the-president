package escort.client.sound;

import java.io.BufferedInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import escort.client.res.ResourceLoader;

/**
 * Create a sound player object
 * @author Kwong Hei Tsang
 */
public class SoundPlayer {

	private AudioInputStream stream = null;
	private Clip clip;
	private float volume;
	private String file;

	/**
	 * Create a sound player object
	 * @param volume The volume by master gain
	 * @throws LineUnavailableException
	 */
	public SoundPlayer(float volume) {
		this.clip = null;
		this.volume = volume;
		this.file = "";
	}

	/**
	 * Player a specific audio file
	 * @param file
	 * @param pan
	 * @throws UnsupportedAudioFileException
	 * @throws IOException
	 * @throws LineUnavailableException
	 */
	public synchronized void play(String file, float pan)
			throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		// If it is the same file
		if(this.file.equals(file) && this.clip != null){
			// should restart instead
			this.clip.stop();
			this.clip.flush();
			this.clip.setFramePosition(0);
			this.setVolume(this.volume);
			// pan has to be set again
			if (this.clip.isControlSupported(FloatControl.Type.PAN)) {
				FloatControl pancontrol = (FloatControl) this.clip.getControl(FloatControl.Type.PAN);
				pancontrol.setValue(pan);
			}
			this.clip.start();
			return;
		}
		
		this.file = file;
		this.playStream(AudioSystem.getAudioInputStream(
				new BufferedInputStream(ResourceLoader.class.getResourceAsStream("sound/" + file))), pan);
	}

	/**
	 * Play a specific audio input stream
	 * 
	 * @param stream
	 * @throws LineUnavailableException
	 * @throws IOException
	 */
	private synchronized void playStream(AudioInputStream stream, float pan)
			throws LineUnavailableException, IOException {
		// stop current playing music
		this.stop();
		this.stream = stream;

		// play new sound
		this.clip = AudioSystem.getClip();
		this.clip.open(stream);
		if (this.clip.isControlSupported(FloatControl.Type.PAN)) {
			FloatControl pancontrol = (FloatControl) this.clip.getControl(FloatControl.Type.PAN);
			pancontrol.setValue(pan);
		}
		this.setVolume(this.volume);
		this.clip.start();
	}

	/**
	 * Stop the current playing music
	 */
	public synchronized void stop() {
		// Stop current playing music
		if (this.clip != null) {
			this.clip.stop();
			final Clip clip = this.clip;
			new Thread(() -> clip.close()).start();
			this.clip = null;
		}
		
		// Close the stream
		if (this.stream != null) {
			final AudioInputStream stream = this.stream;
			new Thread(() -> {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}).start();
		}
	}

	/**
	 * Set a new volume
	 * @param newValue The volume by master gain
	 */
	public synchronized void setVolume(float newValue) {
		this.volume = newValue;
		// Check if volume control is supported
		FloatControl.Type controltype = FloatControl.Type.MASTER_GAIN;
		if (this.clip != null && this.clip.isControlSupported(controltype)) {
			FloatControl volumeControl = (FloatControl) this.clip.getControl(controltype);
			volumeControl.setValue(newValue);
		}
	}

	/**
	 * Get the clip of this sound player
	 * @return The clip of this sound player
	 */
	synchronized Clip getClip() {
		return this.clip;
	}

	/**
	 * The main method for testing the sound player
	 * @param args
	 * @throws LineUnavailableException
	 * @throws UnsupportedAudioFileException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args)
			throws LineUnavailableException, UnsupportedAudioFileException, IOException, InterruptedException {
		SoundPlayer player = new SoundPlayer(0);
		player.setVolume(-20);
		player.play("bgmusic.wav", 0);

		Thread.sleep(2000);
		player.play("bgmusic.wav", 0);

		Thread.sleep(2000);
		player.setVolume(0);

		Thread.sleep(2000);
		FloatControl volumeControl = (FloatControl) player.clip.getControl(FloatControl.Type.MASTER_GAIN);
		System.out.println(volumeControl.getMaximum());
		System.out.println(volumeControl.getMinimum());
	}
}
