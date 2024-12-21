package propra2.splitter;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import java.util.Comparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import propra2.splitter.domain.AusgabenDetails;
import propra2.splitter.domain.TransaktionDetails;
import propra2.splitter.stereotypes.AggregateRoot;
import propra2.splitter.stereotypes.Entity;
import propra2.splitter.stereotypes.Wertobjekt;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static rules.OneAggregateRootExists.ONE_AGGREGATE_ROOT_EXISTS;

@AnalyzeClasses(packagesOf = SplitterApplication.class, importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchTests {

  @ArchTest
  static final ArchRule onion = onionArchitecture()
      .domainModels("propra2.splitter.domain..")
      .domainServices("propra2.splitter.domain..")
      .applicationServices("propra2.splitter.service..")
      .adapter("web", "propra2.splitter.web..")
      .adapter("database", "propra2.splitter.database..");


  @ArchTest
  static final ArchRule rule1 = noFields().should().beAnnotatedWith(Autowired.class).orShould()
      .beAnnotatedWith(Value.class);

  @ArchTest
  static final ArchRule rule2 = classes().that().areAnnotatedWith(Configuration.class).should()
      .resideInAPackage("..config..");

  @ArchTest
  static final ArchRule rule3 = classes().that().areAnnotatedWith(Service.class).should()
      .resideInAPackage("..service..");

  @ArchTest
  static final ArchRule rule4 = classes().that().areAnnotatedWith(Controller.class).should()
      .resideInAPackage("..web..");

  @ArchTest
  static final ArchRule rule5 = classes().that().areAnnotatedWith(Repository.class).should()
      .resideInAPackage("..database..");

  @ArchTest
  static final ArchRule rule6 = noClasses().that().areAnnotatedWith(Controller.class)
      .should().dependOnClassesThat().resideInAPackage("..database..");

  @ArchTest
  static final ArchRule rule7 = slices().matching("..domain..").should(ONE_AGGREGATE_ROOT_EXISTS);

  @ArchTest
  static final ArchRule rule8 = noClasses().should().beAnnotatedWith(Deprecated.class);

  @ArchTest
  static final ArchRule rule9 = classes()
      .that()
      .resideInAPackage("..domain..")
      .and()
      .doNotImplement(Comparator.class)
      .and()
      .doNotBelongToAnyOf(AusgabenDetails.class, TransaktionDetails.class)
      .should()
      .beAnnotatedWith(AggregateRoot.class)
      .orShould()
      .beAnnotatedWith(Entity.class)
      .orShould()
      .beAnnotatedWith(Wertobjekt.class);

  @ArchTest
  static final ArchRule onlyAggregateRootsArePublic = classes()
      .that()
      .areNotAnnotatedWith(AggregateRoot.class)
      .and()
      .doNotBelongToAnyOf(AusgabenDetails.class, TransaktionDetails.class)
      .and()
      .resideInAPackage("..domain..")
      .should()
      .notBePublic()
      .because("the implementation of an aggregate shoduld be hidden");
}
