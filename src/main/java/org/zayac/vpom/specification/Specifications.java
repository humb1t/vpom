package org.zayac.vpom.specification;


import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.stream.Collectors;

public class Specifications {

  private final SQLClient jdbc;

  public Specifications(SQLClient jdbc) {
    this.jdbc = jdbc;
  }

  public void update(RoutingContext context) {
    final Long id = Long.valueOf(context.pathParam("id"));
    JsonObject json = context.getBodyAsJson();
    if (id == null || json == null) {
      context.response().setStatusCode(400).end();
    } else {
      jdbc.getConnection(ar ->
        updateSql(id, json, ar.result(), (specification) -> {
          if (specification.failed()) {
            context.response().setStatusCode(404).end();
          } else {
            context.response()
              .putHeader("content-type", "application/json; charset=utf-8")
              .end(Json.encodePrettily(specification.result()));
          }
          ar.result().close();
        })
      );
    }
  }

  public void get(RoutingContext context) {
    final Long id = Long.valueOf(context.pathParam("id"));
    jdbc.getConnection(ar -> {
      SQLConnection connection = ar.result();
      select(id, connection, result -> {
          if (result.succeeded()) {
            context.response()
              .setStatusCode(200)
              .putHeader("content-type", "application/json; charset=utf-8")
              .end(Json.encodePrettily(result.result()));
          } else {
            context.response()
              .setStatusCode(404).end();
          }
          connection.close();
        }
      );
    });
  }

  public void create(RoutingContext context) {
    jdbc.getConnection(ar -> {
      final Specification specification = Json.decodeValue(context.getBodyAsString(),
        Specification.class);
      SQLConnection connection = ar.result();
      insert(specification, connection, (r) ->
        context.response()
          .setStatusCode(201)
          .putHeader("content-type", "application/json; charset=utf-8")
          .end(Json.encodePrettily(r.result())));
      connection.close();
    });
  }


  public void getAll(RoutingContext context) {
    jdbc.getConnection(ar -> {
      SQLConnection connection = ar.result();
      connection.query("SELECT * FROM specifications", result -> {
        List<Specification> specifications = result.result().getRows().stream().map(Specification::new).collect(Collectors.toList());
        context.response()
          .putHeader("content-type", "application/json; charset=utf-8")
          .end(Json.encodePrettily(specifications));
        connection.close();
      });
    });
  }

  private void select(Long id, SQLConnection connection, Handler<AsyncResult<Specification>> resultHandler) {
    connection.queryWithParams("SELECT * FROM specifications WHERE id=?", new JsonArray().add(id), ar -> {
      if (ar.failed()) {
        resultHandler.handle(Future.failedFuture("specifications not found"));
      } else {
        if (ar.result().getNumRows() >= 1) {
          resultHandler.handle(Future.succeededFuture(new Specification(ar.result().getRows().get(0))));
        } else {
          resultHandler.handle(Future.failedFuture("specifications not found"));
        }
      }
    });
  }

  private void insert(Specification specification, SQLConnection connection, Handler<AsyncResult<Specification>> next) {
    String sql = "INSERT INTO specifications (name) VALUES ?";
    connection.updateWithParams(sql,
      new JsonArray().add(specification.getName()),
      (ar) -> {
        if (ar.failed()) {
          next.handle(Future.failedFuture(ar.cause()));
          connection.close();
          return;
        }
        UpdateResult result = ar.result();
        Specification resultSpecification = new Specification(
          result.getKeys().getLong(0),
          specification.getName()
        );
        next.handle(Future.succeededFuture(resultSpecification));
      });
  }

  private void updateSql(Long id, JsonObject content, SQLConnection connection,
                         Handler<AsyncResult<Specification>> resultHandler) {
    String sql = "UPDATE specifications SET name=? WHERE id=?";
    connection.updateWithParams(sql,
      new JsonArray().add(content.getString("name")).add(id),
      update -> {
        if (update.failed()) {
          resultHandler.handle(Future.failedFuture("Cannot update the specifications"));
          return;
        }
        if (update.result().getUpdated() == 0) {
          resultHandler.handle(Future.failedFuture("Specifications not found"));
          return;
        }
        resultHandler.handle(
          Future.succeededFuture(
            new Specification(
              Long.valueOf(id),
              content.getString("name")
            )
          )
        );
      });
  }

}
