package FeedStudy.StudyFeed.arch;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import org.springframework.beans.factory.annotation.Autowired;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameEndingWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

@AnalyzeClasses(
        packages = "FeedStudy.StudyFeed",
        importOptions = {
                ImportOption.DoNotIncludeTests.class,
                ImportOption.DoNotIncludeJars.class
        }
)
public class TransactionAndDITest {

    // 1) @Transactional 은 서비스 레이어 메서드에서만
    @ArchTest
    static final com.tngtech.archunit.lang.ArchRule transactional_only_on_service =
            methods()
                    .that().areAnnotatedWith(org.springframework.transaction.annotation.Transactional.class)
                    .or().areAnnotatedWith(jakarta.transaction.Transactional.class)
                    .should().beDeclaredInClassesThat().resideInAnyPackage("..service..");

    // 2) 컨트롤러/레포지토리에 @Transactional 금지(메서드 기준)
    @ArchTest
    static final com.tngtech.archunit.lang.ArchRule no_transactional_on_controller_or_repository =
            noMethods()
                    .that().areDeclaredInClassesThat().resideInAnyPackage("..controller..", "..repository..", "..config..")
                    .should().beAnnotatedWith(org.springframework.transaction.annotation.Transactional.class)
                    .orShould().beAnnotatedWith(jakarta.transaction.Transactional.class);

    // 3) 필드 주입 금지 (Application만 예외)
    @ArchTest
    static final com.tngtech.archunit.lang.ArchRule no_field_injection_in_prod_layers =
            noFields()
                    .that().areDeclaredInClassesThat(
                            resideInAnyPackage("..controller..", "..service..", "..repository..")
                                    .and(com.tngtech.archunit.base.DescribedPredicate.not(simpleNameEndingWith("Application")))
                    )
                    .should().beAnnotatedWith(Autowired.class);
}