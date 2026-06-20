package io.github.raeperd.realworld.domain.article;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import io.github.raeperd.realworld.domain.article.tag.Tag;
import io.github.raeperd.realworld.domain.article.tag.TagService;
import io.github.raeperd.realworld.domain.user.User;
import io.github.raeperd.realworld.domain.user.UserFindService;
import java.util.NoSuchElementException;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    private ArticleService articleService;

    @Mock
    private UserFindService userFindService;
    @Mock
    private TagService tagService;
    @Mock
    private ArticleRepository repository;

    @Spy
    private User author;

    @BeforeEach
    void initializeService() {
        articleService = new ArticleService(userFindService, tagService, repository);
    }

    @Test
    void when_author_not_found_expect_NoSuchElementException(@Mock ArticleContents contents) {
        when(userFindService.findById(anyLong())).thenReturn(empty());

        assertThatThrownBy(() ->
                articleService.createNewArticle(1L, contents)
        ).isInstanceOf(NoSuchElementException.class);
    }

    @ParameterizedTest(name = "createNewArticle: tags present={0}")
    @ValueSource(booleans = {true, false})
    void given_author_createNewArticle_then_tagService_reloadTags(boolean tagsPresent, @Mock ArticleContents contents) {
        final Set<Tag> tags = tagsPresent ? Set.of(mock(Tag.class)) : Set.of();
        given(contents.getTags()).willReturn(tags);
        given(userFindService.findById(1L)).willReturn(of(author));
        given(repository.save(any(Article.class))).willReturn(mock(Article.class));

        articleService.createNewArticle(1L, contents);

        then(tagService).should(times(1)).reloadAllTagsIfAlreadyPresent(tags);
        then(author).should(times(1)).writeArticle(contents);
    }

    @Test
    void given_author_createNewArticle_then_author_writeArticle_contents(@Mock ArticleContents contents) {
        given(userFindService.findById(1L)).willReturn(of(author));
        given(repository.save(any(Article.class))).willReturn(mock(Article.class));

        articleService.createNewArticle(1L, contents);

        then(author).should(times(1)).writeArticle(contents);
    }

    @ParameterizedTest(name = "getArticles: empty={0}")
    @ValueSource(booleans = {true, false})
    void when_getArticles_expect_repository_findAll(boolean emptyPage) {
        Pageable pageable = mock(Pageable.class);
        Page<Article> page = (Page<Article>) mock(Page.class);
        Page<Article> returned = emptyPage ? Page.empty() : page;

        given(repository.findAll(pageable)).willReturn(returned);

        articleService.getArticles(pageable);

        then(repository).should(times(1)).findAll(pageable);
        // result should be the same page instance or empty
        // no further interactions required
    }

    @Test
    void given_author_writeArticle_then_userRepository_save(@Mock ArticleContents contents, @Mock Article article) {
        given(userFindService.findById(1L)).willReturn(of(author));
        given(author.writeArticle(contents)).willReturn(article);
        given(repository.save(article)).willReturn(article);

        articleService.createNewArticle(1L, contents);

        then(repository).should(times(1)).save(article);
    }

    @ParameterizedTest(name = "getFeedByUserId: userFound={0}")
    @ValueSource(booleans = {true, false})
    void when_getFeedByUserId_expect_behavior_based_on_user_presence(boolean userFound) {
        Pageable pageable = mock(Pageable.class);
        var user = mock(User.class);
        Page<Article> page = (Page<Article>) mock(Page.class);
        Page<Article> mapped = (Page<Article>) mock(Page.class);

        given(userFindService.findById(1L)).willReturn(userFound ? of(user) : empty());

        if (userFound) {
            given(repository.findAllByUserFavoritedContains(user, pageable)).willReturn(page);
            doReturn(mapped).when(page).map(any());

            articleService.getFeedByUserId(1L, pageable);

            then(repository).should(times(1)).findAllByUserFavoritedContains(user, pageable);
        } else {
            assertThatThrownBy(() -> articleService.getFeedByUserId(1L, pageable))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @ParameterizedTest(name = "getArticleFavoritedByUsername: userFound={0}")
    @ValueSource(booleans = {true, false})
    void when_getArticleFavoritedByUsername_expect_empty_or_mapped(boolean userFound, @Mock Pageable pageable) {
        var username = mock(io.github.raeperd.realworld.domain.user.UserName.class);
        var user = mock(User.class);
        Page<Article> page = (Page<Article>) mock(Page.class);
        Page<Article> mapped = (Page<Article>) mock(Page.class);

        given(userFindService.findByUsername(username)).willReturn(userFound ? of(user) : empty());

        if (userFound) {
            given(repository.findAllByUserFavoritedContains(user, pageable)).willReturn(page);
            doReturn(mapped).when(page).map(any());

            articleService.getArticleFavoritedByUsername(username, pageable);

            then(repository).should(times(1)).findAllByUserFavoritedContains(user, pageable);
        } else {
            articleService.getArticleFavoritedByUsername(username, pageable);
            // should be empty page
        }
    }

    @Test
    void when_getArticlesByAuthorName_expect_repository_called(@Mock Pageable pageable) {

        given(repository.findAllByAuthorProfileUserName(any(), eq(pageable)))
                .willReturn((Page<Article>) mock(Page.class));

        articleService.getArticlesByAuthorName("author-name", pageable);

        then(repository).should(times(1)).findAllByAuthorProfileUserName(any(), eq(pageable));
    }

    @ParameterizedTest(name = "getArticlesByTag: tagExists={0}")
    @ValueSource(booleans = {true, false})
    void when_getArticlesByTag_expect_behavior_based_on_tag_presence(boolean tagExists, @Mock Pageable pageable) {
        var tag = mock(Tag.class);
        given(tagService.findByValue("tag"))
                .willReturn(tagExists ? of(tag) : empty());

        if (tagExists) {
            given(repository.findAllByContentsTagsContains(tag, pageable))
                    .willReturn((Page<Article>) mock(Page.class));

            articleService.getArticlesByTag("tag", pageable);

            then(repository).should(times(1)).findAllByContentsTagsContains(tag, pageable);
        } else {
            articleService.getArticlesByTag("tag", pageable);
            // should be empty page
        }
    }

    @ParameterizedTest(name = "getArticleBySlug: exists={0}")
    @ValueSource(booleans = {true, false})
    void when_getArticleBySlug_expect_repository_called(boolean exists) {
        var slug = "slug";
        var article = mock(Article.class);
        given(repository.findFirstByContentsTitleSlug(slug)).willReturn(exists ? of(article) : empty());

        articleService.getArticleBySlug(slug);

        then(repository).should(times(1)).findFirstByContentsTitleSlug(slug);
    }

    @ParameterizedTest(name = "updateArticle: bothPresent={0}")
    @ValueSource(booleans = {true, false})
    void when_updateArticle_expect_success_or_exception(boolean bothPresent) {
        var user = mock(User.class);
        var article = mock(Article.class);
        var request = mock(ArticleUpdateRequest.class);

        given(userFindService.findById(1L)).willReturn(bothPresent ? of(user) : empty());
        given(repository.findFirstByContentsTitleSlug("slug")).willReturn(bothPresent ? of(article) : empty());

        if (bothPresent) {
            given(user.updateArticle(article, request)).willReturn(article);

            articleService.updateArticle(1L, "slug", request);

            then(user).should(times(1)).updateArticle(article, request);
        } else {
            assertThatThrownBy(() -> articleService.updateArticle(1L, "slug", request))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @ParameterizedTest(name = "favorite/unfavorite: bothPresent={0}")
    @ValueSource(booleans = {true, false})
    void when_favoriteArticle_expect_success_or_exception(boolean bothPresent) {
        var user = mock(User.class);
        var article = mock(Article.class);

        given(userFindService.findById(1L)).willReturn(bothPresent ? of(user) : empty());
        given(repository.findFirstByContentsTitleSlug("slug"))
                .willReturn(bothPresent ? of(article) : empty());

        if (bothPresent) {
            given(user.favoriteArticle(article)).willReturn(article);

            articleService.favoriteArticle(1L, "slug");

            then(user).should(times(1)).favoriteArticle(article);
        } else {
            assertThatThrownBy(() -> articleService.favoriteArticle(1L, "slug"))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @ParameterizedTest(name = "favorite/unfavorite: bothPresent={0}")
    @ValueSource(booleans = {true, false})
    void when_unfavoriteArticle_expect_success_or_exception(boolean bothPresent) {
        var user = mock(User.class);
        var article = mock(Article.class);

        given(userFindService.findById(1L)).willReturn(bothPresent ? of(user) : empty());
        given(repository.findFirstByContentsTitleSlug("slug"))
                .willReturn(bothPresent ? of(article) : empty());

        if (bothPresent) {
            given(user.unfavoriteArticle(article)).willReturn(article);

            articleService.unfavoriteArticle(1L, "slug");

            then(user).should(times(1)).unfavoriteArticle(article);
        } else {
            assertThatThrownBy(() -> articleService.unfavoriteArticle(1L, "slug"))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @ParameterizedTest(name = "deleteArticleBySlug: author found={0}")
    @ValueSource(booleans = {true, false})
    void when_delete_article_expect_behavior_based_on_author_presence(boolean authorFound) {
        if (authorFound) {
            given(userFindService.findById(1L)).willReturn(of(author));

            articleService.deleteArticleBySlug(1L, "slug-to-delete");

            then(repository).should(times(1)).deleteArticleByAuthorAndContentsTitleSlug(author, "slug-to-delete");
        } else {
            given(userFindService.findById(1L)).willReturn(empty());

            assertThatThrownBy(() -> articleService.deleteArticleBySlug(1L, "not-exists"))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }
}