package sample;

import com.grishberg.interfaces.IView;
import com.grishberg.stb.Player;
import com.grishberg.stb.Stb;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application implements IView {
    private Stb mStb;
    private Label secretCodeLabel;
    private Label positionLabel;
    private Label volumeLabel;
    private MediaView mediaView;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("STB emulator");
        //Group root = new Group();

        mStb = new Stb(this);

        mediaView = new MediaView();
        StackPane.setAlignment(mediaView,Pos.CENTER);
        DoubleProperty width = mediaView.fitWidthProperty();
        DoubleProperty height = mediaView.fitHeightProperty();
        width.bind(Bindings.selectDouble(mediaView.sceneProperty(), "width"));
        height.bind(Bindings.selectDouble(mediaView.sceneProperty(), "height"));
        mediaView.setPreserveRatio(true);

        // Add secret code label
        secretCodeLabel = new Label("Secret code: ");
        secretCodeLabel.setFont(new Font("Cambria", 18));
        secretCodeLabel.setTextFill(Color.web("#0076a3"));
        StackPane.setAlignment(secretCodeLabel, Pos.TOP_CENTER);

        positionLabel = new Label("position: ");
        positionLabel.setFont(new Font("Cambria", 12));
        positionLabel.setTextFill(Color.web("#ffffff"));
        StackPane.setAlignment(positionLabel, Pos.BOTTOM_RIGHT);

        volumeLabel = new Label("Volume: ");
        volumeLabel.setFont(new Font("Cambria", 12));
        volumeLabel.setTextFill(Color.web("#ffffff"));
        StackPane.setAlignment(volumeLabel, Pos.BOTTOM_LEFT);


        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #000000;");

        root.getChildren().add(mediaView);
        root.getChildren().add(secretCodeLabel);
        root.getChildren().add(positionLabel);
        root.getChildren().add(volumeLabel);

        mStb.start();
        Scene scene = new Scene(root, 640, 300);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void setMediaPlayer(MediaPlayer mediaPlayer) {

        mediaView.setMediaPlayer(mediaPlayer);
    }

    @Override
    public void onChangedTimePosition(double currentPosition) {
        if(currentPosition <0){
            positionLabel.setText("--- ");

        }else{
            positionLabel.setText(String.format("Position %d", (int)currentPosition));

        }
    }

    @Override
    public void onChangedVolume(double volume) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                volumeLabel.setText(String.format("Volume %d", (int)volume));
            }
        });
    }

    @Override
    public void onChangedState(Player.PlayerState state) {

    }

    /**
     * event when device has registered on Liniking server
     * @param secretKey
     */
    @Override
    public void onRegistered(String secretKey) {
        //TODO: show secret key
        System.out.println("device registered, key = " + secretKey);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                secretCodeLabel.setText(String.format("Secret code: %s", secretKey));
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
