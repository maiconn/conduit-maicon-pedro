package io.github.raeperd.realworld.application.article;

import io.github.raeperd.realworld.application.user.ProfileModel.ProfileModelNested;
import io.github.raeperd.realworld.domain.article.Article;
import io.github.raeperd.realworld.domain.article.tag.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class ArticleModelTest {

    @Test
    void shouldConvertArticleToArticleModel_withAllFields() {
        // Arrange
        Article article = mock(Article.class);
        var contents = mock(io.github.raeperd.realworld.domain.article.ArticleContents.class);
        var title = mock(io.github.raeperd.realworld.domain.article.ArticleTitle.class);
        var author = mock(io.github.raeperd.realworld.domain.user.User.class);
        var profile = mock(io.github.raeperd.realworld.domain.user.Profile.class);

        Tag tag1 = mock(Tag.class);
        Tag tag2 = mock(Tag.class);

        Instant createdAt = Instant.parse("2023-01-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2023-01-02T00:00:00Z");

        when(article.getContents()).thenReturn(contents);
        when(contents.getTitle()).thenReturn(title);

        when(title.getSlug()).thenReturn("slug-test");
        when(title.getTitle()).thenReturn("title-test");

        when(contents.getDescription()).thenReturn("description-test");
        when(contents.getBody()).thenReturn("body-test");

        when(contents.getTags()).thenReturn(Set.of(tag1, tag2));

        when(tag1.toString()).thenReturn("tag1");
        when(tag2.toString()).thenReturn("tag2");

        when(article.getCreatedAt()).thenReturn(createdAt);
        when(article.getUpdatedAt()).thenReturn(updatedAt);

        when(article.isFavorited()).thenReturn(true);
        when(article.getFavoritedCount()).thenReturn(5);

        when(article.getAuthor()).thenReturn(author);
        when(author.getProfile()).thenReturn(profile);


        ProfileModelNested profileModelNested = mock(ProfileModelNested.class);
        try (var mockedStatic = mockStatic(ProfileModelNested.class)) {
            mockedStatic.when(() -> ProfileModelNested.fromProfile(profile))
                    .thenReturn(profileModelNested);

            // Act
            ArticleModel result = ArticleModel.fromArticle(article);

            // Assert
            assertNotNull(result);
            assertNotNull(result.getArticle());

            var nested = result.getArticle();

            assertEquals("slug-test", nested.getSlug());
            assertEquals("title-test", nested.getTitle());
            assertEquals("description-test", nested.getDescription());
            assertEquals("body-test", nested.getBody());

            assertEquals(Set.of("tag1", "tag2"), nested.getTagList());

            assertEquals(createdAt.atZone(ZoneId.of("Asia/Seoul")), nested.getCreatedAt());
            assertEquals(updatedAt.atZone(ZoneId.of("Asia/Seoul")), nested.getUpdatedAt());

            assertTrue(nested.isFavorited());
            assertEquals(5, nested.getFavoritesCount());

            assertEquals(profileModelNested, nested.getAuthor());
        }
    }
}
