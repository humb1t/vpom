package org.zayac.vpom.specification;

import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Specification {
  private Long id;
  private String name;


  public Specification(JsonObject jsonObject) {
    this.id = jsonObject.getLong("id");
    this.name = jsonObject.getString("name");
  }

}
