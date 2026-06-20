package io.github.raeperd.realworld.domain.article.comment;

import io.github.raeperd.realworld.domain.article.Article;
import io.github.raeperd.realworld.domain.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.stream.Stream;

import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentTest {

    @MethodSource("provideDifferentComments")
    @ParameterizedTest
    void when_compare_different_comment_expect_not_equal(Comment commentLeft, Comment commentRight) {
        assertThat(commentLeft).isNotEqualTo(commentRight)
                .extracting(Comment::hashCode)
                .isNotEqualTo(commentRight.hashCode());
    }

    @Test
    void when_compare_same_comment_expect_equal_and_hashCode(@Mock Article article, @Mock User author) {
        var now = Instant.now();
        var commentLeft = commentWithCreatedAt(article, author, "body", now);
        var commentRight = commentWithCreatedAt(article, author, "body", now);

        assertThat(commentLeft)
                .isEqualTo(commentRight)
                .hasSameHashCodeAs(commentRight);
    }

    private static Stream<Arguments> provideDifferentComments() {
        var articleSample = mock(Article.class);
        var authorSample = mock(User.class);
        var bodySample = "bodySample";
        var createAtSample = now();
        var commentSample = commentWithCreatedAt(articleSample, authorSample, bodySample, createAtSample);
        return Stream.of(
                        Pair.of(commentSample, commentWithCreatedAt(mock(Article.class), authorSample, bodySample, createAtSample)),
                        Pair.of(commentSample, commentWithCreatedAt(articleSample, mock(User.class), bodySample, createAtSample)),
                        Pair.of(commentSample, commentWithCreatedAt(articleSample, authorSample, "different body", createAtSample)),
                        Pair.of(commentSample, commentWithCreatedAt(articleSample, authorSample, bodySample, now().plusSeconds(10))))
                .map(pair -> Arguments.of(pair.getFirst(), pair.getSecond()));
    }

    private static Comment commentWithCreatedAt(Article article, User author, String body, Instant createdAt) {
        var comment = new Comment(article, author, body);
        ReflectionTestUtils.setField(comment, "createdAt", createdAt);
        return comment;
    }

    @Test
    void deveTestarOsGets() {
        Article article = mock(Article.class);
        User user = mock(User.class);
        Instant now = now();
        var comment = commentWithCreatedAt(article, user, "teste", now);

        assertThat(comment.getId()).isEqualTo(null);
        assertThat(comment.getAuthor()).isEqualTo(user);
        assertThat(comment.getBody()).isEqualTo("teste");
        assertThat(comment.getCreatedAt()).isEqualTo(now);
        assertNull(comment.getUpdatedAt());
    }

    @Test
    void deveTestarConstrutor() {
        Comment comment = new Comment() {
        };

        assertNotNull(comment);
    }

    @Test
    void shouldBeEqualWhenSlugIsEqual() {
        Article article = mock(Article.class);
        User user = mock(User.class);
        Instant now = now();
        var comment = commentWithCreatedAt(article, user, "teste", now);
        var comment2 = commentWithCreatedAt(null, null, "teste2", now);

        assertEquals(comment, comment);
        assertEquals(comment.hashCode(), comment.hashCode());

        assertNotEquals(comment, "string");
        assertFalse(comment.equals(null));
        assertFalse(comment.equals(comment2));
        assertEquals(comment, comment);
    }


    @Test
    void shouldReturnTrueWhenAllFieldsAreEqual() {
        // given
        Article article = mock(Article.class);
        User author = mock(User.class);

        // mocks devem se considerar iguais a si mesmos
        when(article.equals(article)).thenReturn(true);
        when(author.equals(author)).thenReturn(true);

        Comment c1 = new Comment(article, author, "body");
        Comment c2 = new Comment(article, author, "body");

        Instant now = Instant.now();

        ReflectionTestUtils.setField(c1, "createdAt", now);
        ReflectionTestUtils.setField(c2, "createdAt", now);

        // when + then
        assertEquals(c1, c2);
    }

    @Test
    void shouldReturnFalseWhenArticleDiffers() {
        Article article1 = mock(Article.class);
        Article article2 = mock(Article.class);

        User author = mock(User.class);

        when(article1.equals(article2)).thenReturn(false);
        when(author.equals(author)).thenReturn(true);

        Comment c1 = new Comment(article1, author, "body");
        Comment c2 = new Comment(article2, author, "body");

        Instant now = Instant.now();
        ReflectionTestUtils.setField(c1, "createdAt", now);
        ReflectionTestUtils.setField(c2, "createdAt", now);

        assertNotEquals(c1, c2);
    }

    @Test
    void shouldReturnFalseWhenAuthorDiffers() {
        Article article = mock(Article.class);
        User author1 = mock(User.class);
        User author2 = mock(User.class);

        when(article.equals(article)).thenReturn(true);
        when(author1.equals(author2)).thenReturn(false);

        Comment c1 = new Comment(article, author1, "body");
        Comment c2 = new Comment(article, author2, "body");

        Instant now = Instant.now();
        ReflectionTestUtils.setField(c1, "createdAt", now);
        ReflectionTestUtils.setField(c2, "createdAt", now);

        assertNotEquals(c1, c2);
    }

    @Test
    void shouldReturnFalseWhenCreatedAtDiffers() {
        Article article = mock(Article.class);
        User author = mock(User.class);

        when(article.equals(article)).thenReturn(true);
        when(author.equals(author)).thenReturn(true);

        Comment c1 = new Comment(article, author, "body");
        Comment c2 = new Comment(article, author, "body");

        ReflectionTestUtils.setField(c1, "createdAt", Instant.now());
        ReflectionTestUtils.setField(c2, "createdAt", Instant.now().plusSeconds(60));

        assertNotEquals(c1, c2);
    }

    @Test
    void shouldReturnFalseWhenBodyDiffers() {
        Article article = mock(Article.class);
        User author = mock(User.class);

        when(article.equals(article)).thenReturn(true);
        when(author.equals(author)).thenReturn(true);

        Comment c1 = new Comment(article, author, "body1");
        Comment c2 = new Comment(article, author, "body2");

        Instant now = Instant.now();
        ReflectionTestUtils.setField(c1, "createdAt", now);
        ReflectionTestUtils.setField(c2, "createdAt", now);

        assertNotEquals(c1, c2);
    }


}