package com.akka.example02;

import akka.actor.*;

public class Main2 {

	public static void main(String[] args) {
		//创建ActorSystem.create(名称 或 default为无参)
		ActorSystem system = ActorSystem.create("Hello");
		//创建Actor
		//Props(道具)是一个配置类, Props.create(指定类.class, 指定类实例):创建角色选项
		ActorRef helloWorldActor = system.actorOf(Props.create(HelloWorld.class), "helloWorld");
		system.actorOf(Props.create(Terminator.class, helloWorldActor), "terminator");
	}
	
	public static class Terminator extends AbstractLoggingActor {
	
		private final ActorRef ref;
		//Props.create(指定类.class, 指定类实例):创建角色选项第二个参数为 这个生成者的参数helloWorldActor -> (ActorRef ref)
		public Terminator(ActorRef ref) {
			this.ref = ref;
			//watch指定Actor, 方法watch从监控另一个角色生命活力
			getContext().watch(ref);
		}
		
		@Override
		public Receive createReceive() {
			return receiveBuilder()
				.match(Terminated.class, t -> {
					
					log().info("{} has terminated, shutting down system", ref.path());//akka://Hello/user/helloWorld
					//终止此演员系统.这将阻止守护者演员,递归停止其所有子角色
					getContext().system().terminate();
				})
				.build();
		}
	}

}
