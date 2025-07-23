package service;

import DAO.FoodDAO;
import entity.Food;
import entity.Restaurant;

import java.util.List;
import java.util.UUID;

public class FoodService {
    private final FoodDAO foodDAO = new FoodDAO();

    public boolean createFood(String name, double price, int quantity, List<String> categories, Restaurant restaurant) {
        if (name == null || name.trim().isEmpty() || price < 0 || quantity < 0 || restaurant == null)
            return false;

        Food food = new Food(name.trim(), price, quantity, restaurant);
        if (categories != null && !categories.isEmpty()) {
            food.setCategories(categories);
        }

        foodDAO.save(food);
        return true;
    }

    public Food getFoodById(UUID id) {
        return foodDAO.findById(id).get();
    }

    public List<Food> getAllFoods() {
        return foodDAO.findAll();
    }

    public List<Food> getFoodsByRestaurant(UUID restaurantId) {
        return foodDAO.findAllByRestaurant(restaurantId);
    }

    public boolean updateFood(UUID id, String newName, Integer newPrice, Integer newQuantity, List<String> newCategories) {
        Food food = foodDAO.findById(id).get();
        if (food == null) return false;

        if (newName != null && !newName.trim().isEmpty()) food.setName(newName.trim());
        if (newPrice != null && newPrice >= 0) food.setPrice(newPrice);
        if (newQuantity != null && newQuantity >= 0) food.setSupply(newQuantity);
        if (newCategories != null && !newCategories.isEmpty()) food.setCategories(newCategories);

        foodDAO.save(food);
        return true;
    }

    public boolean deleteFood(UUID id) {
        Food food = foodDAO.findById(id).get();
        if (food == null) return false;

        foodDAO.delete(food);
        return true;
    }
}