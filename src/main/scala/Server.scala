import akka.http.scaladsl.server.{ Directives, PathMatchers }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.Connection
import akka.http.scaladsl.model.StatusCodes.MovedPermanently
import akka.http.scaladsl.model.HttpRequest
import akka.actor.ActorSystem
import akka.stream.{ ActorMaterializer, Materializer }
import scala.util.{ Failure, Success }
import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext }

class Server(implicit system: ActorSystem, materializer: Materializer, ec: ExecutionContext) extends Directives with PathMatchers {
  val route = path("endpoint" / IntNumber) { i =>
    redirect("/nowhere", MovedPermanently)
  }

  def bindingFuture = Http().bindAndHandle(handler = route, interface = "0.0.0.0", port = 50001)
}

object Server extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val server = new Server

  server.bindingFuture.onComplete {
    case Success(_) =>
      println(s"Server listening on port: 50001")
    case Failure(reason) =>
      println(s"Server failed to bind to port 50001.", reason)
      system.terminate()
  }
}
