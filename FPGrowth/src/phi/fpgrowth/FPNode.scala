package phi.fpgrowth

import collection.mutable.{ListBuffer, HashMap}


/**
 * Created by IntelliJ IDEA.
 * User: phi
 * Date: 9/10/11
 * Time: 12:53 PM
 * To change this template use File | Settings | File Templates.
 */
class FPNode(val id: Int, var parent: FPNode) {
  var frequency = 0
  var next: FPNode = null
  var children = new HashMap[Int, FPNode]
  override def toString:String = "id: %s freq %d" format (id,frequency)


  def strVal(mappings:Array[HeaderItem],spaces: String = "") : String= {
    var accum = ""
    if(id!=(-1)){
      accum = "%s%s: %d\n" format (spaces, mappings(id).item,frequency)
    }else{
      accum = "root\n"
    }

    children.foreach((x)=>{
      accum += x._2.strVal(mappings,spaces+"     ")
    })
    return accum

  }
  def add(pattern: List[Tuple2[Int, Int]], index: Int, tree: FPTree): Boolean = {
    if (pattern.size == index + 1 && this.id == pattern(index)._1) {
      frequency += pattern(index)._2
    } else {
      if (!children.contains(pattern(index+1)._1)) {
        val node = new FPNode(pattern(index + 1)._1, this)
        children.put(pattern(index + 1)._1, node)
        tree.insertHeader(node)
      }
      frequency += pattern(index)._2
      children(pattern(index + 1)._1).add(pattern, index + 1, tree)

    }
    return true
  }
}


