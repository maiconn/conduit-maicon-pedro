package io.github.raeperd.realworld.domain.user;

import io.github.raeperd.realworld.domain.article.ArticleTitle;
import io.github.raeperd.realworld.domain.article.ArticleContents;
import io.github.raeperd.realworld.domain.article.ArticleUpdateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static io.github.raeperd.realworld.domain.user.UserTestUtils.userWithEmailAndName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Email emailMock;
    @Mock
    private UserName userNameMock;
    @Mock
    private Password passwordMock;

    @Test
    void when_create_user_getImage_return_null() {
        final var user = User.of(emailMock, userNameMock, passwordMock);

        assertThat(user.getImage()).isNull();
    }

    @Test
    void when_create_user_getBio_return_null() {
        final var user = User.of(emailMock, userNameMock, passwordMock);

        assertThat(user.getBio()).isNull();
    }

    @Test
    void when_user_have_different_email_expect_not_equal_and_hashCode(
            @Mock Email otherEmail, @Mock UserName otherName, @Mock Password otherPassword) {
        final var user = User.of(emailMock, userNameMock, passwordMock);
        final var userWithSameEmail = User.of(otherEmail, otherName, otherPassword);

        assertThat(userWithSameEmail)
                .isNotEqualTo(user)
                .extracting(User::hashCode)
                .isNotEqualTo(user.hashCode());
    }

    @Test
    void when_user_have_same_email_expect_equal_and_hashCode(@Mock UserName otherName, @Mock Password otherPassword) {
        final var user = User.of(emailMock, userNameMock, passwordMock);
        final var userWithSameEmail = User.of(emailMock, otherName, otherPassword);

        assertThat(userWithSameEmail)
                .isEqualTo(user)
                .hasSameHashCodeAs(user);
    }

    @Test
    void when_view_profile_not_following_user_expect_following_false(@Mock Email otherEmail) {
        final var user = User.of(emailMock, userNameMock, passwordMock);
        final var otherUser = User.of(otherEmail, userNameMock, passwordMock);

        assertThat(user.viewProfile(otherUser))
                .hasFieldOrPropertyWithValue("following", false);
    }

    @Test
    void when_matches_password_expect_password_matches_password() {
        final var user = User.of(emailMock, userNameMock, passwordMock);

        user.matchesPassword("some-password", passwordEncoder);

        verify(passwordMock, times(1)).matchesPassword("some-password", passwordEncoder);
    }

    @Test
    void when_changeEmail_expect_getEmail_return_new_email(@Mock Email emailToChange) {
        final var user = User.of(emailMock, userNameMock, passwordMock);

        user.changeEmail(emailToChange);

        assertThat(user.getEmail()).isEqualTo(emailToChange);
    }

    @Test
    void when_changePassword_expect_matchesPassword_matches_new_password(@Mock Password passwordToChange) {
        final var user = User.of(emailMock, userNameMock, passwordMock);

        user.changePassword(passwordToChange);

        user.matchesPassword("some-password", passwordEncoder);
        verify(passwordToChange, times(1)).matchesPassword("some-password", passwordEncoder);
    }

    @Test
    void when_changeName_expect_getName_return_new_name(@Mock UserName userNameToChange) {
        final var user = User.of(emailMock, userNameMock, passwordMock);

        user.changeName(userNameToChange);

        assertThat(user.getName()).isEqualTo(userNameToChange);
    }

    @Test
    void when_changeBio_expect_getBio_return_new_bio() {
        final var user = User.of(emailMock, userNameMock, passwordMock);

        user.changeBio("new bio");

        assertThat(user.getBio()).isEqualTo("new bio");
    }

    @Test
    void when_changeImage_expect_getImage_return_new_image(@Mock Image imageToChange) {
        final var user = User.of(emailMock, userNameMock, passwordMock);

        user.changeImage(imageToChange);

        assertThat(user.getImage()).isEqualTo(imageToChange);
    }

    @Test
    void when_writeArticle_expect_article_author_and_contents_match() {
        final var user = userWithEmailAndName("a@b.com", "author");
        final var contents = new ArticleContents(
                "desc",
                ArticleTitle.of("title"),
                "body",
                Set.of()
        );

        final var article = user.writeArticle(contents);

        assertThat(article.getAuthor()).isEqualTo(user);
        assertThat(article.getContents().getBody()).isEqualTo("body");
        assertThat(article.getContents().getTitle().getTitle()).isEqualTo("title");
    }

    @Test
    void when_updateArticle_and_is_author_expect_contents_updated() {
        final var user = userWithEmailAndName("u1@e.com", "u1");
        final var contents = new ArticleContents(
                "desc",
                ArticleTitle.of("old-title"),
                "old-body",
                Set.of()
        );
        final var article = user.writeArticle(contents);

        final var updateRequest = ArticleUpdateRequest.builder()
                .titleToUpdate(ArticleTitle.of("new-title"))
                .descriptionToUpdate("new-desc")
                .bodyToUpdate("new-body")
                .build();

        final var updated = user.updateArticle(article, updateRequest);

        assertThat(updated.getContents().getTitle().getTitle()).isEqualTo("new-title");
        assertThat(updated.getContents().getDescription()).isEqualTo("new-desc");
        assertThat(updated.getContents().getBody()).isEqualTo("new-body");
    }

    @Test
    void when_updateArticle_and_not_author_expect_throw() {
        final var author = userWithEmailAndName("author@e.com", "author");
        final var other = userWithEmailAndName("other@e.com", "other");
        final var contents = new ArticleContents(
                "desc",
                ArticleTitle.of("t"),
                "b",
                Set.of()
        );
        final var article = author.writeArticle(contents);

        final var updateRequest = ArticleUpdateRequest.builder()
                .bodyToUpdate("new-body")
                .build();

        org.junit.jupiter.api.Assertions.assertThrows(IllegalAccessError.class, () -> other.updateArticle(article, updateRequest));
    }

    @Test
    void when_comment_and_delete_expect_removed_only_when_author_and_comment_author() {
        final var user = userWithEmailAndName("u@e.com", "u");
        final var contents = new ArticleContents(
                "desc",
                ArticleTitle.of("t"),
                "b",
                Set.of()
        );
        final var article = user.writeArticle(contents);

        final var comment = user.writeCommentToArticle(article, "hello");
        ReflectionTestUtils.setField(comment, "id", 1L);

        user.deleteArticleComment(article, 1L);

        assertThat(article.getComments()).doesNotContain(comment);
    }

    @Test
    void when_delete_non_existing_comment_expect_NoSuchElementException() {
        final var user = userWithEmailAndName("u2@e.com", "u2");
        final var contents = new ArticleContents(
                "desc",
                ArticleTitle.of("t"),
                "b",
                Set.of()
        );
        final var article = user.writeArticle(contents);

        org.junit.jupiter.api.Assertions.assertThrows(java.util.NoSuchElementException.class, () -> user.deleteArticleComment(article, 999L));
    }

    @Test
    void when_favorite_and_unfavorite_expect_favorited_state_and_count_change() {
        final var user = userWithEmailAndName("fav@e.com", "fav");
        final var other = userWithEmailAndName("author@e.com", "author");
        final var contents = new ArticleContents(
                "desc",
                ArticleTitle.of("t"),
                "b",
                Set.of()
        );
        final var article = other.writeArticle(contents);

        final var afterFav = user.favoriteArticle(article);

        assertThat(afterFav.isFavorited()).isTrue();
        assertThat(afterFav.getFavoritedCount()).isEqualTo(1);

        final var afterUnfav = user.unfavoriteArticle(article);

        assertThat(afterUnfav.isFavorited()).isFalse();
        assertThat(afterUnfav.getFavoritedCount()).isZero();
    }

    @Test
    void when_follow_and_unfollow_expect_viewProfile_reflects_following() {
        final var follower = userWithEmailAndName("f@e.com", "f");
        final var followee = userWithEmailAndName("ee@e.com", "ee");

        follower.followUser(followee);

        assertThat(follower.viewProfile(followee)).hasFieldOrPropertyWithValue("following", true);

        follower.unfollowUser(followee);

        assertThat(follower.viewProfile(followee)).hasFieldOrPropertyWithValue("following", false);
    }
}