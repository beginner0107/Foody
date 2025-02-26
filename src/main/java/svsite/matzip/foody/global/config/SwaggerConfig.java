package svsite.matzip.foody.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.Arrays;
import java.util.List;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import svsite.matzip.foody.global.auth.AuthenticatedUser;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    Info info = new Info()
        .title("Foody API Document")
        .version("v0.0.1")
        .description("Foody 프로젝트 API 명세서입니다.");

    Server server = new Server();
    server.setUrl("https://map-sv.site");

    return new OpenAPI()
        .components(new Components()
            .addSecuritySchemes("bearerAuth", new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")))
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
        .info(info)
        .servers(List.of(server));
  }

  @Bean
  public OperationCustomizer operationCustomizer() {
    return (operation, handlerMethod) -> {
      Arrays.stream(handlerMethod.getMethodParameters())
          .filter(param -> param.hasParameterAnnotation(AuthenticatedUser.class))
          .forEach(param -> {
            if (operation.getParameters() != null) {
              operation.getParameters().clear();
            }
          });
      return operation;
    };
  }
}
