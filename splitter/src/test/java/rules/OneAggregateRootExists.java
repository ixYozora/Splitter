package rules;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.library.dependencies.Slice;
import java.util.List;
import propra2.splitter.stereotypes.AggregateRoot;

import static com.tngtech.archunit.lang.SimpleConditionEvent.satisfied;
import static com.tngtech.archunit.lang.SimpleConditionEvent.violated;
import static java.util.stream.Collectors.toList;

public class OneAggregateRootExists extends ArchCondition<Slice> {

  public static final OneAggregateRootExists ONE_AGGREGATE_ROOT_EXISTS =
      new OneAggregateRootExists("exactly one Aggregate Root exists");

  OneAggregateRootExists(String description, Object... args) {
    super(description, args);
  }

  public void check(Slice slice, ConditionEvents events) {
    List<String> rootNames = getAggregateRootNames(slice);
    String packageName = slice.iterator().next().getPackageName();
    int numberOfAggregateRoots = rootNames.size();
    switch (numberOfAggregateRoots) {
      case 0:
        events.add(violated(slice, packageName + " has no aggregate root"));
        break;
      case 1:
        events.add(satisfied(slice, "ok!"));
        break;
      default:
        events.add(violated(slice, packageName + " has more than one aggregate root"));
    }
  }

  private List<String> getAggregateRootNames(Slice slice) {
    return slice.stream()
        .filter(c -> c.isAnnotatedWith(AggregateRoot.class))
        .map(JavaClass::getName)
        .collect(toList());
  }


}
