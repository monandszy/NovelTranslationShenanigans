
package code.model.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.beans.ConstructorProperties;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

@javax.annotation.processing.Generated("jsonschema2pojo")
@Value
@EqualsAndHashCode(of = {})
@AllArgsConstructor(onConstructor = @__({
  @JsonCreator,
  @ConstructorProperties({
    "text"
  })
}))
public class Part {

  String text;

}