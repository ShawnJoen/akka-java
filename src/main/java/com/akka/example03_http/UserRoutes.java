package com.akka.example03_http;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.akka.example03_http.UserRegistryActor.User;
import com.akka.example03_http.UserRegistryMessages.ActionPerformed;
import com.akka.example03_http.UserRegistryMessages.CreateUser;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static akka.japi.Util.classTag;
import static scala.compat.java8.FutureConverters.toJava;

/**
 * Routes can be defined in separated classes like shown in here
 */
public class UserRoutes extends AllDirectives {

    final private ActorRef userRegistryActor;
    final private LoggingAdapter log;
    public UserRoutes(ActorSystem system, ActorRef userRegistryActor) {
 
        this.userRegistryActor = userRegistryActor;
        
        log = Logging.getLogger(system, this);
    }

    // Required by the `ask` (?) method below
    Timeout timeout = new Timeout(Duration.create(5, TimeUnit.SECONDS)); // usually we'd obtain the timeout from the system's configuration

    /**
     * This method creates one route (of possibly many more that will be part of your Web App)
     */
    //#all-routes
    public Route routes() {
        return route(pathPrefix("users", () ->
                route(
	                pathEnd(() ->
	                    route(
                            get(() -> {
		                            Future<UserRegistryActor.Users> futureUsers = 
		                            			Patterns.ask(userRegistryActor, new UserRegistryMessages.GetUsers(), timeout)
		                            				.mapTo(classTag(UserRegistryActor.Users.class));
		                            return onSuccess(() -> toJava(futureUsers),
		                                users -> complete(StatusCodes.OK, users, Jackson.marshaller()));
		                            }
                            ),
                            post(() -> entity(Jackson.unmarshaller(User.class), user -> {
                                    Future<ActionPerformed> userCreated =
                                    		Patterns.ask(userRegistryActor, new CreateUser(user), timeout)
                                                .mapTo(classTag(ActionPerformed.class));
                                    return onSuccess(() -> toJava(userCreated),
                                        performed -> {
                                            log.info("Created user [{}]: {}", user.getName(), performed.getDescription());
                                            return complete(StatusCodes.CREATED, performed, Jackson.marshaller());
                                        });
                                })
                           )
	                    )
	                ),
                    path(PathMatchers.segment(),
                        name -> route(
                                get(() -> {
                                    //获取指定用户信息
                                	//Patterns.ask(Actor, 消息内容, 延迟时间): 发送一条包含 Future 的消息作为响应
                                	//mapTo方法返回将返回结果映射为classTag(Optional.class)类型
                                    Future<Optional> maybeUser = 
                                    		Patterns.ask(userRegistryActor, new UserRegistryMessages.GetUser(name), timeout)
                                            	.mapTo(classTag(Optional.class));
                                    return rejectEmptyResponse(() ->
                                            onSuccess(() -> toJava(maybeUser),
                                            performed  -> complete(StatusCodes.OK, (User)performed.get(), Jackson.<User>marshaller())));
                                }),
                                //#users-delete-logic
                                delete(() -> {
                                    Future<ActionPerformed> userDeleted =
                                    		Patterns.ask(userRegistryActor, new UserRegistryMessages.DeleteUser(name), timeout)
                                        		.mapTo(classTag(ActionPerformed.class));
                                    return onSuccess(() -> toJava(userDeleted),
                                        performed -> {
                                            log.info("Deleted user [{}]: {}", name, performed.getDescription());
                                            return complete(StatusCodes.OK, performed, Jackson.marshaller());
                                        }

                                    );
                                })
	                                //#users-delete-logic
	                        )
                    )
                )
        	)
        );
    }
    //#all-routes
}
