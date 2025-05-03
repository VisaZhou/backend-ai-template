package org.visage.backend.pojo.res;

import io.swagger.v3.oas.annotations.media.Schema;import lombok.AllArgsConstructor;import lombok.Builder;import lombok.Data;import lombok.NoArgsConstructor;import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@Schema(name = "ActorFilmAndYearRes", description = "电影名称和发行年份")
public class ActorFilmAndYearRes {

    @Schema(description = "电影名称")
    String filmName;

    @Schema(description = "发行年份")
    String year;
}
