package sample;

import com.grishberg.stb.Input;
import com.grishberg.stb.Player;
import com.grishberg.stb.Stb;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

public class Main extends Application {
    private static final String MEDIA_URL = "file:/home/g/Видео/test.flv";
    private Stb mStb;
    private Input mInput;
    private Player mPlayer;
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("STB emulator");
        Group root = new Group();
        Scene scene = new Scene(root, 540, 240);
        // создаем медиаплеер
        Media media = new Media (MEDIA_URL);
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setAutoPlay(true);
        mPlayer = new Player(mediaPlayer);
        scene.setRoot(mPlayer);

        mInput = new Input(mPlayer);
        mStb = new Stb(mInput, mPlayer);
        mStb.start();
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
