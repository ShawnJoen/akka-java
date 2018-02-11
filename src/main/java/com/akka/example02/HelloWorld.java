package com.akka.example02;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import static com.akka.example02.Greeter.Msg;

public class HelloWorld extends AbstractActor {

  @Override
  public Receive createReceive() {
    return receiveBuilder()
      .matchEquals(Msg.DONE, m -> {
        //停止此Actor
        getContext().stop(self());
      })
      .build();
  }

  //preStart()，Actor启动之前调用，用于完成初始化工作
  @Override
  public void preStart() {
    //getContext()为此Actor的上下文
	//actorOf创建Actor
    final ActorRef greeter = getContext().actorOf(Props.create(Greeter.class), "greeter");
    //调用Actor发送消息tell(消息, 发送者Actor)
    greeter.tell(Msg.GREET, self());
  }
}
