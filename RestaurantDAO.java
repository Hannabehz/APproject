package DAO;

import dto.FoodDTO;
import dto.MenuResponseDTO;
import entity.Food;
import entity.Menu;
import entity.Restaurant;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import util.HibernateUtil;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;
import java.util.stream.Collectors;

public class RestaurantDAO {

    public void save(Restaurant restaurant) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.save(restaurant);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Failed to save restaurant: " + e.getMessage());
        }
    }

    public List<Restaurant> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Restaurant> cq = cb.createQuery(Restaurant.class);
            Root<Restaurant> root = cq.from(Restaurant.class);
            cq.select(root);
            return session.createQuery(cq).getResultList();
        }
    }

    public void delete(Restaurant restaurant) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.delete(restaurant);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Failed to delete restaurant: " + e.getMessage());
        }
    }
    public List<Restaurant> findRestaurants(String search, List<String> categories) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Restaurant r WHERE 1=1";
            if (search != null && !search.trim().isEmpty()) {
                hql += " AND (r.name LIKE :search OR r.address LIKE :search)";
            }
            if (categories != null && !categories.isEmpty()) {
                hql += " AND r.category IN :categories";
            }

            Query<Restaurant> query = session.createQuery(hql, Restaurant.class);
            if (search != null && !search.trim().isEmpty()) {
                query.setParameter("search", "%" + search + "%");
            }
            if (categories != null && !categories.isEmpty()) {
                query.setParameter("categories", categories);
            }

            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Error in findRestaurants: " + e.getMessage());
            throw new RuntimeException("Failed to fetch restaurants: " + e.getMessage());
        }
    }
    public MenuResponseDTO findMenusAndItemsByRestaurantId(UUID restaurantId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // دریافت رستوران
            Restaurant restaurant = session.get(Restaurant.class, restaurantId);
            if (restaurant == null) {
                System.err.println("Restaurant not found for ID: " + restaurantId);
                throw new RuntimeException("Restaurant not found");
            }

            // دریافت منوها
            String menuHql = "FROM Menu m WHERE m.restaurant.id = :restaurantId";
            Query<Menu> menuQuery = session.createQuery(menuHql, Menu.class);
            menuQuery.setParameter("restaurantId", restaurantId);
            List<Menu> menus = menuQuery.getResultList();

            // دریافت غذاهای بدون منو
            String foodHql = "FROM Food f WHERE f.restaurant.id = :restaurantId " +
                    "AND NOT EXISTS (SELECT 1 FROM Menu m JOIN m.items mi WHERE mi.id = f.id)";
            Query<Food> foodQuery = session.createQuery(foodHql, Food.class);
            foodQuery.setParameter("restaurantId", restaurantId);
            List<Food> miscFoods = foodQuery.getResultList();

            // تبدیل به DTO
            List<String> menuTitles = new ArrayList<>();
            Map<String, List<FoodDTO>> menusMap = new HashMap<>();

            // اضافه کردن منوهای رستوران
            for (Menu menu : menus) {
                menuTitles.add(menu.getTitle());
                List<FoodDTO> foodDTOs = menu.getItems().stream()
                        .map(food -> new FoodDTO(
                                food.getId().toString(),
                                food.getName(),
                                food.getImageBase64(),
                                food.getDescription(),
                                food.getVendorId().toString(),
                                food.getPrice(),
                                food.getSupply(),
                                food.getRate(),
                                food.getCategories()
                        ))
                        .collect(Collectors.toList());
                menusMap.put(menu.getTitle(), foodDTOs);
            }

            // اضافه کردن غذاهای متفرقه
            if (!miscFoods.isEmpty()) {
                menuTitles.add("متفرقه");
                List<FoodDTO> miscFoodDTOs = miscFoods.stream()
                        .map(food -> new FoodDTO(
                                food.getId().toString(),
                                food.getName(),
                                food.getImageBase64(),
                                food.getDescription(),
                                food.getVendorId().toString(),
                                food.getPrice(),
                                food.getSupply(),
                                food.getCategories()
                        ))
                        .collect(Collectors.toList());
                menusMap.put("متفرقه", miscFoodDTOs);
            }

            System.out.println("Found " + menuTitles.size() + " menu titles for restaurant ID " + restaurantId + ": " + menuTitles);
            return new MenuResponseDTO(menuTitles, menusMap);
        } catch (Exception e) {
            System.err.println("Error in findMenusAndItemsByRestaurantId: " + e.getMessage());
            throw new RuntimeException("Failed to fetch menus and items: " + e.getMessage());
        }
    }
    public List<FoodDTO> searchFoods(UUID restaurantId, String search, List<String> categories) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Food> cq = cb.createQuery(Food.class);
            Root<Food> root = cq.from(Food.class);

            // شرط‌های اصلی
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("restaurant").get("id"), restaurantId));

            // شرط جستجو
            if (search != null && !search.trim().isEmpty()) {
                predicates.add(cb.or(
                        cb.like(root.get("name"), "%" + search + "%"),
                        cb.like(root.get("description"), "%" + search + "%")
                ));
            }

            // شرط دسته‌بندی‌ها
            if (categories != null && !categories.isEmpty()) {
                predicates.add(root.get("categories").in(categories));
            }

            cq.where(predicates.toArray(new Predicate[0]));

            List<Food> foods = session.createQuery(cq).getResultList();

            // تبدیل به DTO
            return foods.stream()
                    .map(food -> new FoodDTO(
                            food.getId().toString(),
                            food.getName(),
                            food.getImageBase64(),
                            food.getDescription(),
                            food.getVendorId().toString(),
                            food.getPrice(),
                            food.getSupply(),
                            food.getRate(),
                            food.getCategories()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error in searchFoods: " + e.getMessage());
            throw new RuntimeException("Failed to search foods: " + e.getMessage());
        }
    }
    public Optional<Restaurant> findById(UUID id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(Restaurant.class, id));
        }
    }
    public List<Restaurant> findBySellerId(UUID sellerId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM entity.Restaurant r WHERE r.seller.id = :sellerId", Restaurant.class)
                    .setParameter("sellerId", sellerId)
                    .list();
        }
    }
    public void update(Restaurant restaurant) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.update(restaurant);
            session.getTransaction().commit();
        }
    }

}

