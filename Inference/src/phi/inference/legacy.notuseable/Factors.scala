package phi.inference.legacy

import collection.mutable.{HashSet, HashMap}


/**
 * Created by IntelliJ IDEA.
 * User: phi
 * Date: 11/6/11
 * Time: 2:13 PM
 * To change this template use File | Settings | File Templates.
 */
/*

abstract class Factor extends Comparable[Factor] {
  def sumOver(factors: HashMap[String, Boolean]): Double

  def getFactorNames: HashSet[String]

  def getToStrings: HashSet[String]

  override def toString: String

  def compareTo(o: Factor) = toString.compare(o.toString)
}

case class FactorOfFactors(factor: List[Factor]) extends Factor {
  def getFactorNames: HashSet[String] = {
    val ret = new HashSet[String]
    factor.foreach(x => x.getFactorNames.foreach(ret.add))
    return ret
  }

  def getToStrings: HashSet[String] = {
    val ret = new HashSet[String]
    factor.foreach(x => x.getToStrings.foreach(ret.add))
    return ret
  }

  override def toString: String = {
    return factor.sortWith((x, y) => x.toString.compare(y.toString) < 0).mkString("Factor[", ",", "]")
  }

  override def sumOver(factors: HashMap[String, Boolean]): Double= {
      return factor.foldLeft(1.0)((d, f) => {
        f.sumOver(factors) * d
      })

  }
}
case class SingleFactor(name: String, off: Double, on: Double) extends Factor {

  def getFactorNames = HashSet(name)

  def getToStrings = HashSet(toString)

  override def toString: String = {
    return List(name.toString).sortWith(_.compare(_) < 0).mkString("Factor[", ",", "]")
  }

  override def sumOver(factors: HashMap[String, Boolean]): Double= {
      if (factors(name)) {
        return on
      } else {
        return off
      }
  }


}

case class DoubleFactor(variable: String, conditionedOn: String, ff: Double, tf: Double, ft: Double, tt: Double) extends Factor {
  def getFactorNames = HashSet(variable, conditionedOn)

  def getToStrings = HashSet(toString)

  override def toString: String = {
    return List(variable, conditionedOn).sortWith((x, y) => x.compare(y) < 0).mkString("Factor[", ",", "]")
  }

    override def sumOver(factors:HashMap[String, Boolean]): Double= {
    if (factors(variable)) {
      if (factors(conditionedOn)) {
        return tt
      } else {
        return tf
      }
    }else{
      if (factors(conditionedOn)) {
        return ft
      } else {
        return ff
      }
    }
  }
}
*/
