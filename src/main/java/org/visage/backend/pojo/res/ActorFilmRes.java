package org.visage.backend.pojo.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.io.Serial;import java.io.Serializable;import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@Schema(name = "ActorFilmRes", description = "电影列表")
public class ActorFilmRes implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "演员姓名")
    private String actor;

    @Schema(description = "电影数量")
    private Integer sum;

    @Schema(description = "电影名称列表")
    private List<String> movies;
}
