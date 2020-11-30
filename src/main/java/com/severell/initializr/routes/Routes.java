package com.severell.initializr.routes;

import com.severell.core.http.Router;
import com.severell.initializr.controller.MainController;

public class Routes {

    public static void init() throws NoSuchMethodException, ClassNotFoundException {
        Router.Get("/", MainController.class, "index");
    }
}
