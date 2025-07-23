package controller;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.OrderResponseDTO;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class HistoryViewController {
    @FXML private TableView<OrderResponseDTO> ordersTable;
    @FXML private TableColumn<OrderResponseDTO, String> orderIdColumn;
    @FXML private TableColumn<OrderResponseDTO, String> deliveryAddressColumn;
    @FXML private TableColumn<OrderResponseDTO, String> restaurantColumn;
    @FXML private TableColumn<OrderResponseDTO, Number> payPriceColumn;
    @FXML private TableColumn<OrderResponseDTO, LocalDateTime> createdAtColumn;
    @FXML private TableColumn<OrderResponseDTO, Void> ratingColumn;
    private final Gson gson;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private String token;
    private Stage stage;
    public HistoryViewController() {
        // پیکربندی Gson با TypeAdapter برای LocalDateTime
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

            @Override
            public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                    throws JsonParseException {
                return LocalDateTime.parse(json.getAsString(), formatter);
            }
        });
        gson = gsonBuilder.create();
    }
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setToken(String token) {
        this.token = token;
        System.out.println("Token set in HistoryViewController: " + token);
        loadOrderHistory(); // فراخوانی بعد از تنظیم توکن
    }

    @FXML
    public void initialize() {
        if (ordersTable == null) {
            System.err.println("Error: ordersTable is null. Check FXML binding.");
            showAlert(Alert.AlertType.ERROR, "خطا", "خطا در بارگذاری جدول سفارشات");
            return;
        }

        // تنظیم ستون‌های TableView
        orderIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrderId().toString()));
        deliveryAddressColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDeliveryAddress()));
        restaurantColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRestaurantName()));
        payPriceColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPayPrice()));
        createdAtColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCreatedAt()));

        // فرمت تاریخ
        createdAtColumn.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });
        ratingColumn.setCellFactory(column -> new TableCell<>() {
            private final Button rateButton = new Button();
            {
                ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/images/rate_icon.png")));
                icon.setFitWidth(20);
                icon.setFitHeight(20);
                rateButton.setGraphic(icon);
                rateButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                rateButton.setOnAction(event -> {
                    OrderResponseDTO order = getTableView().getItems().get(getIndex());
                    openRatingDialog(order.getOrderId().toString());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    OrderResponseDTO order = getTableView().getItems().get(getIndex());
                    if ("delivered".equalsIgnoreCase(order.getDeliveryStatus())) {
                        setGraphic(rateButton);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
        }
    private void loadOrderHistory() {
        if (token == null || token.trim().isEmpty()) {
            System.err.println("Error: Token is null or empty in loadOrderHistory");
            showAlert(Alert.AlertType.ERROR, "خطا", "توکن احراز هویت موجود نیست");
            return;
        }

        String authHeader = token.startsWith("Bearer ") ? token : "Bearer " + token;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/orders/history"))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        CompletableFuture.supplyAsync(() -> {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String body = response.body();
                System.out.println("Raw response from /orders/history: " + body);
                if (response.statusCode() == 200) {
                    List<OrderResponseDTO> orders = gson.fromJson(body, new TypeToken<List<OrderResponseDTO>>(){}.getType());
                    return orders != null ? orders : new ArrayList<OrderResponseDTO>();
                } else {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "خطا", "خطا در دریافت تاریخچه سفارشات: کد " + response.statusCode()));
                    return new ArrayList<OrderResponseDTO>();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "خطا", "خطا در ارتباط با سرور: " + e.getMessage()));
                return new ArrayList<OrderResponseDTO>();
            }
        }).thenAcceptAsync(orders -> Platform.runLater(() -> {
            ordersTable.getItems().clear();
            ordersTable.getItems().addAll(orders);
        }));
    }
    private void openRatingDialog(String orderId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo1/ratingDialog.fxml"));
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(stage);
            dialogStage.setTitle("ثبت نظر");
            dialogStage.setScene(new Scene(loader.load()));
            RatingDialogController controller = loader.getController();
            controller.setStage(dialogStage);
            controller.setToken(token);
            controller.setOrderId(orderId);
            dialogStage.showAndWait();
            loadOrderHistory(); // رفرش جدول بعد از ثبت نظر
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "خطا", "خطا در باز کردن پنجره ثبت نظر: " + e.getMessage());
        }
    }
    private void showAlert(Alert.AlertType type, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}