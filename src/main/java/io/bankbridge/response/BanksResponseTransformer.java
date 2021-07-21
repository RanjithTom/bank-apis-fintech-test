package io.bankbridge.response;


import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BanksResponseTransformer  {

    private static ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);

  /**
   * Spark ResponseTransformer for converting response model to string.
   * @param model
   * @return
   * @throws JsonProcessingException
   */
    public static String render(Object model) throws JsonProcessingException {
      return mapper.writeValueAsString(model);
    }

}
