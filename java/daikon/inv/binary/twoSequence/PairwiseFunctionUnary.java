package daikon.inv.binary.twoSequence;

import daikon.*;
import daikon.inv.Invariant;
import daikon.inv.binary.twoScalar.*;
import java.lang.reflect.*;


public class PairwiseFunctionUnary extends TwoSequence {

  // Variables starting with dkconfig_ should only be set via the
  // daikon.config.Configuration interface.
  public static boolean dkconfig_enabled = true;

  FunctionUnaryCore core;

  protected PairwiseFunctionUnary(PptSlice ppt, String methodname, Method function, boolean inverse) {
    super(ppt);
    core = new FunctionUnaryCore(this, methodname, function, inverse);
  }

  public static PairwiseFunctionUnary instantiate(PptSlice ppt, String methodname, Method function, boolean inverse) {
    if (!dkconfig_enabled) return null;
    return new PairwiseFunctionUnary(ppt, methodname, function, inverse);
  }

  public String repr() {
    return "PairwiseFunctionUnary" + varNames() + ": " + core.repr();
  }

  public String format() {
    return core.format(var1().name, var2().name);
  }

  public String format_esc() {
    return "format_esc " + this.getClass() + " needs to be changed: " + format();
  }

  /* IOA */
  public String format_ioa(String classname) {
    if (var1().isIOASet() || var2().isIOASet())
      return "Not valid for sets: " + format();
    String[] form =
      VarInfoName.QuantHelper.format_ioa(new VarInfo[] { var1(), var2() }, classname);
    return form[0]+"("+form[4]+"="+form[5]+") => ("+core.format_ioa(form[1],form[2])+")"+form[3];
  }

  public String format_simplify() {
    return "format_simplify " + this.getClass() + " needs to be changed: " + format();
  }

  public void add_modified(long[] x_arr, long[] y_arr, int count) {
    if (x_arr.length != y_arr.length) {
      destroy();
      return;
    }
    int len = x_arr.length;
    // int len = Math.min(x_arr.length, y_arr.length);

    for (int i=0; i<len; i++) {
      long x = x_arr[i];
      long y = y_arr[i];

      core.add_modified(x, y, count);
      if (no_invariant)
        return;
    }
  }

  protected double computeProbability() {
    return core.computeProbability();
  }

  public boolean isSameFormula(Invariant other)
  {
    return core.isSameFormula(((PairwiseFunctionUnary) other).core);
  }

}
