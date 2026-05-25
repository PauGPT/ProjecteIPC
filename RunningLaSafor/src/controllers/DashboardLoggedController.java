package controllers;

import application.App;
import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

/**
 * FXML Controller class for DashboardLogged
 *
 * @author Pau
 */
public class DashboardLoggedController implements Initializable {

    private SportActivityApp app = SportActivityApp.getInstance();

    @FXML
    private Circle avatarCircle;
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label monthLabel;
    @FXML
    private Label lblTotalKms;
    @FXML
    private Label lblTotalTime;
    @FXML
    private Label lblElevationGain;
    @FXML
    private Label lblElevationLoss;
    @FXML
    private Region left_region;
    @FXML
    private Region right_region;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        User user = app.getCurrentUser();
        if (user != null) {
            welcomeLabel.setText("Benvingut de nou, " + user.getNickName() + "!");
            if (user.getAvatarPath() != null && !user.getAvatarPath().trim().isEmpty()) {
                try {
                    File file = new File(user.getAvatarPath());
                    if (file.exists()) {
                        Image image = new Image(file.toURI().toString());
                        avatarCircle.setFill(new ImagePattern(image));
                    } else {
                        setDefaultAvatar();
                    }
                } catch (Exception e) {
                    setDefaultAvatar();
                }
            } else {
                setDefaultAvatar();
            }
        } else {
            welcomeLabel.setText("Benvingut!");
            setDefaultAvatar();
        }

        // Set month label and calculate stats
        updateMonthlyActivities();
    }

    private void setDefaultAvatar() {
        try {
            Image defaultAvatar = new Image(getClass().getResourceAsStream("/resources/main/default_avatar.png"));
            avatarCircle.setFill(new ImagePattern(defaultAvatar));
        } catch (Exception e) {
            System.err.println("No s'ha pogut carregar l'avatar per defecte: " + e.getMessage());
        }
    }

    private void updateMonthlyActivities() {
        LocalDate now = LocalDate.now();
        String[] mesos = {
            "gener", "febrer", "març", "abril", "maig", "juny", 
            "juliol", "agost", "setembre", "octubre", "novembre", "desembre"
        };
        String nomMes = mesos[now.getMonthValue() - 1];
        monthLabel.setText("Acumulat de les teues activitats de " + nomMes + ":");

        double totalMeters = 0;
        long totalSeconds = 0;
        double totalGain = 0;
        double totalLoss = 0;

        List<Activity> activities = app.getUserActivities();
        if (activities != null) {
            int currentMonth = now.getMonthValue();
            int currentYear = now.getYear();

            for (Activity activity : activities) {
                if (activity.getStartTime() != null) {
                    LocalDateTime start = activity.getStartTime();
                    if (start.getMonthValue() == currentMonth && start.getYear() == currentYear) {
                        totalMeters += activity.getTotalDistance();
                        if (activity.getDuration() != null) {
                            totalSeconds += activity.getDuration().getSeconds();
                        }
                        totalGain += activity.getElevationGain();
                        totalLoss += activity.getElevationLoss();
                    }
                }
            }
        }

        double totalKms = totalMeters / 1000.0;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;

        lblTotalKms.setText(String.format(java.util.Locale.getDefault(), "%.1f km", totalKms));
        lblTotalTime.setText(String.format(java.util.Locale.getDefault(), "%dh %dm", hours, minutes));
        lblElevationGain.setText(String.format(java.util.Locale.getDefault(), "+%.0f m", totalGain));
        lblElevationLoss.setText(String.format(java.util.Locale.getDefault(), "-%.0f m", totalLoss));
    }
}
