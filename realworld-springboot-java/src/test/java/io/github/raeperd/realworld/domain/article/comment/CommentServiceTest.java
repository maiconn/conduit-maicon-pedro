package io.github.raeperd.realworld.domain.article.comment;

import io.github.raeperd.realworld.domain.article.Article;
import io.github.raeperd.realworld.domain.article.ArticleFindService;
import io.github.raeperd.realworld.domain.user.User;
import io.github.raeperd.realworld.domain.user.UserFindService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Set;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    private CommentService commentService;

    @Mock
    private UserFindService userFindService;
    @Mock
    private ArticleFindService articleFindService;

    @BeforeEach
    private void initializeService() {
        commentService = new CommentService(userFindService, articleFindService);
    }

    @Test
    void when_articleFindService_return_empty_expect_NoSuchElementException() {
        when(articleFindService.getArticleBySlug("slug")).thenReturn(empty());

        assertThatThrownBy(() ->
                commentService.deleteCommentById(1L, "slug", 2L)
        ).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void when_articleFindService_return_empty_expect_NoSuchElementException_without_userId() {
        when(articleFindService.getArticleBySlug("slug")).thenReturn(empty());

        assertThatThrownBy(() -> commentService.getComments("slug")).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void given_articleFindService_return_article_then_return_comments_of_article(@Mock Article article, @Mock Comment comment) {
        given(articleFindService.getArticleBySlug("slug")).willReturn(of(article));
        given(article.getComments()).willReturn(Set.of(comment));

        assertThat(commentService.getComments("slug")).contains(comment);

        then(article).should(times(1)).getComments();
    }

    @Test
    void when_userFindService_return_empty_expect_NoSuchElementException(@Mock Article article) {
        when(articleFindService.getArticleBySlug("slug")).thenReturn(of(article));
        when(userFindService.findById(1L)).thenReturn(empty());

        assertThatThrownBy(() ->
                commentService.deleteCommentById(1L, "slug", 2L)
        ).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void given_user_and_article_deleteCommentById_then_user_deleteArticleComment(@Mock User user, @Mock Article article) {
        given(userFindService.findById(1L)).willReturn(of(user));
        given(articleFindService.getArticleBySlug("slug")).willReturn(of(article));

        commentService.deleteCommentById(1L, "slug", 2L);

        then(user).should(times(1)).deleteArticleComment(article, 2L);
    }

    @Test
    void given_user_and_article_when_createComment_then_return_created_comment(
            @Mock User user,
            @Mock Article article,
            @Mock Comment comment
    ) {
        given(userFindService.findById(1L)).willReturn(of(user));
        given(articleFindService.getArticleBySlug("slug")).willReturn(of(article));
        given(user.writeCommentToArticle(article, "body")).willReturn(comment);

        Comment createdComment = commentService.createComment(1L, "slug", "body");

        assertThat(createdComment).isEqualTo(comment);

        then(user).should(times(1))
                .writeCommentToArticle(article, "body");
    }

    @Test
    void when_createComment_and_user_not_found_then_throw_NoSuchElementException(
            @Mock Article article
    ) {
        given(userFindService.findById(1L)).willReturn(empty());
        given(articleFindService.getArticleBySlug("slug")).willReturn(of(article));

        assertThatThrownBy(() ->
                commentService.createComment(1L, "slug", "body")
        ).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void when_createComment_and_article_not_found_then_throw_NoSuchElementException(
            @Mock User user
    ) {
        given(userFindService.findById(1L)).willReturn(of(user));
        given(articleFindService.getArticleBySlug("slug")).willReturn(empty());

        assertThatThrownBy(() ->
                commentService.createComment(1L, "slug", "body")
        ).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void given_user_and_article_when_getComments_then_return_comments(
            @Mock User user,
            @Mock Article article,
            @Mock Comment comment
    ) {
        given(userFindService.findById(1L)).willReturn(of(user));
        given(articleFindService.getArticleBySlug("slug")).willReturn(of(article));
        given(user.viewArticleComments(article)).willReturn(Set.of(comment));

        Set<Comment> comments = commentService.getComments(1L, "slug");

        assertThat(comments).containsExactly(comment);

        then(user).should(times(1))
                .viewArticleComments(article);
    }

    @Test
    void when_getComments_and_user_not_found_then_throw_NoSuchElementException(
            @Mock Article article
    ) {
        given(userFindService.findById(1L)).willReturn(empty());
        given(articleFindService.getArticleBySlug("slug")).willReturn(of(article));

        assertThatThrownBy(() ->
                commentService.getComments(1L, "slug")
        ).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void when_getComments_and_article_not_found_then_throw_NoSuchElementException(
            @Mock User user
    ) {
        given(userFindService.findById(1L)).willReturn(of(user));
        given(articleFindService.getArticleBySlug("slug")).willReturn(empty());

        assertThatThrownBy(() ->
                commentService.getComments(1L, "slug")
        ).isInstanceOf(NoSuchElementException.class);
    }

}