package com.akka.example01;

import com.akka.example01.Printer.Greeting;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class Greeter extends AbstractActor {

	//Printer类的ActorReference
	private final ActorRef printerActor;
	private final String message;
	private String greeting = "";

	public Greeter(String message, ActorRef printerActor) {
		this.message = message;
		this.printerActor = printerActor;
	}
	
	static public Props props(String message, ActorRef printerActor) {
		return Props.create(Greeter.class, () -> new Greeter(message, printerActor));
	}
	
	static public class Greet {
		public Greet() {}
	}

	static public class WhoToGreet {
		public final String who;
		
		public WhoToGreet(String who) {
			this.who = who;
		}
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.match(WhoToGreet.class, wtg -> {
				this.greeting = message + ", " + wtg.who;
			})
			.match(Greet.class, z -> {
				//在main方法内actorOf创建 ActorReference配Greeter.props时类实例传递的参数为printerActor
				//tell异步发送一个消息到Printer.createReceive
				printerActor.tell(new Greeting(greeting), getSelf());
			})
			.build();
	}

}
