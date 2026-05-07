package com.hyejin.portfolio.api;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {
    private final com.tngtech.archunit.core.domain.JavaClasses classes =
        new ClassFileImporter().importPackages("com.hyejin.portfolio");

    @Test
    void domainPackagesDoNotDependOnSpring() {
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
            .check(classes);
    }

    @Test
    void apiControllersDoNotDependOnAdapterOutPackages() {
        noClasses()
            .that().resideInAPackage("..api.adapter.in.web..")
            .should().dependOnClassesThat().resideInAPackage("..adapter.out..")
            .check(classes);
    }

    @Test
    void allocationModuleDoesNotDependOnOtherBusinessModules() {
        noClasses()
            .that().resideInAPackage("..allocation..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..asset..",
                "..evidence..",
                "..simulation..",
                "..recommendation..",
                "..proposal.."
            )
            .check(classes);
    }
}
