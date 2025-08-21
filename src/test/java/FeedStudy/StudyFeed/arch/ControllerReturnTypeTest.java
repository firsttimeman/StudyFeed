package FeedStudy.StudyFeed.arch;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

@AnalyzeClasses(packages = "FeedStudy.StudyFeed")
public class ControllerReturnTypeTest {

    /**
     * 컨트롤러의 public 메서드는 다음 중 하나여야 함:
     *  - ResponseEntity<?>
     *  - ..dto.. 패키지의 DTO 타입
     *  - (옵션) Collection / Page 같은 컨테이너 (내부 제네릭은 여기서 못 봄)
     */
    @ArchTest
    static final ArchRule controllers_should_return_dto_or_responseentity =
            methods()
                    .that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
                    .and().arePublic()
                    // ResponseEntity 허용 (assignableTo를 쓰면 HttpEntity 하위도 허용 가능)
                    .should().haveRawReturnType(assignableTo(ResponseEntity.class))
                    // DTO 패키지 직접 반환 허용 (예: UserResponseDto)
                    .orShould().haveRawReturnType(resideInAPackage("..dto.."))
                    // ===== 필요하면 아래 두 줄 유지 (List<DTO>, Page<DTO> 같은 케이스 허용) =====
                    .orShould().haveRawReturnType(assignableTo(java.util.Collection.class))
                    .orShould().haveRawReturnType(assignableTo(org.springframework.data.domain.Page.class));

    /**
     * 컨트롤러는 엔티티를 절대 직접 반환하면 안 됨.
     */
    @ArchTest
    static final ArchRule controllers_must_not_return_entity =
            methods()
                    .that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
                    .and().arePublic()
                    .should().notHaveRawReturnType(resideInAPackage("..entity.."));
}
