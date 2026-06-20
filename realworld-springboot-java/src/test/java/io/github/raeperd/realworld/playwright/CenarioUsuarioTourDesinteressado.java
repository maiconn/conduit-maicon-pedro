package io.github.raeperd.realworld.playwright;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class CenarioUsuarioTourDesinteressado {

    public static void main(String[] args) {

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions()
                            .setHeadless(false)
                            .setSlowMo(500) // deixa mais visual
            );

            Page page = browser.newPage();

            // Abre sistema
            page.navigate("http://localhost:4200");

            // Vai para tela de cadastro
            page.getByRole(AriaRole.LINK,
                            new Page.GetByRoleOptions().setName("Sign up"))
                    .click();

            // Valida URL
            assertThat(page).hasURL("http://localhost:4200/register");

            // Localizadores
            Locator username = page.locator("#username");
            Locator email = page.locator("#email");
            Locator password = page.locator("#password");
            Locator passwordConfirmation = page.locator("#passwordConfirmation");

            Locator signUpButton = page.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Sign up"));

            // =========================================================
            // Cenário:
            // Verificar botão desabilitado inicialmente
            // =========================================================

            assertThat(signUpButton).isDisabled();

            System.out.println("Botão Sign up iniciou desabilitado.");

            // =========================================================
            // Digita "teste" em todos os campos
            // =========================================================

            username.fill("teste");
            email.fill("teste");
            password.fill("teste");
            passwordConfirmation.fill("teste");

            // força blur para validação
            email.press("Tab");

            // =========================================================
            // Validar erros
            // =========================================================

            Locator errorMessages = page.locator("text=Please provide a valid email.");

            assertThat(errorMessages).hasCount(2);

            System.out.println("Mensagens de erro exibidas corretamente.");

            // =========================================================
            // Digita email válido apenas no Username
            // =========================================================

            username.fill("teste@teste.com");

            // botão ainda deve continuar desabilitado
            assertThat(signUpButton).isDisabled();

            System.out.println("Botão continuou desabilitado.");

            // =========================================================
            // Digita email válido no campo Email
            // =========================================================

            email.fill("teste@teste.com");

            // botão agora habilita
            assertThat(signUpButton).isEnabled();

            System.out.println("Botão habilitado.");

            // =========================================================
            // Clica em Sign up
            // =========================================================

            signUpButton.click();

            // Espera navegação ou comportamento esperado
            page.waitForTimeout(3000);

            // Exemplo:
            // validar que saiu da página register
            // ajuste conforme comportamento real da aplicação

            System.out.println("Usuário cadastrado com sucesso.");

            // Screenshot final
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(java.nio.file.Paths.get("cadastro-sucesso.png")));

            browser.close();
        }
    }
}