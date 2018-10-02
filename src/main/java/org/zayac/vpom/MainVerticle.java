package org.zayac.vpom;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.zayac.vpom.specification.SpecificationController;

public class MainVerticle extends AbstractVerticle {

  private SpecificationController specifications;

  @Override
  public void start(Future<Void> startFuture) {
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
    router.get("/specifications/search:name").handler(specifications::getByName);
    router.delete("/specifications/:id").handler(specifications::delete);

    vertx
      .createHttpServer()
      .requestHandler(router::accept)
      .listen(
        config().getInteger("http.port", 8080),
        result -> {
          if (result.succeeded()) {
            startFuture.complete();
          } else {
            startFuture.fail(result.cause());
          }
        }
      );
  }

}
