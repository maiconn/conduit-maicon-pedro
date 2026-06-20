package io.github.raeperd.realworld.application.article;


import io.github.raeperd.realworld.domain.article.ArticleContents;
import io.github.raeperd.realworld.domain.article.tag.Tag;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class ArticlePostRequestDTOTest {

    @Test
    void shouldConvertToArticleContents_withValidData() {
        // Arrange
        String title = "My Title";
        String description = "My Description";
        String body = "My Body";

        Tag tag1 = mock(Tag.class);
        Tag tag2 = mock(Tag.class);

        Set<Tag> tags = Set.of(tag1, tag2);

        ArticlePostRequestDTO dto =
                new ArticlePostRequestDTO(title, description, body, tags);

        // Act
        ArticleContents result = dto.toArticleContents();

        // Assert
        assertNotNull(result);

        // valida description
        assertEquals(description, result.getDescription());

        // valida title (usa factory ArticleTitle.of)
        assertEquals(title, result.getTitle().getTitle());

        // valida body
        assertEquals(body, result.getBody());

        // valida tags
        assertEquals(tags, result.getTags());
    }

    @Test
    void shouldKeepOriginalValuesInDto() {
        // Arrange
        String title = "Another Title";
        String description = "Another Description";
        String body = "Another Body";

        Set<Tag> tags = Set.of(mock(Tag.class));

        ArticlePostRequestDTO dto =
                new ArticlePostRequestDTO(title, description, body, tags);

        // Assert (cobrindo getters gerados pelo Lombok)
        assertEquals(title, dto.getTitle());
        assertEquals(description, dto.getDescription());
        assertEquals(body, dto.getBody());
        assertEquals(tags, dto.getTagList());
    }
}
