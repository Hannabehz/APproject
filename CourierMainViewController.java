package controller;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import model.Order;
import service.CartService;
import service.CourierService;
import service.UserService;
import view.LoginView;

import java.io.IOException;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;


public class CourierMainViewController {
    @FXML private MenuItem showProfileItem;
    @FXML private MenuItem logOutButton;
    @FXML private MenuItem viewDeliveryHistoryItem;
    @FXML private MenuButton menuButton;
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, String> orderIdColumn;
    @FXML private TableColumn<Order, String> deliveryAddressColumn;
    @FXML private TableColumn<Order, String> restaurantColumn;
    @FXML private TableColumn<Order, Number> payPriceColumn;
    @FXML private TableColumn<Order, String> createdAtColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    private String token;
    private Stage stage;
    private final UserService userService = new UserService();
    private final CartService cartService = new CartService();
    private final CourierService courierService = new CourierService();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson;
    public CourierMainViewController(Stage stage, String token) {
        this.stage = stage;
        this.token = token;

        // پیکربندی Gson با TypeAdapter برای LocalDateTime
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

            @Override
            public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
                return LocalDateTime.parse(json.getAsString(), formatter);
            }
        });
        gson = gsonBuilder.create();
    }

    @FXML
    public void initialize() {
       // MenuButton menuButton = (MenuButton) stage.getScene().lookup("#toolBar").lookup(".menu-button");
        viewDeliveryHistoryItem = new MenuItem("تاریخچه تحویل‌ها");
        viewDeliveryHistoryItem.setOnAction(event -> showDeliveryHistory());
        menuButton.getItems().add(1, viewDeliveryHistoryItem);
        // تنظیم ستون‌های TableView
        orderIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId().toString()));
        deliveryAddressColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDeliveryAddress()));
        restaurantColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRestaurantName()));
        payPriceColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getPayPrice()));
        createdAtColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCreatedAt() != null
                        ? cellData.getValue().getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        : ""
        ));

        // تنظیم فرمت تاریخ برای ستون createdAtColumn
        createdAtColumn.setCellFactory(column -> new TableCell<Order, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
            }
        });

        // تنظیم ستون وضعیت با ComboBox
        statusColumn.setCellFactory(column -> new TableCell<Order, String>() {
            private final ComboBox<String> comboBox = new ComboBox<>();

            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    return;
                }

                comboBox.getItems().clear();
                if (status.equals("null") || status.isEmpty()) {
                    comboBox.getItems().add("accepted"); // فقط امکان پذیرش
                }
                else if (status.equals("accepted")) {
                    comboBox.getItems().addAll("received", "delivered");
                }
                else if (status.equals("received")) {
                    comboBox.getItems().add("delivered");
                }

                comboBox.setValue(status);
                comboBox.setOnAction(event -> {
                    String newStatus = comboBox.getValue();
                    Order order = getTableView().getItems().get(getIndex());
                    updateOrderStatus(order.getId().toString(), newStatus);
                });

                setGraphic(comboBox);
            }
        });

        // تنظیم استایل جدول
        ordersTable.setPlaceholder(new Label("هیچ سفارشی در دسترس نیست"));

        // بارگذاری سفارش‌ها
        showAvailableDeliveries();
    }
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


    @FXML
    private void showAvailableDeliveries() {
        if (token == null || token.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "خطا", "توکن کاربر یافت نشد");
            return;
        }

        CompletableFuture<List<Order>> future = courierService.getAvailableDeliveries(token);
        future.thenAcceptAsync(orders -> Platform.runLater(() -> {
            ordersTable.getItems().clear();
            ordersTable.getItems().addAll(orders != null ? orders : new ArrayList<>());
        })).exceptionally(throwable -> {
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "خطا", "خطا در دریافت سفارش‌ها: " + throwable.getMessage()));
            return null;
        });
    }
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private void updateOrderStatus(String orderId, String newStatus) {
        if (token == null || token.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "خطا", "توکن معتبر نیست!");
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("status", newStatus);
        String requestBody = gson.toJson(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/deliveries/" + orderId))
                .header("Authorization", token)
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> Platform.runLater(() -> {
                    System.out.println("Update status response: " + response.body());
                    if (response.statusCode() == 200) {
                        showAlert(Alert.AlertType.INFORMATION, "موفقیت", "وضعیت سفارش با موفقیت تغییر کرد.");
                        showAvailableDeliveries(); // به‌روزرسانی جدول
                    } else {
                        String errorMessage = response.body();
                        try {
                            Map<String, String> error = gson.fromJson(response.body(), new TypeToken<Map<String, String>>(){}.getType());
                            errorMessage = error.getOrDefault("error", response.body());
                        } catch (Exception ignored) {}
                        showAlert(Alert.AlertType.ERROR, "خطا", "خطا در تغییر وضعیت: " + errorMessage);
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "خطا", "خطا در ارتباط با سرور: " + ex.getMessage()));
                    return null;
                });
    }
    @FXML
    private void showDeliveryHistory() {
        System.out.println("Show delivery history clicked"); // تأیید کلیک

        if (token == null || token.trim().isEmpty()) {
            System.err.println("Token is null or empty");
            showAlert(Alert.AlertType.ERROR, "خطا", "توکن معتبر نیست!");
            return;
        }

        System.out.println("Fetching history with token: " + token); // تأیید توکن

        courierService.getDeliveryHistory(token)
                .thenAccept(orders -> {
                    System.out.println("Received " + (orders != null ? orders.size() : 0) + " orders"); // تأیید داده‌ها
                    Platform.runLater(() -> {
                        ordersTable.getItems().clear();
                        if (orders == null || orders.isEmpty()) {
                            showAlert(Alert.AlertType.INFORMATION, "اطلاعات", "هیچ سفارشی یافت نشد.");
                        } else {
                            orders.sort((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));
                            ordersTable.getItems().addAll(orders);
                        }
                    });
                })
                .exceptionally(throwable -> {
                    System.err.println("Error fetching history: " + throwable.getMessage()); // خطای دقیق
                    Platform.runLater(() ->
                            showAlert(Alert.AlertType.ERROR, "خطا", "خطا در دریافت تاریخچه: " + throwable.getMessage()));
                    return null;
                });
    }
}