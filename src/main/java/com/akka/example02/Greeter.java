package com.akka.example02;

import akka.actor.AbstractActor;

public class Greeter extends AbstractActor {

  public static enum Msg {
    GREET, DONE;
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
      .matchEquals(Msg.GREET, m -> {
    	
        System.out.println("Hello World!");
        //处理完后同时发送消息tell(消息, 发送者Actor)
        sender().tell(Msg.DONE, self());
      })
      .build();
  }
}
