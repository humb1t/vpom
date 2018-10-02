package org.zayac.vpom;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.asyncsql.PostgreSQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.zayac.vpom.specification.Specifications;

public class MainVerticle extends AbstractVerticle {

  private Specifications specifications;
  private SQLClient jdbc;

  @Override
  public void start(Future<Void> fut) {
    jdbc = PostgreSQLClient.createShared(vertx, config(), "Pom");
    specifications = new Specifications(jdbc);
    startBackend(
      (nothing) -> startWebApp(
        (http) -> completeStartup(http, fut)
      ),
      fut);
  }

  private void startBackend(Handler<AsyncResult<SQLConnection>> next, Future<Void> fut) {
    jdbc.getConnection(ar -> {
      if (ar.failed()) {
        fut.fail(ar.cause());
      } else {
        next.handle(Future.succeededFuture(ar.result()));
      }
    });
  }

  private void startWebApp(Handler<AsyncResult<HttpServer>> next) {
    Router router = Router.router(vertx);

    router.route("/").handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response
        .putHeader("content-type", "text/html")
        .end("<h1>Vert.x POM</h1>");
    });

    router.get("/specifications").handler(specifications::getAll);
    router.route("/specifications*").handler(BodyHandler.create());
    router.post("/specifications").handler(specifications::create);
    router.get("/specifications/:id").handler(specifications::get);
    router.put("/specifications/:id").handler(specifications::update);

    vertx
      .createHttpServer()
      .requestHandler(router::accept)
      .listen(
        config().getInteger("http.port", 8080),
        next::handle
      );
  }

  private void completeStartup(AsyncResult<HttpServer> http, Future<Void> fut) {
    if (http.succeeded()) {
      fut.complete();
    } else {
      fut.fail(http.cause());
    }
  }

  @Override
  public void stop() throws Exception {
    // Close the JDBC client.
    jdbc.close();
  }

}
