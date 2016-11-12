Illustrating Expect 100 and 301 Behaviour in Akka-Http
======================================================

This sets up a server (on port 50001) and a proxy (on port 50000), both written
in Akka-HTTP.

The server simply matches on /endpoint/N and then redirects to /nowhere.  The
proxy just sees whatever it gets and proxies it to the server.

How to Run
----------

In the first terminal, execute `sbt 'run-main Server'`
In a second terminal, execute `sbt 'run-main Proxy'`
In a third, execute `run`.

What You Should See
-------------------

Because the server redirects, it does not consume the request.  When that
request is a POST with an `Expect: 100-Continue` header, the server spits out
the error:

```
Sending an 2xx 'early' response before end of request was received...
Note that the connection will be closed after this response. Also, many clients
will not read early responses! Consider only issuing this response after the
request data has been completely read!
```

However, the proxy exhibits a few other problems.  The first is, given a large POST body payload:

```
[WARN] [11/11/2016 19:25:27.185] [default-akka.actor.default-dispatcher-58] [akka.actor.ActorSystemImpl(default)] Sending an 2xx 'early' response before end of request was received... Note that the connection will be closed after this response. Also, many clients will not read early responses! Consider only issuing this response after the request data has been completely read!
[ERROR] [11/11/2016 19:25:27.186] [default-akka.actor.default-dispatcher-62] [akka.actor.ActorSystemImpl(default)] Outgoing request stream error
akka.http.scaladsl.model.InvalidContentLengthException: HTTP message had declared Content-Length 3466986 but entity data stream amounts to 3303080 bytes less
	at akka.http.scaladsl.model.InvalidContentLengthException$.apply(ErrorInfo.scala:50)
	at akka.http.impl.engine.rendering.RenderSupport$CheckContentLengthTransformer$$anon$2.onUpstreamFinish(RenderSupport.scala:130)
	at akka.stream.impl.fusing.GraphInterpreter.processEvent(GraphInterpreter.scala:732)
	at akka.stream.impl.fusing.GraphInterpreter.execute(GraphInterpreter.scala:616)
	at akka.stream.impl.fusing.GraphInterpreterShell.runBatch(ActorGraphInterpreter.scala:471)
	at akka.stream.impl.fusing.GraphInterpreterShell.receive(ActorGraphInterpreter.scala:423)
	at akka.stream.impl.fusing.ActorGraphInterpreter.akka$stream$impl$fusing$ActorGraphInterpreter$$processEvent(ActorGraphInterpreter.scala:603)
	at akka.stream.impl.fusing.ActorGraphInterpreter$$anonfun$receive$1.applyOrElse(ActorGraphInterpreter.scala:618)
	at akka.actor.Actor$class.aroundReceive(Actor.scala:484)
	at akka.stream.impl.fusing.ActorGraphInterpreter.aroundReceive(ActorGraphInterpreter.scala:529)
	at akka.actor.ActorCell.receiveMessage(ActorCell.scala:526)
	at akka.actor.ActorCell.invoke(ActorCell.scala:495)
	at akka.dispatch.Mailbox.processMailbox(Mailbox.scala:257)
	at akka.dispatch.Mailbox.run(Mailbox.scala:224)
	at akka.dispatch.Mailbox.exec(Mailbox.scala:234)
	at scala.concurrent.forkjoin.ForkJoinTask.doExec(ForkJoinTask.java:260)
	at scala.concurrent.forkjoin.ForkJoinPool$WorkQueue.runTask(ForkJoinPool.java:1339)
	at scala.concurrent.forkjoin.ForkJoinPool.runWorker(ForkJoinPool.java:1979)
	at scala.concurrent.forkjoin.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:107)
```

And given a small POST body payload:

