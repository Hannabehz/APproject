package controller;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.RestaurantDTO;
import service.CartService;
import service.RestaurantService;
import service.UserService;
import view.LoginView;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.util.*;
import java.util.concurrent.CompletableFuture;


public class MainViewController {
    @FXML private MenuItem showProfileItem;
    @FXML private MenuItem logOutButton;
    @FXML private MenuItem viewShoppingCartButton;
    @FXML private MenuItem viewOrderHistoryButton;
    @FXML private MenuItem topUpWallet;
    @FXML private MenuItem favoritesMenuItem;
    @FXML private TextField searchField;
    @FXML private ToggleButton fastFoodToggle;
    @FXML private ToggleButton iranianToggle;
    @FXML private ToggleButton italianToggle;
    @FXML private ToggleGroup categoryToggleGroup;
    @FXML private ListView<RestaurantDTO> restaurantListView;
    private String token;
    private Stage stage;
    private Map<UUID, Boolean> favoriteStatusMap = new HashMap<>();
    private final UserService userService = new UserService();
    private final CartService cartService = new CartService();
    private final RestaurantService restaurantService = new RestaurantService();
    private final Gson gson = new Gson();
    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setMaximized(true);
    }

    public void setToken(String token) {
        System.out.println("Setting token in MainViewController: " + token); // لاگ برای بررسی
        this.token = token;
        // بارگذاری رستوران‌ها پس از تنظیم توکن
        if (token != null && !token.trim().isEmpty()) {
            updateRestaurantList();
        } else {
            System.err.println("Token is null or empty in setToken");
            showAlert(Alert.AlertType.ERROR, "خطا", "توکن معتبر نیست یا وجود ندارد!");
        }
    }

    @FXML
    public void initialize() {
        if (categoryToggleGroup == null) {
            System.err.println("Error: categoryToggleGroup is null in initialize!");
            categoryToggleGroup = new ToggleGroup();
        }
        favoritesMenuItem = new MenuItem("مورد علاقه‌ها");
        favoritesMenuItem.setOnAction(this::showFavorites);

        // اضافه کردن منوی جدید به منوی اصلی
        MenuButton menuButton = (MenuButton) stage.getScene().lookup("#toolBar").lookup(".menu-button");
        menuButton.getItems().add(0, favoritesMenuItem);
        fastFoodToggle.setToggleGroup(categoryToggleGroup);
        iranianToggle.setToggleGroup(categoryToggleGroup);
        italianToggle.setToggleGroup(categoryToggleGroup);

        System.out.println("FastFoodToggle in group: " + (fastFoodToggle.getToggleGroup() == categoryToggleGroup));
        System.out.println("IranianToggle in group: " + (iranianToggle.getToggleGroup() == categoryToggleGroup));
        System.out.println("ItalianToggle in group: " + (italianToggle.getToggleGroup() == categoryToggleGroup));

        // تنظیم گوش‌دهنده‌ها با بررسی توکن
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            System.out.println("Search field changed: " + newValue);
            if (isTokenValid()) {
                updateRestaurantList();
            } else {
                System.out.println("Skipping updateRestaurantList: Token is not set yet");
            }
        });

        fastFoodToggle.selectedProperty().addListener((obs, oldValue, newValue) -> {
            System.out.println("FastFoodToggle changed: " + newValue);
            if (isTokenValid()) {
                updateRestaurantList();
            } else {
                System.out.println("Skipping updateRestaurantList: Token is not set yet");
            }
        });

        iranianToggle.selectedProperty().addListener((obs, oldValue, newValue) -> {
            System.out.println("IranianToggle changed: " + newValue);
            if (isTokenValid()) {
                updateRestaurantList();
            } else {
                System.out.println("Skipping updateRestaurantList: Token is not set yet");
            }
        });

        italianToggle.selectedProperty().addListener((obs, oldValue, newValue) -> {
            System.out.println("ItalianToggle changed: " + newValue);
            if (isTokenValid()) {
                updateRestaurantList();
            } else {
                System.out.println("Skipping updateRestaurantList: Token is not set yet");
            }
        });

        restaurantListView.setCellFactory(listView -> new ListCell<RestaurantDTO>() {
            @Override
            protected void updateItem(RestaurantDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox hBox = new HBox(10);
                    hBox.setAlignment(Pos.CENTER_RIGHT);
                    ImageView logoView = new ImageView();
                    if (item.getLogoBase64() != null && !item.getLogoBase64().isEmpty()) {
                        try {
                            byte[] imageBytes = Base64.getDecoder().decode(item.getLogoBase64());
                            Image image = new Image(new ByteArrayInputStream(imageBytes));
                            logoView.setImage(image);
                            logoView.setFitWidth(50);
                            logoView.setFitHeight(50);
                        } catch (Exception e) {
                            System.err.println("Error loading logo: " + e.getMessage());
                        }
                    }
                    Label nameLabel = new Label(item.getName());
                    Label categoryLabel = new Label("دسته‌بندی: " + item.getCategory());
                    Label addressLabel = new Label("آدرس: " + item.getAddress());
                    ToggleButton favoriteButton = new ToggleButton();
                    favoriteButton.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

                    // تعیین وضعیت اولیه قلب
                    boolean isFavorite = favoriteStatusMap.getOrDefault(item.getId(), false);
                    favoriteButton.setSelected(isFavorite);
                    updateFavoriteButtonGraphic(favoriteButton);

                    favoriteButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
                        updateFavoriteButtonGraphic(favoriteButton);
                        toggleFavorite(item.getId(), newVal);
                    });
                    HBox infoBox = new HBox(10, logoView, nameLabel, categoryLabel, addressLabel);
                    hBox.getChildren().addAll(infoBox, favoriteButton);
                    setGraphic(hBox);
                }
            }
        });

        restaurantListView.setOnMouseClicked(event -> {
            RestaurantDTO selectedRestaurant = restaurantListView.getSelectionModel().getSelectedItem();
            if (selectedRestaurant != null && isTokenValid()) {
                System.out.println("Restaurant clicked: " + selectedRestaurant.getName() + " (ID: " + selectedRestaurant.getId() + ")");
                showMenuView(selectedRestaurant.getId(), selectedRestaurant.getName());
            } else {
                System.out.println("No restaurant selected or token is not set yet");
                if (!isTokenValid()) {
                    showAlert(Alert.AlertType.ERROR, "خطا", "توکن معتبر نیست یا هنوز تنظیم نشده است!");
                }
            }
        });
    }
    @FXML
    private void showFavorites(ActionEvent event) {
        if (!isTokenValid()) {
            showAlert(Alert.AlertType.ERROR, "خطا", "توکن معتبر نیست!");
            return;
        }

        restaurantService.getFavorites(token)
                .thenAccept(favorites -> Platform.runLater(() -> {
                    restaurantListView.getItems().clear();
                    if (favorites.isEmpty()) {
                        showAlert(Alert.AlertType.INFORMATION, "اطلاعات", "هیچ رستوران مورد علاقه‌ای یافت نشد.");
                    } else {
                        restaurantListView.getItems().addAll(favorites);
                        // به‌روزرسانی وضعیت مورد علاقه‌ها
                        favorites.forEach(rest -> favoriteStatusMap.put(rest.getId(), true));
                    }
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() ->
                            showAlert(Alert.AlertType.ERROR, "خطا", "خطا در دریافت رستوران‌های مورد علاقه: " + throwable.getMessage()));
                    return null;
                });
    }
    private void updateFavoriteButtonGraphic(ToggleButton button) {
        if (button.isSelected()) {
            button.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/images/heart_filled.png"))));
        } else {
            button.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/images/heart_empty.png"))));
        }
    }

    // تغییر وضعیت مورد علاقه
    private void toggleFavorite(UUID restaurantId, boolean isFavorite) {
        if (!isTokenValid()) {
            showAlert(Alert.AlertType.ERROR, "خطا", "توکن معتبر نیست!");
            return;
        }

        if (isFavorite) {
            restaurantService.addFavorite(token, restaurantId)
                    .thenAccept(response -> Platform.runLater(() -> {
                        favoriteStatusMap.put(restaurantId, true);
                        System.out.println("Added to favorites: " + restaurantId);
                    }))
                    .exceptionally(throwable -> {
                        Platform.runLater(() ->
                                showAlert(Alert.AlertType.ERROR, "خطا", "خطا در افزودن به مورد علاقه‌ها: " + throwable.getMessage()));
                        return null;
                    });
        } else {
            restaurantService.removeFavorite(token, restaurantId)
                    .thenAccept(response -> Platform.runLater(() -> {
                        favoriteStatusMap.put(restaurantId, false);
                        System.out.println("Removed from favorites: " + restaurantId);
                    }))
                    .exceptionally(throwable -> {
                        Platform.runLater(() ->
                                showAlert(Alert.AlertType.ERROR, "خطا", "خطا در حذف از مورد علاقه‌ها: " + throwable.getMessage()));
                        return null;
                    });
        }
    }
    private boolean isTokenValid() {
        return token != null && !token.trim().isEmpty();
    }
    private void showMenuView(UUID restaurantId, String restaurantName) {
        try {
            String fxmlPath = "/org/example/demo1/MenuView.fxml";
            java.net.URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new IOException("Cannot find FXML file at path: " + fxmlPath);
            }
            System.out.println("Loading FXML from: " + fxmlUrl);

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            MenuViewController controller = loader.getController();
            controller.setStage(stage);
            controller.setToken(token);
            controller.setRestaurantInfo(restaurantId, restaurantName);
            stage.setScene(new Scene(root));
            stage.show();
            System.out.println("Navigated to MenuView for restaurant: " + restaurantName + " (ID: " + restaurantId + ")");
        } catch (IOException e) {
            System.err.println("Error loading menu view: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "خطا", "خطا در نمایش منو: " + e.getMessage());
        }
    }
    private void updateRestaurantList() {
        if (token == null || token.trim().isEmpty()) {
            System.err.println("Token is not set or empty in updateRestaurantList!");
            showAlert(Alert.AlertType.ERROR, "خطا", "توکن معتبر نیست یا وجود ندارد!");
            return;
        }

        // جمع‌آوری دسته‌بندی‌های انتخاب‌شده
        List<String> categories = new ArrayList<>();
        if (fastFoodToggle.isSelected()) categories.add("فست‌فود");
        if (iranianToggle.isSelected()) categories.add("ایرانی");
        if (italianToggle.isSelected()) categories.add("ایتالیایی");
        System.out.println("Selected categories: " + categories);

        String search = searchField.getText();
        System.out.println("Search query: " + search);

        // فراخوانی سرویس
        System.out.println("Fetching restaurants with token: " + token);
        restaurantService.getRestaurants(token, search, categories.isEmpty() ? null : categories)
                .thenAccept(restaurants -> Platform.runLater(() -> {
                    restaurantListView.getItems().clear();
                    if (restaurants.isEmpty()) {
                        showAlert(Alert.AlertType.INFORMATION, "اطلاعات", "هیچ رستورانی با این فیلترها یافت نشد.");
                    } else {
                        restaurantListView.getItems().addAll(restaurants);
                        System.out.println("Loaded " + restaurants.size() + " restaurants: " + restaurants);
                    }
                }))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "خطا", "خطا در دریافت رستوران‌ها: " + throwable.getMessage()));
                    return null;
                });
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
    @FXML
    private void viewShoppingCart() {
        if (token == null || token.trim().isEmpty()) {
            System.out.println("Token is not set or empty!");
            showAlert(Alert.AlertType.ERROR, "خطا", "توکن معتبر نیست یا وجود ندارد!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo1/displayShoppingCart.fxml"));
            Parent root = loader.load();
            CartViewController controller = loader.getController();
            controller.setToken(token);
            controller.setStage(new Stage());
            Stage cartStage = new Stage();
            cartStage.setScene(new Scene(root, 800, 600));
            cartStage.setTitle("سبد خرید");
            cartStage.show();
        } catch (IOException e) {
            System.err.println("FXML Load Error: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "خطا", "خطا در بارگذاری صفحه سبد خرید: " + e.getMessage());
        }
    }
    @FXML
    private void topUpWallet(){
        if (token == null || token.trim().isEmpty()) {
            System.out.println("Token is not set or empty!");
            showAlert(Alert.AlertType.ERROR, "خطا", "توکن معتبر نیست یا وجود ندارد!");
            return;
        }
        try{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo1/walletTopUp.fxml"));
        Parent root = loader.load();
        WalletTopUpController controller = loader.getController();
        controller.setToken(token);
        controller.setStage(new Stage());
        Stage cartStage = new Stage();
        cartStage.setScene(new Scene(root, 800, 600));
        cartStage.setTitle("شارژ کیف پول");
        cartStage.show();
    } catch (IOException e) {
        System.err.println("FXML Load Error: " + e.getMessage());
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "خطا", "خطا در بارگذاری صفحه شارژ کیف پول: " + e.getMessage());
    }
    }
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML
    private void viewOrderHistory() {
        if (token == null || token.trim().isEmpty()) {
            System.out.println("Token is not set or empty!");
            showAlert(Alert.AlertType.ERROR, "خطا", "توکن معتبر نیست یا وجود ندارد!");
            return;
        }
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo1/HistoryView.fxml"));
            Parent root = loader.load();
            HistoryViewController controller = loader.getController();
            controller.setToken(token);
            controller.setStage(new Stage());
            Stage cartStage = new Stage();
            cartStage.setScene(new Scene(root, 1000, 600));
            cartStage.setTitle("تاریخچه سفارش ها");
            cartStage.show();
        } catch (IOException e) {
            System.err.println("FXML Load Error: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "خطا", "خطا در بارگذاری صفحه تاریخچه سفارش ها: " + e.getMessage());
        }
    }
}