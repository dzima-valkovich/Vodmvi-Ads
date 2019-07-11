package controllers;

import play.*;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {

    public Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public Result hello() {
        return ok(hello.render("World"));
    }

    public Result hello(String name) {
        return ok(hello.render(name));
    }
}
