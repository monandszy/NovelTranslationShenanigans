package code.model.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.beans.ConstructorProperties;
import javax.annotation.Generated;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Generated("jsonschema2pojo")
@Value
@EqualsAndHashCode(of = {})
@AllArgsConstructor(onConstructor = @__({
  @JsonCreator,
  @ConstructorProperties({
    "promptTokenCount",
    "candidatesTokenCount",
    "totalTokenCount"
  })
}))
public class UsageMetadata {

  Integer promptTokenCount;
  Integer candidatesTokenCount;
  Integer totalTokenCount;

}