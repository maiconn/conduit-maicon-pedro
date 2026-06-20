# Relatório de Conexão Frontend (Angular) e Backend (Spring Boot)

Este relatório detalha as configurações necessárias para conectar corretamente o frontend Angular com o backend Spring Boot, com base na análise dos códigos-fonte fornecidos.

## 1. Análise do Frontend (Angular)

O projeto frontend Angular utiliza arquivos de ambiente para gerenciar as URLs da API. Os arquivos relevantes são:

*   `/home/ubuntu/frontend/realworld-app-angular-v20/src/environments/environment.ts`
*   `/home/ubuntu/frontend/realworld-app-angular-v20/src/environments/environment.development.ts`

Ambos os arquivos contêm a seguinte configuração para `apiUrl`:

```typescript
export const environment = {
    production: true,
    apiUrl: 'http://localhost:8080' // TODO: Update for production
};
```

E para o ambiente de desenvolvimento:

```typescript
export const environment = {
    production: false,
    apiUrl: 'http://localhost:8080'
};
```

A `ApiService` (`/home/ubuntu/frontend/realworld-app-angular-v20/src/app/core/services/api-service.ts`) utiliza essa `apiUrl` para construir todas as requisições HTTP para o backend. Por exemplo:

```typescript
// ...
export class ApiService {
  // ...
  private readonly apiUrl = environment.apiUrl;

  get<T>(path: string, params: HttpParams = new HttpParams()): Observable<T> {
    return this.http.get<T>(`${this.apiUrl}${path}`, { params });
  }
  // ...
}
```

Atualmente, o frontend está configurado para buscar o backend na porta `8000`.

## 2. Análise do Backend (Spring Boot)

O projeto backend Spring Boot possui configurações importantes nos seguintes arquivos:

*   `/home/ubuntu/backend/realworld-springboot-java/src/main/resources/application.properties`
*   `/home/ubuntu/backend/realworld-springboot-java/src/main/java/io/github/raeperd/realworld/application/security/SecurityConfiguration.java`

### 2.1. Configuração de Porta

Não foi encontrada uma propriedade `server.port` explícita no arquivo `application.properties`. Isso significa que o Spring Boot utilizará a porta padrão, que é `8080`.

### 2.2. Configuração de CORS (Cross-Origin Resource Sharing)

O arquivo `application.properties` define as origens permitidas para CORS:

```properties
security.allowedOrigins=http://localhost:3000,http://localhost:3001
```

O arquivo `SecurityConfiguration.java` implementa `WebMvcConfigurer` e configura o CORS dinamicamente com base nessas propriedades:

```java
// ...
@Configuration
public class SecurityConfiguration implements WebMvcConfigurer {

    private final SecurityConfigurationProperties properties;

    SecurityConfiguration(SecurityConfigurationProperties properties) {
        this.properties = properties;
    }

    // ...

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods("GET", "HEAD", "POST", "DELETE", "PUT")
                .allowedOrigins(properties.getAllowedOrigins().toArray(new String[0]))
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}

@ConfigurationProperties("security")
class SecurityConfigurationProperties {
    private final List<String> allowedOrigins;

    SecurityConfigurationProperties(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }
}
```

Atualmente, as origens permitidas são `http://localhost:3000` e `http://localhost:3001`. O servidor de desenvolvimento do Angular geralmente roda na porta `4200` (`http://localhost:4200`).

## 3. Solução para Conexão Correta

Para que o frontend Angular e o backend Spring Boot se comuniquem corretamente, são necessárias as seguintes alterações:

### 3.1. Ajustar a `apiUrl` no Frontend

O frontend deve apontar para a porta correta do backend (`8080`). Edite os arquivos `environment.ts` e `environment.development.ts` para refletir a porta `8080`.

**Arquivo:** `/home/ubuntu/frontend/realworld-app-angular-v20/src/environments/environment.ts`

```typescript
export const environment = {
    production: true,
    apiUrl: 'http://localhost:8080/' // Atualizado para a porta do backend
};
```

**Arquivo:** `/home/ubuntu/frontend/realworld-app-angular-v20/src/environments/environment.development.ts`

```typescript
export const environment = {
    production: false,
    apiUrl: 'http://localhost:8080/' // Atualizado para a porta do backend
};
```

### 3.2. Adicionar a Origem do Frontend nas Configurações de CORS do Backend

O backend precisa permitir requisições vindas do servidor de desenvolvimento do Angular (geralmente `http://localhost:4200`). Adicione esta origem à propriedade `security.allowedOrigins` no arquivo `application.properties`.

**Arquivo:** `/home/ubuntu/backend/realworld-springboot-java/src/main/resources/application.properties`

```properties
spring.jpa.open-in-view=false
spring.jpa.hibernate.ddl-auto=none
spring.datasource.url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE

logging.level.sql=DEBUG

security.allowedOrigins=http://localhost:3000,http://localhost:3001,http://localhost:4200
```

## 4. Passos para Execução

1.  **Modifique os arquivos de ambiente do Angular** conforme indicado na seção 3.1.
2.  **Modifique o arquivo `application.properties` do Spring Boot** conforme indicado na seção 3.2.
3.  **Compile e execute o backend Spring Boot.** Certifique-se de que ele esteja rodando na porta `8080`.
4.  **Compile e execute o frontend Angular.** Ele deve rodar na porta `4200` por padrão (ou outra porta configurada no `angular.json` ou via CLI).

Após essas alterações, o frontend Angular deverá conseguir se comunicar com o backend Spring Boot sem problemas de CORS ou de URL da API.
