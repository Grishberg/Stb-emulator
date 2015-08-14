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
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application implements IView {
    private Stb mStb;
    private Label secretCodeLabel;
    private Label positionLabel;
    private Label volumeLabel;
    private MediaView mediaView;
    private boolean stopRequested = false;
    private boolean atEndOfMedia = false;
    private Slider timeSlider;
    private Slider volumeSlider;
    private HBox mediaBar;
    private Stage mPrimaryStage;
    private boolean isFullScreen = false;

    public Main() {
    }

    @Override
    public void start(Stage primaryStage) {
        mPrimaryStage = primaryStage;
        primaryStage.setTitle("STB emulator");
        //Group root = new Group();

        mStb = new Stb(this);

        mediaView = new MediaView();
        StackPane.setAlignment(mediaView, Pos.CENTER);
        DoubleProperty width = mediaView.fitWidthProperty();
        DoubleProperty height = mediaView.fitHeightProperty();
        width.bind(Bindings.selectDouble(mediaView.sceneProperty(), "width").subtract(30));
        height.bind(Bindings.selectDouble(mediaView.sceneProperty(), "height").subtract(30));
        mediaView.setPreserveRatio(true);

        // Add secret code label
        secretCodeLabel = new Label("Secret code: ");
        makeSelectable(secretCodeLabel);
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

        StackPane rootMedia = new StackPane();
        rootMedia.setStyle("-fx-background-color: #000000;");

        rootMedia.getChildren().add(mediaView);
        rootMedia.getChildren().add(secretCodeLabel);
        rootMedia.getChildren().add(positionLabel);
        rootMedia.getChildren().add(volumeLabel);

        mediaBar = new HBox();
        mediaBar.setMinHeight(30);
        mediaBar.setAlignment(Pos.CENTER);
        mediaBar.setPadding(new Insets(5, 10, 5, 10));
        BorderPane.setAlignment(mediaBar, Pos.CENTER);

        // Add spacer
        Label spacer = new Label("   ");
        mediaBar.getChildren().add(spacer);

        // Add Time label
        Label timeLabel = new Label("Time: ");
        mediaBar.getChildren().add(timeLabel);

        // Add time slider
        timeSlider = new Slider();
        HBox.setHgrow(timeSlider, Priority.ALWAYS);
        timeSlider.setMinWidth(50);
        timeSlider.setMaxWidth(Double.MAX_VALUE);

        mediaBar.getChildren().add(timeSlider);

        // Add the volume label
        Label volumeLabel = new Label("Vol: ");
        mediaBar.getChildren().add(volumeLabel);

        // Add Volume slider
        volumeSlider = new Slider();
        volumeSlider.setPrefWidth(70);
        volumeSlider.setMaxWidth(Region.USE_PREF_SIZE);
        volumeSlider.setMinWidth(30);

        mediaBar.getChildren().add(volumeSlider);

        mStb.start();

        VBox root = new VBox();
        root.setAlignment(Pos.TOP_CENTER);
        root.getChildren().add(mediaBar);
        root.getChildren().add(rootMedia);

        Scene scene = new Scene(root, 800, 600);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static String formatTime(Duration elapsed, Duration duration) {
        int intElapsed = (int) Math.floor(elapsed.toSeconds());
        int elapsedHours = intElapsed / (60 * 60);
        if (elapsedHours > 0) {
            intElapsed -= elapsedHours * 60 * 60;
        }
        int elapsedMinutes = intElapsed / 60;
        int elapsedSeconds = intElapsed - elapsedHours * 60 * 60 - elapsedMinutes
                * 60;

        if (duration.greaterThan(Duration.ZERO)) {
            int intDuration = (int) Math.floor(duration.toSeconds());
            int durationHours = intDuration / (60 * 60);
            if (durationHours > 0) {
                intDuration -= durationHours * 60 * 60;
            }
            int durationMinutes = intDuration / 60;
            int durationSeconds = intDuration - durationHours * 60 * 60
                    - durationMinutes * 60;
            if (durationHours > 0) {
                return String.format("%d:%02d:%02d/%d:%02d:%02d", elapsedHours,
                        elapsedMinutes, elapsedSeconds, durationHours, durationMinutes,
                        durationSeconds);
            } else {
                return String.format("%02d:%02d/%02d:%02d", elapsedMinutes,
                        elapsedSeconds, durationMinutes, durationSeconds);
            }
        } else {
            if (elapsedHours > 0) {
                return String.format("%d:%02d:%02d", elapsedHours, elapsedMinutes,
                        elapsedSeconds);
            } else {
                return String.format("%02d:%02d", elapsedMinutes, elapsedSeconds);
            }
        }
    }

    @Override
    public void setMediaPlayer(MediaPlayer mediaPlayer) {

        mediaView.setMediaPlayer(mediaPlayer);
    }

    @Override
    public void onChangedTimePosition(final double currentPosition, final String caption) {
        if (currentPosition < 0) {
            positionLabel.setText("--- ");
            timeSlider.setDisable(true);
        } else {
            positionLabel.setText(String.format("Position %d%% %s", (int) currentPosition, caption));
            timeSlider.setDisable(false);
            Platform.runLater(new Runnable() {
                public void run() {
                    if (!timeSlider.isDisabled() && !timeSlider.isValueChanging()) {
                        timeSlider.setValue(currentPosition);
                    }
                }
            });
        }
    }

    @Override
    public void onChangedVolume(final double volume) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                volumeLabel.setText(String.format("Volume %d", (int) volume));
                if (!volumeSlider.isValueChanging()) {
                    volumeSlider.setValue((int) Math.round(volume));
                }
            }
        });
    }

    @Override
    public void onChangedState(Player.PlayerState state) {

    }

    /**
     * event when device has registered on Liniking server
     *
     * @param secretKey
     */
    @Override
    public void onRegistered(final String secretKey) {
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
    public void onFullScreen() {
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        if (!isFullScreen) {

            mPrimaryStage.setX(bounds.getMinX());
            mPrimaryStage.setY(bounds.getMinY());
            mPrimaryStage.setWidth(bounds.getWidth());
            mPrimaryStage.setHeight(bounds.getHeight());
            isFullScreen = !isFullScreen;
        } else {
            mPrimaryStage.setX(bounds.getMinX());
            mPrimaryStage.setY(bounds.getMinY());
            mPrimaryStage.setWidth(800);
            mPrimaryStage.setHeight(600);
            isFullScreen = !isFullScreen;
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        System.out.println("stop...");
        if (mStb != null) {
            mStb.release();
        }
    }

    private Label makeSelectable(Label label) {
        StackPane textStack = new StackPane();
        TextField textField = new TextField(label.getText());
        textField.setFont(new Font("Cambria", 16));
        textField.setMinWidth(220);
        label.setMinWidth(220);
        textField.setEditable(false);
        textField.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-background-insets: 0;" +
                        "-fx-background-radius: 0;" +
                        "-fx-padding: 0;" +
                        "-fx-text-inner-color:#0076a3;"
        );
        // the invisible label is a hack to get the textField to size like a label.
        Label invisibleLabel = new Label();
        invisibleLabel.textProperty().bind(label.textProperty());
        invisibleLabel.setVisible(false);
        textStack.getChildren().addAll(invisibleLabel, textField);
        label.textProperty().bindBidirectional(textField.textProperty());
        label.setGraphic(textStack);
        label.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        return label;
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
