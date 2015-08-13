package sample;

import com.grishberg.stb.Input;
import com.grishberg.stb.Player;
import com.grishberg.stb.Stb;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application implements Stb.IOnRegisteredObserver {
    private Stb mStb;
    private Label mSecretCodeLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("STB emulator");
        Group root = new Group();
        Scene scene = new Scene(root, 540, 240);

        mStb = new Stb(this);

        BorderPane rootPane = new BorderPane();
        rootPane.setCenter(mStb.getMediaPlayer());

        // Add secret code label
        mSecretCodeLabel = new Label("Secret code: ");
        mSecretCodeLabel.setFont(new Font("Cambria", 32));

        rootPane.setBottom(mSecretCodeLabel);
        scene.setRoot(rootPane);
        mStb.start();
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void onRegistered(String secretKey) {
        //TODO: show secret key
        System.out.println("device registered, key = " + secretKey);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                mSecretCodeLabel.setText(String.format("Secret code: %s", secretKey));
            }
        });

    }

    @Override
    public void stop() throws Exception {
        super.stop();
        System.out.println("stop...");
        if(mStb != null){
            mStb.release();
        }
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
