package eniso.common.hik;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

import lombok.Data;

/**
 * 扁平请求传递对象
 *
 * @author Eniso
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CasFlatRequest {

    @JsonProperty(value = "method", required = true)
    private String method;

    @JsonProperty(value = "url", required = true)
    private String url;

    @JsonProperty("values")
    private Map<String, String> values;

}
