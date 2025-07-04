package com.cheftory.api.recipe.info.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class RecipeInfo {
    @Id
    @UuidGenerator
    private UUID id;

    @Column(length=512, unique=true)
    private URI url;
    private String title;
    private URI thumbnailUrl;
    private Integer videoSeconds;

    @Enumerated(EnumType.STRING)
    private RecipeStatus status;

    private String description;

    private Integer count;
    private LocalDateTime createdAt;

    public static RecipeInfo preCreationOf(URI url, String title, URI thumbnailUrl, Long videoSeconds){
        return RecipeInfo.builder()
                .url(url)
                .title(title)
                .thumbnailUrl(thumbnailUrl)
                .videoSeconds(videoSeconds.intValue())
                .status(RecipeStatus.PRE_CREATION)
                .count(0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public String getVideoId(){
        return UriComponentsBuilder
                .fromUri(url)
                .build()
                .getQueryParams()
                .get("videoId")
                .toString();
    }

    public void setStatus(RecipeStatus status){
        this.status = status;
    }
}
