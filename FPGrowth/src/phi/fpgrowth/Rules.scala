package phi.fpgrowth

/**
 * Created by IntelliJ IDEA.
 * User: phi
 * Date: 9/29/11
 * Time: 9:16 PM
 * To change this template use File | Settings | File Templates.
 */

case class Rule(anticedent:List[String],consequent:List[String], confidence:Double) {
  override def toString:String = "%s -> %s: %f" format (anticedent,consequent,confidence)
}

object Rules{
  def mineRules(){
    val tree = new FPTree()
  }
}