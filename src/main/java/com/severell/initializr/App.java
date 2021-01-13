package com.severell.initializr;

import com.severell.core.config.Config;
import com.severell.core.container.Container;
import com.severell.core.http.AppServer;
import com.severell.core.http.Router;
import com.severell.core.providers.ServiceProvider;
import com.severell.initializr.auth.Auth;
import com.severell.initializr.action.GeneratorException;
import com.severell.initializr.action.template.TemplateGenerator;
import com.severell.initializr.models.MavenBuildTransformer;
import com.severell.initializr.models.parameter.TemplateParameter;

import javax.naming.NamingException;
import java.util.ArrayList;


public class App {

    public static void main(String[] args) throws NamingException, GeneratorException {
        try {
            Config.loadConfig();
        }catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        Container c = new Container();
        c.singleton("_MiddlewareList", Middleware.MIDDLEWARE);
        c.singleton(Auth.class, new Auth());

        AppServer server = new AppServer(Config.get("PORT", "8080"));
        c.singleton(AppServer.class, server);

        ServiceProvider[] providers = Providers.load(c);

        for(ServiceProvider provider : providers) {
            provider.register();
        }

        TemplateParameter templateParameter = new TemplateParameter();
        TemplateGenerator templateGenerator = new TemplateGenerator(templateParameter, new MavenBuildTransformer());
        c.singleton(TemplateGenerator.class, templateGenerator);

//        boolean generated = templateGenerator.generate();
//        if(generated) {
//        }else{
//            throw new GeneratorException("Unable to build template");
//        }

        try {
            RouteBuilder builder = new RouteBuilder();
            ArrayList routes = builder.build();
            Router.setCompiledRoutes(routes);
            c.singleton("DefaultMiddleware", builder.buildDefaultMiddleware());
        }catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        for(ServiceProvider provider : providers) {
            try {
                provider.boot();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
