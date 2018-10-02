package org.zayac.vpom.specification;


import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

public class SpecificationController {
  public void getByName(RoutingContext context) {
    final String name = context.queryParam("name").get(0);
    context.response()
      .putHeader("content-type", "application/json; charset=utf-8")
      .end(
        Json.encodePrettily(
          SpecificationDao.getByName(name)
        )
      );
  }

  public void delete(RoutingContext context) {
    final Long id = Long.valueOf(context.pathParam("id"));
    if (id == null) {
      context.response().setStatusCode(400).end();
    } else {
      SpecificationDao.delete(id);
    }
    context.response().setStatusCode(204).end();
  }

  public void update(RoutingContext context) {
    final Long id = Long.valueOf(context.pathParam("id"));
    context.response()
      .putHeader("content-type", "application/json; charset=utf-8")
      .end(
        Json.encodePrettily(
          SpecificationDao.update(
            id,
            Json.decodeValue(context.getBodyAsString(), Specification.class)
          )
        )
      );
  }

  public void get(RoutingContext context) {
    final Long id = Long.valueOf(context.pathParam("id"));
    context.response()
      .putHeader("content-type", "application/json; charset=utf-8")
      .end(
        Json.encodePrettily(
          SpecificationDao.getById(id)
        )
      );
  }

  public void create(RoutingContext context) {
    context.response()
      .setStatusCode(201)
      .putHeader("content-type", "application/json; charset=utf-8")
      .end(
        Json.encodePrettily(
          SpecificationDao.create(
            Json.decodeValue(context.getBodyAsString(), Specification.class)
          )
        )
      );
  }


  public void getAll(RoutingContext context) {
    context.response()
      .putHeader("content-type", "application/json; charset=utf-8")
      .end(
        Json.encodePrettily(
          SpecificationDao.getAll()
        )
      );
  }

}
