package com.akka.example03_http;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import java.util.concurrent.CompletionStage;

public class QuickstartServer extends AllDirectives {

    private final UserRoutes userRoutes;
    public QuickstartServer(ActorSystem system, ActorRef userRegistryActor) {
        userRoutes = new UserRoutes(system, userRegistryActor);
    }

    public static void main(String[] args) throws Exception {
        //创建ActorSystem
        ActorSystem system = ActorSystem.create("helloAkkaHttpServer");
        //创建http
        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);
        //创建Actor
        ActorRef userRegistryActor = system.actorOf(UserRegistryActor.props(), "userRegistryActor");
        //指定Route
        QuickstartServer app = new QuickstartServer(system, userRegistryActor);
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.createRoute().flow(system, materializer);
        //绑定端口开启http服务器
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow,
                ConnectHttp.toHost("localhost", 8080), materializer);

        System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
        System.in.read(); // let it run until user presses return
        
        binding
                .thenCompose(ServerBinding::unbind) // trigger unbinding from the port
                .thenAccept(unbound -> system.terminate()); // and shutdown when done
    }

    /**
     * Here you can define all the different routes you want to have served by this web server
     * Note that routes might be defined in separated classes like the current case
     */
    protected Route createRoute() {
        return userRoutes.routes();
    }
}
