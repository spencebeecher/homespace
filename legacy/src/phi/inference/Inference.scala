package phi.inference

import collection.mutable.{HashSet, HashMap}


/**
 * Created by IntelliJ IDEA.
 * User: phi
 * Date: 11/5/11
 * Time: 10:48 AM
 * To change this template use File | Settings | File Templates.
 */


object Inference {

  def getOrder(g:InfGraph):HashMap[String, Int]={
      val undNet = g.createUndirected

    //step 1:  assign i to the vertex with the most marked neighbors

    val nodeOrder = new HashMap[String, Int]
    var currStep1 = ""
    while(nodeOrder.size != undNet.nodes.size){



      var candadates = undNet.nodes.toList.map(_.toString).filter((x)=>{
        !nodeOrder.contains(x)
      })

      candadates = candadates.sortWith((x1:String,x2:String)=>{
          //num marked adj nodes

          val x1Neighbor = undNet.neighbors(x1).filter(x=>nodeOrder.contains(x)).size
          val x2Neighbor = undNet.neighbors(x2).filter(x=>nodeOrder.contains(x)).size

          if(x1Neighbor - x2Neighbor == 0){
            x1.compare(x2) < 0
          }else{
            (x1Neighbor - x2Neighbor) > 0
          }
      })


        currStep1 = candadates.head

        nodeOrder.put(currStep1, nodeOrder.size)
    }
    return nodeOrder
  }
  class InfGraph extends Graph[String, String]{

  }
  def moralize(g:InfGraph) : Graph[String,String]={
    val und = g.createUndirected
    val und2 = g.createUndirected
    g.nodes.foreach((x)=>{
      //for each node add edges between parents
       g.parents(x,und).foreach((p1)=>{
         g.parents(x,und).foreach((p2)=>{
           if(!p1.equals(p2)){
             und2.addEdge(p1,p2)
           }
         })
       })
    })

    return und2
  }

  def triangulate(und:Graph[String, String], order:HashMap[String, Int]) : Graph[String,String]={
    val orderedNodes = und.nodes.toList.map(_.toString).sortWith((x,y)=> order(x)> order(y))
    var ret = und.createUndirected

    orderedNodes.foreach((x:String)=>{
      //for each node add edges between parents
       ret.neighbors(x).toList.foreach((c1)=>{
         ret.neighbors(x).toList.foreach((c2)=>{
            if(!c1.equals(c2) && order(c1)<order(x) && order(c2)<order(x)){
              ret.addUndirectedEdge(c1,c2)
            }
         })
       })
    })

    return  ret
  }

  def parentSet(n:String,  und:Graph[String, String], order:HashMap[String, Int]): Set[String] = {
    return und.neighbors(n).filter((x)=>order(x)<order(n)).toSet + n
  }

  def createJoinTree(cliques: List[Tuple2[Set[String],Int]], factorMap: HashMap[String,Factor]) : Graph[Clique,CliqueEdge]={
    var orderedClique = cliques.sortBy(_._2)
    for(val i <- 0 until orderedClique.size){
      val options = orderedClique.slice(0,i)
      println(options)

    }
    return null
  }


  case class CliqueEdge(common:HashSet[String],var sepset:HashSet[Factor]){

  }

  case class Clique(values:List[String],factor:Factor) extends Comparable[Clique]{
    var workingFactors:HashSet[Factor] = HashSet(factor)
    def compareTo(o: Clique) = factor.toString.compareTo(o.factor.toString)
  }

  abstract class Factor extends Comparable[Factor]{
    def sumOver(factors:HashMap[String,Boolean]):Double
    def getFactorNames:HashSet[String]
    def getToStrings:HashSet[String]
    override def toString:String
    def compareTo(o: Factor) =toString.compare(o.toString)
  }

  case class FactorOfFactors(factor1:Factor, factor2:Factor) extends Factor{
    def getFactorNames:HashSet[String] ={
      val ret = new HashSet[String]
      factor1.getFactorNames.foreach(ret.add)
      factor2.getFactorNames.foreach(ret.add)
      return ret
    }

    def getToStrings:HashSet[String] ={
      val ret = new HashSet[String]
      factor1.getToStrings.foreach(ret.add)
      factor2.getToStrings.foreach(ret.add)
      return ret
    }
    override def toString:String={
      return List(factor1.toString, factor2.toString).sortWith(_.compare(_)>0).mkString("[",",","]")
    }
    override def sumOver(factors:HashMap[String,Boolean]):Double={
      return factor1.sumOver(factors) * factor2.sumOver(factors)
    }
  }

  case class SingleFactor(name:String, on:Double, off:Double) extends Factor{
    def getFactorNames = HashSet(name)
    def getToStrings= HashSet(toString)

    override def toString:String={
      return List(name.toString).sortWith(_.compare(_)>0).mkString("[",",","]")
    }
    override def sumOver(factors:HashMap[String,Boolean]):Double={
      if(factors(name)){
        return on
      }else{
        return off
      }

    }
  }

  case class DoubleFactor(name1:String, name2:String, tt:Double, tf:Double, ft:Double, ff:Double ) extends Factor{
    def getFactorNames = HashSet(name1,name2)

    def getToStrings= HashSet(toString)

    override def toString:String={
      return List(name1,name2).sortWith(_.compare(_)>0).mkString("[",",","]")
    }
    override def sumOver(factors:HashMap[String, Boolean]):Double={
      if(factors(name1)){
       if(factors(name2)) {
        return tt
       }else{
         return tf
       }
      }else{
        if(factors(name2)) {
         return ft
       }else{
         return ff
       }
      }
    }
  }



  def createCliques(und:Graph[String,String], order:HashMap[String, Int]) : List[Tuple2[Set[String],Int]]={
    val orderedNodes = und.nodes.toList.map(_.toString).sortWith((x,y)=> order(x)> order(y))
    val cliqued = new HashSet[String]()
    var ret =List[Tuple2[Set[String],Int]]()
    orderedNodes.foreach(n=>{
      println(n)
      if(cliqued.size != und.nodes.size){
        val pset = parentSet(n,und,order)
        ret = (pset, pset.map(order.apply).max)::ret
        pset.foreach(cliqued.add)
      }
    })
    return ret
  }

  def main(args: Array[String]) {
    val net = new InfGraph
    net.addEdge("a","b")
    net.addEdge("b","c")
    net.addEdge("b","d")
    var undNet = moralize(net)
    //step 1 of pearls:  assign i to the vertex with the most marked neighbors
    val nodeOrder = getOrder(net)

    println("Node order %s" format nodeOrder.toString)
    val orderedNodes = net.nodes.toList.map(_.toString).sortWith((x,y)=> nodeOrder(x)> nodeOrder(y))
    println("Ordered Nodes " + orderedNodes)

    //step 2 of pearls: fill in edges between any two nonadjacent parents of n
    undNet = triangulate(undNet,nodeOrder)
    println("Triangulated graph:")
    println(undNet.toString)
    println()

    var cliques = createCliques(undNet,nodeOrder)
    cliques = cliques.sortBy(_._2)
    println("Cliques:")
    println(cliques)

    val aFact= SingleFactor("a",.3,.7)
    val bGivenAFact = DoubleFactor("b","a",.7,.2,.3,.8)
    val dGivenBFact = DoubleFactor("b","a",.7,.2,.3,.8)
    val GivenBFact = DoubleFactor("b","a",.7,.2,.3,.8)


  }
}