package com.reactivespring.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class MovieInfo {

    @Id
    private String movieInfoId;
    @NotBlank(message = "movieInfo.name cannot be empty")
    private String name;
    @NotNull(message = "movieInfo.year cannot be null or negative")
    @Positive(message = "movieInfo.year must be a Positive value")
    private Integer year;
    private List<@NotBlank(message = "movieInfo.cast cannot be blank") String> cast;
    private LocalDate release_date;
}
