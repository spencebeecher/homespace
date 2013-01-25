package phi.fpgrowth

import collection.mutable.{HashMap, ListBuffer}


/**
 * Created by IntelliJ IDEA.
 * User: phi
 * Date: 9/10/11
 * Time: 10:40 PM
 * To change this template use File | Settings | File Templates.
 */

case class HeaderItem(id: Int, item: String, frequency: Int) {
  var node:FPNode = null

  override def toString: String = {
    var s = "item: %s id: %d freq: %d- " format(item, id, frequency)
    var curr = node
    while (curr != null) {
      s += " " + curr
      curr = curr.next
    }
    return s
  }
}

object Main{
  def main(args:Array[String]) ={
    var arr = List[List[String]](List("hannah","spencer"),List("hannah","spencer","rika","fred","mom"),List("spencer","mom"),List("hannah","rika","fred"),List("hannah","spencer","gordon"))
    var tup = arr.map((li)=> li.map((s)=>(s,1)))
    val tree = new FPTree()
    tree.init(tup,1)
    println(tree.headerTable.foreach((x)=>println(x)))
    println(tree)
    println(tree.headerTable.head.item)
    println(tree.conditionalTreeDatasource(tree.headerTable(tree.mappingTable("mom")).node))
    tree.fpGrowth().foreach((x)=>{
      println(x)
    })

  }
}

class FPTree {
  val root = new FPNode(-1, null)
  var headerTable : Array[HeaderItem] = null
  val mappingTable = new HashMap[String, Int]
  var baseTree: FPTree = null
  val permute = new Permutation[Tuple2[String,Int]]
  var support = 1
  def init(dataSource: scala.Iterable[List[Tuple2[String, Int]]], support: Int, bt: FPTree = null) {
    baseTree = bt
    this.support = support
    var ht = new ListBuffer[HeaderItem]
    val header = new HashMap[String, Int]
    dataSource.foreach((trans) => {
      trans.foreach((x: Tuple2[String, Int]) => {
        if (!header.contains(x._1)) {
          header.put(x._1, x._2)
        } else {
          header(x._1) += x._2
        }
      })
    })
    var count = 0
    header.toList.filter((x) => x._2 >= support).sort((e1, e2) => e1._2 < e2._2).foreach((h) => {
      ht += (new HeaderItem(count, h._1, h._2))
      mappingTable.put(h _1 , count)
      count = count + 1
    })
    headerTable = ht.toArray

    dataSource.foreach((trans: List[Tuple2[String, Int]]) => {
      val transaction = trans.filter((x) => mappingTable.contains(x._1)).map((x) => (mappingTable(x._1), x._2))
        .sort((e1, e2) => e1._1 > e2._1)

      if (transaction.size > 0) {
        if (!this.root.children.contains(transaction(0)._1)) {
          this.root.children.put(transaction(0)._1, new FPNode(transaction(0)._1, this.root))
          insertHeader(root.children(transaction(0)._1))
        }
        root.children(transaction(0)._1).add(transaction, 0, this)
      }
    })
  }

  override def toString:String = root.strVal(headerTable,"")

  def insertHeader(fpNode: FPNode) = {
    var curr: FPNode = this.headerTable(fpNode.id).node
    if (curr == null) {
      this.headerTable(fpNode.id).node = fpNode
    } else {
      while (curr.next != null) {
        curr = curr.next
      }
      curr.next = fpNode
    }
  }

  def conditionalTreeDatasource(link: FPNode):List[List[Tuple2[String,Int]]] = {
    var currLink = link
    var patterns = List[List[Tuple2[String, Int]]]()
    while (currLink != null) {


      val support = currLink.frequency
      var currNode = currLink.parent
      var pattern = List[Tuple2[String,Int]]()
      while (currNode != root) {
        pattern = (headerTable(currNode.id).item, support)::pattern
        currNode = currNode.parent
      }
      if (pattern.size > 0) {
        patterns =  pattern::patterns
      }
      currLink = currLink.next
    }
    return patterns
  }

  def singleChain(curr : FPNode = root) : Boolean = {
    if(curr.children.size > 1){
        return false
    }
    if(curr.children.size == 0){
      return true
    }
    return singleChain(curr.children.values.head)

  }
  def findFrequency(p:List[Tuple2[String,Int]]):Tuple2[List[String],Int]={
    return (p.map(_._1),p.map(_._2).min)
  }
  def fpGrowth(pattern : List[Tuple2[String,Int]] = List[Tuple2[String,Int]]()) : List[Tuple2[List[String],Int]]= {
    if(singleChain()){
      var optional = List[Tuple2[String,Int]]()
      if(root.children.size > 0){
        var curr = root.children.head
        while(curr._2.children.size>0){
          optional = (headerTable(curr._2.id).item,curr._2.frequency)::optional
          curr = curr._2.children.head

        }
        optional = (headerTable(curr._2.id).item,curr._2.frequency)::optional
      }

      val l:List[List[Tuple2[String,Int]]] = permute.permuteAll(optional.toArray).toList
      return l.map((x)=>findFrequency(x:::pattern))

    }else{
      val listRet = new ListBuffer[Tuple2[List[String],Int]]
      for(val x <- 0 until headerTable.size){
        val i = headerTable.size -1 - x
        if(this.support <= headerTable(i).frequency){
          var patternBase:List[Tuple2[String,Int]]= pattern
          patternBase =  (headerTable(i).item,headerTable(i).frequency)::patternBase

          val condTree = new FPTree
          condTree.init(this.conditionalTreeDatasource(headerTable(i).node),this.support)

          val v =  findFrequency(patternBase)::condTree.fpGrowth(patternBase)
          listRet.appendAll(v)
        }
      }
      return listRet.toList


    }
  }


}