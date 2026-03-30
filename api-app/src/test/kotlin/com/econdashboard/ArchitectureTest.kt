package com.econdashboard

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("아키텍처 규칙 테스트")
class ArchitectureTest {

    private lateinit var allClasses: JavaClasses

    @BeforeAll
    fun setup() {
        allClasses = ClassFileImporter()
            .withImportOption(ImportOption.DoNotIncludeTests())
            .importPackages("com.econdashboard")
    }

    @Nested
    @DisplayName("레이어 의존성 규칙")
    inner class LayerDependencyRules {

        @Test
        @DisplayName("Controller는 Service만 의존해야 한다 (Repository 직접 접근 금지)")
        fun controllersShouldOnlyDependOnServices() {
            val rule: ArchRule = noClasses()
                .that().resideInAPackage("..controller..")
                .should().dependOnClassesThat().resideInAPackage("..repository..")

            rule.check(allClasses)
        }

        @Test
        @DisplayName("Service는 Controller를 의존하지 않아야 한다")
        fun servicesShouldNotDependOnControllers() {
            val rule: ArchRule = noClasses()
                .that().resideInAPackage("..service..")
                .should().dependOnClassesThat().resideInAPackage("..controller..")

            rule.check(allClasses)
        }

        @Test
        @DisplayName("Repository는 Controller를 의존하지 않아야 한다")
        fun repositoriesShouldNotDependOnControllers() {
            val rule: ArchRule = noClasses()
                .that().resideInAPackage("..repository..")
                .should().dependOnClassesThat().resideInAPackage("..controller..")

            rule.check(allClasses)
        }

        @Test
        @DisplayName("Repository는 Service를 의존하지 않아야 한다")
        fun repositoriesShouldNotDependOnServices() {
            val rule: ArchRule = noClasses()
                .that().resideInAPackage("..repository..")
                .should().dependOnClassesThat().resideInAPackage("..service..")

            rule.check(allClasses)
        }

        @Test
        @DisplayName("Domain 엔티티는 DTO를 의존하지 않아야 한다")
        fun domainShouldNotDependOnDto() {
            val rule: ArchRule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..dto..")

            rule.check(allClasses)
        }

        @Test
        @DisplayName("Domain 엔티티는 Service를 의존하지 않아야 한다")
        fun domainShouldNotDependOnServices() {
            val rule: ArchRule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..service..")

            rule.check(allClasses)
        }

        @Test
        @DisplayName("Domain 엔티티는 Controller를 의존하지 않아야 한다")
        fun domainShouldNotDependOnControllers() {
            val rule: ArchRule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..controller..")

            rule.check(allClasses)
        }
    }

    @Nested
    @DisplayName("네이밍 컨벤션")
    inner class NamingConventionRules {

        @Test
        @DisplayName("Controller 클래스는 Controller로 끝나야 한다")
        fun controllersShouldBeNamedCorrectly() {
            val rule: ArchRule = classes()
                .that().resideInAPackage("..controller..")
                .and().areAnnotatedWith(org.springframework.web.bind.annotation.RestController::class.java)
                .should().haveSimpleNameEndingWith("Controller")

            rule.check(allClasses)
        }

        @Test
        @DisplayName("Service 클래스는 Service 또는 Listener로 끝나야 한다")
        fun servicesShouldBeNamedCorrectly() {
            val rule: ArchRule = classes()
                .that().resideInAPackage("..service..")
                .and().areAnnotatedWith(org.springframework.stereotype.Service::class.java)
                .or().areAnnotatedWith(org.springframework.stereotype.Component::class.java)
                .should().haveSimpleNameEndingWith("Service")
                .orShould().haveSimpleNameEndingWith("Listener")

            rule.check(allClasses)
        }

        @Test
        @DisplayName("Repository 인터페이스는 Repository로 끝나야 한다")
        fun repositoriesShouldBeNamedCorrectly() {
            val rule: ArchRule = classes()
                .that().resideInAPackage("..repository..")
                .should().haveSimpleNameEndingWith("Repository")

            rule.check(allClasses)
        }
    }

    @Nested
    @DisplayName("어노테이션 규칙")
    inner class AnnotationRules {

        @Test
        @DisplayName("Controller는 @RestController 어노테이션이 있어야 한다")
        fun controllersShouldBeAnnotated() {
            val rule: ArchRule = classes()
                .that().resideInAPackage("..controller..")
                .and().haveSimpleNameEndingWith("Controller")
                .should().beAnnotatedWith(org.springframework.web.bind.annotation.RestController::class.java)

            rule.check(allClasses)
        }
    }
}
