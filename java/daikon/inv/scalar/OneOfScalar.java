package daikon.inv.scalar;

import daikon.*;
import daikon.inv.*;

import utilMDE.*;

import java.util.*;

// *****
// Automatically generated from OneOf-cpp.java
// *****

// States that the value is one of the specified values.

// This subsumes an "exact" invariant that says the value is always exactly
// a specific value.  Do I want to make that a separate invariant
// nonetheless?  Probably not, as this will simplify implication and such.

public final class OneOfScalar  extends SingleScalar  implements OneOf {
  final static int LIMIT = 5;	// maximum size for the one_of list
  // Probably needs to keep its own list of the values, and number of each seen.
  // (That depends on the slice; maybe not until the slice is cleared out.
  // But so few values is cheap, so this is quite fine for now and long-term.)

  private long [] elts;
  private int num_elts;

  OneOfScalar (PptSlice ppt) {
    super(ppt);

    elts = new long [LIMIT];

    num_elts = 0;
  }

  public static OneOfScalar  instantiate(PptSlice ppt) {
    return new OneOfScalar (ppt);
  }

  public int num_elts() {
    return num_elts;
  }

  public Object elt() {
    if (num_elts != 1)
      throw new Error("Represents " + num_elts + " elements");

    // Not sure whether interning is necessary (or just returning an Integer
    // would be sufficient), but just in case...
    return Intern.internedLong(elts[0]);

  }

  private String subarray_rep() {
    // Not so efficient an implementation, but simple;
    // and how often will we need to print this anyway?
    Arrays.sort(elts, 0, num_elts  );
    StringBuffer sb = new StringBuffer();
    sb.append("{ ");
    for (int i=0; i<num_elts; i++) {
      if (i != 0)
        sb.append(", ");
      sb.append( elts[i]  );
    }
    sb.append(" }");
    return sb.toString();
  }

  public String repr() {
    double probability = getProbability();
    return "OneOfScalar(" + var().name + "): "
      + "no_invariant=" + no_invariant
      + ", num_elts=" + num_elts
      + ", elts=" + subarray_rep();
  }

  public String format() {
    if (no_invariant || (num_elts == 0) || (! justified()))
      return null;
    if (num_elts == 1)
      return var().name  + " = " +  elts[0]  ;
    else
      return var().name  + " one of " + subarray_rep();
  }

  public void add_modified(long  v, int count) {

    for (int i=0; i<num_elts; i++)
      if (elts[i] == v)
        return;
    if (num_elts == LIMIT) {
      destroy();
      return;
    }
    elts[num_elts] = v;
    num_elts++;

  }

  protected double computeProbability() {
    // This is wrong; fix it
    return Invariant.PROBABILITY_JUSTIFIED;
  }

}
