package daikon.inv.ternary.threeScalar;

import daikon.*;
import daikon.inv.Invariant;
import daikon.derive.unary.*;
import daikon.derive.binary.*;
import java.util.*;
import utilMDE.*;

public class LinearTernary extends ThreeScalar {

  public final static boolean debugLinearTernary = false;
  // public final static boolean debugLinearTernary = true;

  public LinearTernaryCore core;

  protected LinearTernary(PptSlice ppt) {
    super(ppt);
    core = new LinearTernaryCore(this);
  }

  public static LinearTernary instantiate(PptSlice ppt) {
    VarInfo x = ppt.var_infos[0];
    VarInfo y = ppt.var_infos[1];
    VarInfo z = ppt.var_infos[2];

    VarInfo x_summand = null;
    VarInfo y_summand = null;
    VarInfo z_summand = null;
    if (x.derived instanceof SequenceSum) x_summand = ((SequenceSum) x.derived).base;
    if (y.derived instanceof SequenceSum) y_summand = ((SequenceSum) y.derived).base;
    if (z.derived instanceof SequenceSum) z_summand = ((SequenceSum) z.derived).base;

    if ((x_summand != null) && (y_summand != null) && (z_summand != null)) {
      // all 3 of x, y, and z are "sum(...)"
      // avoid sum(a[0..i]) + sum(a[i..]) = sum(a[])

      if (debugLinearTernary) {
        System.out.println(ppt.varNames() + " 3 summands: " + x_summand.name + " " + y_summand.name + " " + z_summand.name);
      }

      VarInfo x_seq = x_summand.isDerivedSubSequenceOf();
      VarInfo y_seq = y_summand.isDerivedSubSequenceOf();
      VarInfo z_seq = z_summand.isDerivedSubSequenceOf();
      if (x_seq == null) x_seq = x_summand;
      if (y_seq == null) y_seq = y_summand;
      if (z_seq == null) z_seq = z_summand;
      VarInfo seq = x_seq;

      if (debugLinearTernary) {
        System.out.println(ppt.varNames() + " 3 sequences: " + x_seq.name + " " + y_seq.name + " " + z_seq.name);
      }

      if (((seq == x_summand) || (seq == y_summand) || (seq == z_summand))
          && (x_seq == y_seq) && (x_seq == z_seq)) {
        Assert.assert(y_seq == z_seq);
        if (debugLinearTernary) {
          System.out.println(ppt.varNames() + " 3 sequences match");
        }

        SequenceScalarSubsequence part1 = null;
        SequenceScalarSubsequence part2 = null;
        for (int i=0; i<3; i++) {
          VarInfo vi = ppt.var_infos[i];
          VarInfo summand = ((SequenceSum) vi.derived).base;
          if (debugLinearTernary) {
            System.out.println("considering: " + summand.name + " " + vi.name);
          }
          if (summand.derived instanceof SequenceScalarSubsequence) {
            SequenceScalarSubsequence sss = (SequenceScalarSubsequence) summand.derived;
            if (sss.from_start) {
              part1 = sss;
            } else {
              part2 = sss;
            }
          } else {
            if (debugLinearTernary) {
              System.out.println("Not subseq: " + summand.name + " " + vi.name);
            }
          }
        }
        if (debugLinearTernary) {
          System.out.println(ppt.varNames() + " part1=" + part1 + ", part2=" + part2 + ", seq=" + seq.name);
        }
        if ((part1 != null) && (part2 != null)) {
          // now part1, and part2 are set
          if ((part1.sclvar() == part2.sclvar())
              && (part1.index_shift + 1 == part2.index_shift)) {
            if (debugLinearTernary) {
              System.out.println("LinearTernary suppressed: " + ppt.varNames());
            }
            Global.implied_noninstantiated_invariants += 1;
            return null;
          }
        }
      }
    } else if ((x_summand != null) && (y_summand != null)
               || ((x_summand != null) && (z_summand != null))
               || ((y_summand != null) && (z_summand != null))) {
      // two of x, y, and z are "sum(...)"
      // avoid sum(a[0..i-1]) + a[i] = sum(a[0..i])

      // if (debugLinearTernary) {
      //   System.out.println(ppt.varNames() + " 2 summands: " + x_summand + " " + y_summand + " " + z_summand);
      // }

      // The intention is that parta is a[0..i], partb is a[0..i-1], and
      // notpart is a[i].
      VarInfo parta;
      VarInfo partb;
      VarInfo notpart;

      if (x_summand != null) {
        parta = x;
        if (y_summand != null) {
          partb = y;
          notpart = z;
        } else {
          partb = z;
          notpart = y;
        }
      } else {
        notpart = x;
        parta = y;
        partb = z;
      }
      parta = ((SequenceSum) parta.derived).base;
      partb = ((SequenceSum) partb.derived).base;
      VarInfo seq = null;
      VarInfo eltindex = null;
      if (notpart.derived instanceof SequenceScalarSubscript) {
        SequenceScalarSubscript sss = (SequenceScalarSubscript) notpart.derived;
        seq = sss.seqvar();
        eltindex = sss.sclvar();
      }
      if ((seq != null)
          && (seq == parta.isDerivedSubSequenceOf())
          && (seq == partb.isDerivedSubSequenceOf())) {
        // For now, don't deal with case where one variable is the entire
        // sequence.
        if (! ((parta == seq) || (partb == seq))) {
          SequenceScalarSubsequence a_sss = (SequenceScalarSubsequence) parta.derived;
          SequenceScalarSubsequence b_sss = (SequenceScalarSubsequence) partb.derived;
          if ((a_sss.sclvar() == eltindex)
              && (b_sss.sclvar() == eltindex)) {
            if ((a_sss.from_start
                 && b_sss.from_start
                 && (((a_sss.index_shift == -1) && (b_sss.index_shift == 0))
                     || ((a_sss.index_shift == 0) && (b_sss.index_shift == -1))))
                || ((! a_sss.from_start)
                    && (! b_sss.from_start)
                    && (((a_sss.index_shift == 0) && (b_sss.index_shift == 1))
                        || ((a_sss.index_shift == 1) && (b_sss.index_shift == 0))))) {
            Global.implied_noninstantiated_invariants += 1;
            return null;
            }
          }
        }
      }
    }

    LinearTernary result = new LinearTernary(ppt);
    if (debugLinearTernary) {
      System.out.println("LinearTernary.instantiate: " + result.repr());
    }
    return result;
  }

