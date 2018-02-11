package com.akka.example02;

public class Main {

  public static void main(String[] args) {
	//通用启动器类akka.Main,只接收一个命令行参数: 应用的主actor类名
    akka.Main.main(new String[] { HelloWorld.class.getName() });
  }
}
