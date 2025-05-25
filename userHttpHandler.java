package httpRequestHandler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dto.userdto;
import service.userService;

import java.io.IOException;
import java.io.InputStreamReader;

public class userHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if("POST".equals(exchange.getRequestMethod())){
            String path=exchange.getRequestURI().getPath();
            if(path.equals("/user/signup")){
                handleSignUp(exchange);
            }
        }
    }
    private void handleSignUp(HttpExchange exchange) throws IOException {
        userService service=new userService();
        InputStreamReader stream = new InputStreamReader(exchange.getRequestBody());
        userdto dto = new Gson().fromJson(stream,userdto.class);
        service.save(dto);
    }
}
