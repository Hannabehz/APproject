package service;

import DAO.MenuDAO;
import DAO.FoodDAO;
import DAO.RestaurantDAO;
import entity.Menu;
import entity.Food;
import org.hibernate.Session;
import util.HibernateUtil;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MenuService {
    private final MenuDAO menuDAO = new MenuDAO();
    private final FoodDAO foodDAO = new FoodDAO();
    private final RestaurantDAO restaurantDAO = new RestaurantDAO();
    public boolean createMenu(UUID restaurant, String title) {
        if (restaurant == null || title == null || title.trim().isEmpty()) return false;

        // بررسی اینکه عنوان منو تکراری نباشه
        Menu existing = menuDAO.findByRestaurantAndTitle(restaurant, title.trim()).get();
        if (existing != null) return false;

        Menu menu = new Menu(title.trim(), restaurantDAO.findById(restaurant).get());
        menuDAO.save(menu);
        return true;
    }

    public boolean deleteMenu(UUID restaurantId, String title) {
        Menu menu = menuDAO.findByRestaurantAndTitle(restaurantId, title).get();
        if (menu == null) return false;

        menuDAO.delete(menu);
        return true;
    }

    public boolean addFoodToMenu(UUID restaurantId, String menuTitle, UUID foodId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            Menu menu = menuDAO.findByRestaurantAndTitle(restaurantId, menuTitle).get();
            if (menu == null) {
                tx.rollback();
                return false;
            }

            Food food = session.get(Food.class, foodId);
            if (food == null || food.getRestaurant() == null || !food.getRestaurant().getId().equals(restaurantId)) {
                tx.rollback();
                return false;
            }

            menu.addItem(food);
            session.update(menu);

            tx.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Menu> getMenusByRestaurant(UUID restaurantId) {
        return menuDAO.findMenusByRestaurant(restaurantId);
    }


    public boolean removeFoodFromMenu(UUID restaurantId, String menuTitle, UUID foodId) {
        Menu menu = menuDAO.findByRestaurantAndTitle(restaurantId, menuTitle).get();
        if (menu == null) return false;

        Optional<Food> foodOpt = menu.getItems().stream()
                .filter(f -> f.getId().equals(foodId))
                .findFirst();
        if (foodOpt.isEmpty()) return false;

        menuDAO.removeItemFromMenu(menu, foodOpt.get());
        return true;
    }
}