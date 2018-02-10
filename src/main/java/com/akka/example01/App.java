package com.akka.example01;

import com.akka.example01.Greeter.*;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class App {
	
	public static void main(String[] args) {
		//创建ActorSystem.create(名称 或 default为无参)
		final ActorSystem system = ActorSystem.create("System01");
		try {
			//ActorSystem.actorOf创建ActorReference, actorOf(配置类, 名称)
			//Props(道具)是一个配置类, Props.create(指定类.class, 指定类实例):创建角色选项
			final ActorRef printerActor = system.actorOf(Printer.props(), "printerActor");
			//
			final ActorRef howdyGreeter = system.actorOf(Greeter.props("Howdy", printerActor), "howdyGreeter");
			final ActorRef goodDayGreeter = system.actorOf(Greeter.props("Good day", printerActor), "goodDayGreeter");

			//tell(new 类, 第一个参数的实例)
			//当前发送消息不在Actor内部所以发送者无实现类。因此发送ActorRef.noSender()(null)
			//Greeter.createReceive第一个match: 给Greeter的内部类WhoToGreet配置Message信息
			howdyGreeter.tell(new WhoToGreet("Akka"), ActorRef.noSender());
			//Greeter.createReceive第二个match: tell异步发送一个消息到Printer.createReceive
			howdyGreeter.tell(new Greet(), ActorRef.noSender());

			howdyGreeter.tell(new WhoToGreet("Lightbend"), ActorRef.noSender());
			howdyGreeter.tell(new Greet(), ActorRef.noSender());

			goodDayGreeter.tell(new WhoToGreet("Play"), ActorRef.noSender());
			goodDayGreeter.tell(new Greet(), ActorRef.noSender());
			
			/*
			 * 相同ActorRef的话执行顺序为先进先出
			 * 不同ActorRef的话顺序随机
			 * */
	    } catch (Exception e) {
	    	e.printStackTrace();
	    } finally {
	    	system.terminate();
	    }
	}
}