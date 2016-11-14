import akka.http.scaladsl.server.{ Directives, PathMatchers }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.Connection
import akka.http.scaladsl.model.HttpRequest
import akka.actor.ActorSystem
import akka.stream.{ ActorMaterializer, Materializer }
import com.typesafe.config.ConfigFactory
import scala.util.{ Failure, Success }

class Proxy(implicit system: ActorSystem, materializer: Materializer) extends Directives {
  import Proxy._

  val extractedInfo = extractUnmatchedPath & extractRequest

  val route = extractedInfo { (path, req) =>
    complete {
      Http().singleRequest(transformRequest(req))
    }
  }

  def bindingFuture = Http().bindAndHandle(handler = route, interface = "0.0.0.0", port = 50000)
}

object Proxy extends App {
  val config = ConfigFactory.parseString("akka.http.server.proxy-mode = on").withFallback(ConfigFactory.load())
  implicit val system = ActorSystem("Proxy", config)
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val headersToRemove = Array(
    // Akka balks at remote-address and timeout-access in client requests
    "Remote-Address",
    "Timeout-Access",
    // The rest are hop-by-hop headers that must not be propagated
    // (https://tools.ietf.org/html/rfc2616#section-13.5.1)
    "Connection",
    "Keep-Alive",
    "Proxy-Authenticate",
    "Proxy-Authorization",
    "TE",
    "Trailers",
    "Transfer-Encoding",
    "Upgrade"
  )

  // A request transformer that removes headers that are invalid on HTTP requests
  val transformRequest = (req: HttpRequest) => {
    headersToRemove.foldLeft(req) { (req, name) =>
      req.removeHeader(name)
    }.copy(uri = req.uri.withHost("localhost").withPort(50001))
  }

  val server = new Proxy

  server.bindingFuture.onComplete {
    case Success(_) =>
      println(s"Server listening on port: 50000")
    case Failure(reason) =>
      println(s"Server failed to bind to port 50000.", reason)
      system.terminate()
  }
}
