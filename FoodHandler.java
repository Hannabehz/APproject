package httpRequestHandler;

import entity.Food;
import entity.Restaurant;
import dto.FoodDTO;
import service.FoodService;
import service.RestaurantService;

import java.util.UUID;

public class FoodHandler {
    private final FoodService foodService = new FoodService();
    private final RestaurantService restaurantService = new RestaurantService();

    public boolean registerFood(UUID restaurantId, FoodDTO dto, int quantity) {
        Restaurant rest = restaurantService.getRestaurantById(restaurantId);
        if (rest == null) return false;

        return foodService.createFood(dto.getName(), dto.getPrice(), quantity, dto.getCategories(), rest);
    }

    public boolean updateFood(UUID foodId, FoodDTO dto, int quantity) {
        return foodService.updateFood(foodId, dto.getName(), dto.getPrice(), quantity, dto.getCategories());
    }

    public boolean removeFood(UUID foodId) {
        return foodService.deleteFood(foodId);
    }

    public FoodDTO getFoodDetails(UUID foodId) {
        Food food = foodService.getFoodById(foodId);
        if (food == null) return null;
        FoodDTO foodDTO = new FoodDTO();
        foodDTO.setName(food.getName());
        foodDTO.setPrice(food.getPrice());
        foodDTO.setCategories(food.getCategories());
        return foodDTO;
    }
}