package ru.livetex.robot

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import spray.can.Http

import scala.concurrent.duration._


object RobotApp extends App {
  implicit val timeout = Timeout(5.seconds)
  implicit val system = ActorSystem("ru-livetex-robot")

  IO(Http) ? Http.Bind(
    system.actorOf(Props[Assistant]), interface = "localhost", port = 8080)
}
