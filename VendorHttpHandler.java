/*
package httpRequestHandler;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import entity.Restaurant;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.IOException;
import service.VendorService;
import util.JwtUtil;

public class VendorHttpHandler implements HttpHandler {
    private final VendorService vendorService;
    private final Gson gson;
    public VendorHttpHandler(VendorService vendorService) {
        this.vendorService = vendorService;
    }
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        if ("/vendors".equals(path) && "POST".equals(method)) {
            handleListVendors(exchange);
        } else if (path.matches("/vendors/[^/]+/items") && "GET".equals(method)) {
            handleGetVendorItems(exchange);
        } else {
            exchange.sendResponse(404, -1);
            exchange.close();
        }
    }
    private void handleListVendors(HttpExchange exchange) throws IOException {

}
*/
