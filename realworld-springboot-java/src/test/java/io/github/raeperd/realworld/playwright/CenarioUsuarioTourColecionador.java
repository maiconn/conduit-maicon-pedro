package io.github.raeperd.realworld.playwright;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class CenarioUsuarioTourColecionador {

    public static void main(String[] args) {

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(500) // deixa mais visual
            );

            Page page = browser.newPage();

            // Abre sistema
            page.navigate("http://localhost:4200");

            // Vai para tela de cadastro
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Sign up")).click();

            // Valida URL
            assertThat(page).hasURL("http://localhost:4200/register");

            // Localizadores
            Locator username = page.locator("#username");
            Locator email = page.locator("#email");
            Locator password = page.locator("#password");
            Locator passwordConfirmation = page.locator("#passwordConfirmation");

            Locator signUpButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign up"));

            // =========================================================
            // Cenário:
            // Verificar botão desabilitado inicialmente
            // =========================================================

            assertThat(signUpButton).isDisabled();

            System.out.println("Botão Sign up iniciou desabilitado.");

            // =========================================================
            // Digita "teste" em todos os campos
            // =========================================================

            username.fill("teste2@teste.com");
            email.fill("teste2@teste.com");
            password.fill("teste");
            passwordConfirmation.fill("teste");

            signUpButton.click();

            // Espera navegação ou comportamento esperado
            page.waitForTimeout(3000);

            System.out.println("Usuário cadastrado com sucesso.");


// criar um artigo novo
            page.navigate("http://localhost:4200/editor");

// ==========================
// Preencher formulário
// ==========================

// Localizadores (usando placeholder pois não há id)
            Locator title = page.getByPlaceholder("Article Title");
            Locator about = page.getByPlaceholder("What's this article about?");
            Locator body = page.getByPlaceholder("Write your article (in markdown)");
            Locator tags = page.getByPlaceholder("Enter tags (separated by spaces)");

            Locator publishButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Publish Article"));

// Preenche dados
            title.fill("teste");
            about.fill("Testando o teste");
            body.fill("# Teste");
            tags.fill("teste teste2 teste3");

// Publica artigo
            publishButton.click();

// ==========================
// Validar criação do artigo
// ==========================

// Espera navegação
            page.waitForURL("**/article/**");

// Valida título na página
            Locator articleTitle = page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("teste"));

            assertThat(articleTitle).isVisible();

            System.out.println("✅ Artigo criado com sucesso");


// ==========================
// Voltar para Home
// ==========================

            page.getByRole(AriaRole.LINK,
                            new Page.GetByRoleOptions().setName("Home"))
                    .click();

// Espera carregar
            page.waitForLoadState(LoadState.NETWORKIDLE);

// ==========================
// Clicar no Global Feed (TAB = BUTTON)
// ==========================

            Locator globalFeedTab = page.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Global Feed"));

            globalFeedTab.click();

// ==========================
// Validar artigo no feed
// ==========================

// Espera possível carregamento
            page.waitForLoadState(LoadState.NETWORKIDLE);

// Verifica se artigo aparece
            Locator articlePreview = page.getByText("teste");

// Melhor validação: primeiro visível
            assertThat(articlePreview.first()).isVisible();

            System.out.println("✅ Artigo encontrado no Global Feed");
// Screenshot final
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(java.nio.file.Paths.get("artigo-cadastrado-sucesso.png")));

            browser.close();
        }
    }
}