  public String repr() {
    return "LinearTernary" + varNames() + ": "
      + "no_invariant=" + no_invariant
      + "; " + core.repr();
  }

  public String format() {
    return core.format(var1().name.name(), var2().name.name(), var3().name.name());
  }

  public String format_esc() {
    return core.format(var1().name.esc_name(), var2().name.esc_name(), var3().name.esc_name());
  }

  // public String format_reversed() {
  //   return core.format_reversed(var1().name.name(), var2().name.name(), var3().name.name());
  // }
  //
  // public String format_esc_reversed() {
  //   return core.format_reversed(var1().name.esc_name(), var2().name.esc_name(), var3().name.esc_name());
  // }

  public String format_simplify() {
    return "format_simplify " + this.getClass() + " needs to be changed: " + format();
  }

  public void add_modified(long x, long y, long z, int count) {
    core.add_modified(x, y, z, count);
  }

  protected double computeProbability() {
    return core.computeProbability();
  }

  public boolean isExact() {
    return true;
  }

  public boolean isObviousDerived() {
    // VarInfo var1 = ppt.var_infos[0];
    // VarInfo var2 = ppt.var_infos[1];
    // VarInfo var3 = ppt.var_infos[2];

    return false;
  }


  public boolean isSameFormula(Invariant other)
  {
    return core.isSameFormula(((LinearTernary) other).core);
  }

  public boolean isExclusiveFormula(Invariant other)
  {
    if (other instanceof LinearTernary) {
      return core.isExclusiveFormula(((LinearTernary) other).core);
    }
    return false;
  }


  // Look up a previously instantiated invariant.
  public static LinearTernary find(PptSlice ppt) {
    Assert.assert(ppt.arity == 3);
    for (Iterator itor = ppt.invs.iterator(); itor.hasNext(); ) {
      Invariant inv = (Invariant) itor.next();
      if (inv instanceof LinearTernary)
        return (LinearTernary) inv;
    }
    return null;
  }

  // Returns a vector of LinearTernary objects.
  // This ought to produce an iterator instead.
  public static Vector findAll(VarInfo vi) {
    Vector result = new Vector();
    for (Iterator itor = vi.ppt.views_iterator() ; itor.hasNext() ; ) {
      PptSlice view = (PptSlice) itor.next();
      if ((view.arity == 3) && view.usesVar(vi)) {
        LinearTernary lt = LinearTernary.find(view);
        if (lt != null) {
          result.add(lt);
        }
      }
    }
    return result;
  }

}
