package httpRequestHandler;

import DAO.MenuDAO;
import DAO.UserDAO;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import entity.Menu;
import dto.MenuDTO;
import service.MenuService;
import service.UserService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MenuHandler implements HttpHandler {
    private final MenuService menuService;
    private final Gson gson;

    public MenuHandler() {
        this.menuService = new MenuService();
        this.gson = new Gson();
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/menu")) {}
    }
    public boolean createMenu(UUID restaurantId, MenuDTO dto) {
        return menuService.createMenu(restaurantId, dto.getTitle());
    }

    public boolean deleteMenu(UUID restaurantId, String title) {
        return menuService.deleteMenu(restaurantId, title);
    }

    public boolean addFood(UUID restaurantId, String title, UUID foodId) {
        return menuService.addFoodToMenu(restaurantId, title, foodId);
    }

    public boolean removeFood(UUID restaurantId, String title, UUID foodId) {
        return menuService.removeFoodFromMenu(restaurantId, title, foodId);
    }

    public List<MenuDTO> getMenusOfRestaurant(UUID restaurantId) {
        List<Menu> menus = menuService.getMenusByRestaurant(restaurantId);
        return menus.stream()
                .map(menu -> new MenuDTO(menu.getTitle()))
                .collect(Collectors.toList());
    }
}