package FeedStudy.StudyFeed.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {

        final String securitySchemeName = "Bearer Authentication";

        return new OpenAPI()
                .info(new Info()
                        .title("고리 api 명세서")
                        .description("고리(Gory) 서비스의 백엔드 API 문서입니다.\n\n"
                                     + "❗ 먼저 로그인 후, 응답받은 accessToken을 상단 'Authorize' 버튼에 등록하세요.\n\n"
                                     + "`Bearer {accessToken}` 형식으로 입력해야 합니다.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("성상화")
                                .email("sangwha0@gmail.com")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT") // 표시용, 필수 아님
                        ));

    }
}