```
[WARN] [11/11/2016 19:27:17.305] [default-akka.actor.default-dispatcher-60] [akka.actor.ActorSystemImpl(default)] Sending an 2xx 'early' response before end of request was received... Note that the connection will be closed after this response. Also, many clients will not read early responses! Consider only issuing this response after the request data has been completely read!
[ERROR] [11/11/2016 19:27:17.305] [default-akka.actor.default-dispatcher-60] [akka://default/user/StreamSupervisor-0/flow-7117-0-unknown-operation] Error in stage [akka.http.impl.engine.server.HttpServerBluePrint$ControllerStage@6d57cc42]: requirement failed: Cannot pull port (requestParsingIn) twice
java.lang.IllegalArgumentException: requirement failed: Cannot pull port (requestParsingIn) twice
	at scala.Predef$.require(Predef.scala:224)
	at akka.stream.stage.GraphStageLogic.pull(GraphStage.scala:355)
	at akka.http.impl.engine.server.HttpServerBluePrint$ControllerStage$$anon$12$$anon$15.onPush(HttpServerBluePrint.scala:432)
	at akka.stream.impl.fusing.GraphInterpreter.processPush(GraphInterpreter.scala:747)
	at akka.stream.impl.fusing.GraphInterpreter.execute(GraphInterpreter.scala:649)
	at akka.stream.impl.fusing.GraphInterpreterShell.runBatch(ActorGraphInterpreter.scala:471)
	at akka.stream.impl.fusing.GraphInterpreterShell.receive(ActorGraphInterpreter.scala:423)
	at akka.stream.impl.fusing.ActorGraphInterpreter.akka$stream$impl$fusing$ActorGraphInterpreter$$processEvent(ActorGraphInterpreter.scala:603)
	at akka.stream.impl.fusing.ActorGraphInterpreter$$anonfun$receive$1.applyOrElse(ActorGraphInterpreter.scala:618)
	at akka.actor.Actor$class.aroundReceive(Actor.scala:484)
	at akka.stream.impl.fusing.ActorGraphInterpreter.aroundReceive(ActorGraphInterpreter.scala:529)
	at akka.actor.ActorCell.receiveMessage(ActorCell.scala:526)
	at akka.actor.ActorCell.invoke(ActorCell.scala:495)
	at akka.dispatch.Mailbox.processMailbox(Mailbox.scala:257)
	at akka.dispatch.Mailbox.run(Mailbox.scala:224)
	at akka.dispatch.Mailbox.exec(Mailbox.scala:234)
	at scala.concurrent.forkjoin.ForkJoinTask.doExec(ForkJoinTask.java:260)
	at scala.concurrent.forkjoin.ForkJoinPool$WorkQueue.runTask(ForkJoinPool.java:1339)
	at scala.concurrent.forkjoin.ForkJoinPool.runWorker(ForkJoinPool.java:1979)
	at scala.concurrent.forkjoin.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:107)

[ERROR] [11/11/2016 19:27:17.305] [default-akka.actor.default-dispatcher-59] [akka.actor.ActorSystemImpl(default)] Outgoing request stream error
java.lang.IllegalArgumentException: requirement failed: Cannot pull port (requestParsingIn) twice
	at scala.Predef$.require(Predef.scala:224)
	at akka.stream.stage.GraphStageLogic.pull(GraphStage.scala:355)
	at akka.http.impl.engine.server.HttpServerBluePrint$ControllerStage$$anon$12$$anon$15.onPush(HttpServerBluePrint.scala:432)
	at akka.stream.impl.fusing.GraphInterpreter.processPush(GraphInterpreter.scala:747)
	at akka.stream.impl.fusing.GraphInterpreter.execute(GraphInterpreter.scala:649)
	at akka.stream.impl.fusing.GraphInterpreterShell.runBatch(ActorGraphInterpreter.scala:471)
	at akka.stream.impl.fusing.GraphInterpreterShell.receive(ActorGraphInterpreter.scala:423)
	at akka.stream.impl.fusing.ActorGraphInterpreter.akka$stream$impl$fusing$ActorGraphInterpreter$$processEvent(ActorGraphInterpreter.scala:603)
	at akka.stream.impl.fusing.ActorGraphInterpreter$$anonfun$receive$1.applyOrElse(ActorGraphInterpreter.scala:618)
	at akka.actor.Actor$class.aroundReceive(Actor.scala:484)
	at akka.stream.impl.fusing.ActorGraphInterpreter.aroundReceive(ActorGraphInterpreter.scala:529)
	at akka.actor.ActorCell.receiveMessage(ActorCell.scala:526)
	at akka.actor.ActorCell.invoke(ActorCell.scala:495)
	at akka.dispatch.Mailbox.processMailbox(Mailbox.scala:257)
	at akka.dispatch.Mailbox.run(Mailbox.scala:224)
	at akka.dispatch.Mailbox.exec(Mailbox.scala:234)
	at scala.concurrent.forkjoin.ForkJoinTask.doExec(ForkJoinTask.java:260)
	at scala.concurrent.forkjoin.ForkJoinPool$WorkQueue.runTask(ForkJoinPool.java:1339)
	at scala.concurrent.forkjoin.ForkJoinPool.runWorker(ForkJoinPool.java:1979)
	at scala.concurrent.forkjoin.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:107)
```

The third is something I can't reproduce in such a small use case and this is
the most insidious one, unfortunately.  It manifests in the curl behaviour, which essentially sees:

```
< HTTP/1.1 100 Continue
< Server: akka-http/2.4.10
< Date: Fri, 11 Nov 2016 21:29:26 GMT
* Empty reply from server
* Connection #0 to host <host goes here> left intact
curl: (52) Empty reply from server
```

So, basically it doesn't get the 301 and the command fails.  I'm hoping that
these previous issues can point at why the 52 error might occur.  It's
currently hurting us in production.

