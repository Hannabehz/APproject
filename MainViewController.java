package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import service.UserService;
import view.LoginView;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


public class MainViewController {
    @FXML private MenuItem showProfileItem;

    private String token;
    private Stage stage;
    private final UserService userService = new UserService();
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setToken(String token) {
        this.token = token;
    }


    @FXML
    private void showProfile() {
        if (token == null || token.trim().isEmpty()) {
            System.out.println("Token is not set or empty!");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("خطا");
            alert.setHeaderText(null);
            alert.setContentText("توکن معتبر نیست یا وجود ندارد!");
            alert.showAndWait();
            return;
        }

        CompletableFuture<Map<String, Object>> future = userService.getProfile(token);
        future.thenAccept(response -> Platform.runLater(() -> {
            if (response != null && response.containsKey("status")) {
                // تبدیل ایمن مقدار status به int
                int status;
                Object statusObj = response.get("status");
                if (statusObj instanceof Double) {
                    status = ((Double) statusObj).intValue();
                } else if (statusObj instanceof Integer) {
                    status = (Integer) statusObj;
                } else {
                    System.out.println("Unexpected status type: " + statusObj.getClass().getName());
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("خطا");
                    alert.setHeaderText(null);
                    alert.setContentText("نوع داده status نامعتبر است!");
                    alert.showAndWait();
                    return;
                }

                if (status == 200 && response.containsKey("user")) {
                    try {
                        Map<String, Object> userData = (Map<String, Object>) response.get("user");
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo1/displayProfile.fxml"));
                        Parent root = loader.load();
                        DisplayProfileController controller = loader.getController();
                        controller.setToken(token); // ارسال توکن خام
                        controller.setProfileData(userData);
                        Stage stage = new Stage();
                        stage.setScene(new Scene(root, 600, 400));
                        stage.setTitle("نمایش پروفایل");
                        stage.show();
                    } catch (IOException e) {
                        System.err.println("FXML Load Error: " + e.getMessage());
                        e.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("خطا");
                        alert.setHeaderText(null);
                        alert.setContentText("خطا در بارگذاری صفحه پروفایل: " + e.getMessage());
                        alert.showAndWait();
                    }
                } else {
                    System.out.println("Error: " + response.getOrDefault("message", "Unknown error"));
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("خطا");
                    alert.setHeaderText(null);
                    alert.setContentText(response.getOrDefault("message", "خطا در دریافت پروفایل").toString());
                    alert.showAndWait();
                }
            } else {
                System.out.println("Response is null or missing status!");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("خطا");
                alert.setHeaderText(null);
                alert.setContentText("پاسخ سرور نامعتبر است!");
                alert.showAndWait();
            }
        })).exceptionally(throwable -> {
            Platform.runLater(() -> {
                System.err.println("Error fetching profile: " + throwable.getMessage());
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("خطا");
                alert.setHeaderText(null);
                alert.setContentText("خطا در ارتباط با سرور: " + throwable.getMessage());
                alert.showAndWait();
            });
            return null;
        });
    }
    @FXML
    private void logOut() {
        if (token == null || token.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "خطا", "توکن معتبر نیست یا وجود ندارد!");
            return;
        }

        userService.logOut(token).thenAccept(response -> Platform.runLater(() -> {
            System.out.println("Logout response: " + response);
            if (response.containsKey("status") && ((Number) response.get("status")).intValue() == 200) {
                token = null; // پاک کردن توکن// حذف صحنه فعلی
                if (stage != null) {
                    stage.close();
                } else {
                    System.err.println("Stage is null in MainViewController.logOut!");
                }

                // باز کردن LoginView در Stage جدید
                try {
                    Stage newStage = new Stage();
                    LoginView loginView = new LoginView(newStage);
                } catch (Exception e) {
                    System.err.println("Error creating LoginView: " + e.getMessage());
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "خطا", "خطا در بارگذاری صفحه ورود: " + e.getMessage());
                }

            } else {
                String message = response.getOrDefault("message", "خطا در خروج از حساب").toString();
                showAlert(Alert.AlertType.ERROR, "خطا", message);
            }
        }));
    }
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}