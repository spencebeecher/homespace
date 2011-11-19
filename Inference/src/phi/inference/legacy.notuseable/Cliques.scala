package phi.inference.legacy

import collection.mutable.{ListBuffer, HashSet, HashMap}
import phi.inference.Inference.InfGraph
import phi.inference.Graph


/**
 * Created by IntelliJ IDEA.
 * User: phi
 * Date: 11/6/11
 * Time: 8:53 AM
 * To change this template use File | Settings | File Templates.
 */

/*def inferOnBU(f:HashSet[Factor], aValues:List[Boolean],bValues:List[Boolean],cValues:List[Boolean],dValues:List[Boolean]):HashMap[Boolean,Double]={
    val bProb = new HashMap[Boolean, Double]
    bValues.foreach((bval) => {
      var sum = 0.0
      aValues.foreach((aval) => {
        dValues.foreach((dval) => {
          cValues.foreach((cval) => {

            var v = new HashMap[String, Boolean]()
            v += "a" -> aval
            v += "b" -> bval
            v += "c" -> cval
            v += "d" -> dval
            var prod = 1.0
            f.foreach((y) => {
              prod = y.sumOver(v) * prod
            })
            sum += prod
          })
        })
      })
      bProb.put(bval,sum)
    })
    return bProb
  }
  /*
   *  I am not going to do the full Belief Update algorithm.  I have already put in a lot of hours getting this far!
   *  What I am doing is moving the Summatioins all the way to the left.  By doing this I am eliminating a ton of extra
   *  code that I would have to write (Summing over distributions to get another distribution)
   *
   *  other than that detail the belief update algorithm remains unchanged
   */
  def SimulateBUMessage(cliqueTree: Graph[Clique, CliqueEdge]) {
    //BU message loop this is Initialize-CTree and BU-Message combined
    //Initialized CTree is passed in
    while (cliqueTree.nodes.filter((x: Clique) => x.updated).size > 0) {
      cliqueTree.edges.foreach((edge) => {
        edge._2.updated = false
        var factors = edge._1.workingFactors.clone()

        //divide by sepset
        edge._3.sepset.foreach(factors.remove)

        val workingSize = edge._2.workingFactors.size
        factors.foreach(edge._2.workingFactors.add)

        edge._3.sepset = factors

        edge._2.updated = workingSize < edge._2.workingFactors.size
      })
    }
  }*/



/*
object Cliques {

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

  def createJoinTree(cliques: List[Tuple2[Set[String],Int]], factors: List[Factor]) : Graph[Clique,CliqueEdge]={
    val retGraph = new Graph[Clique, CliqueEdge]
    var orderedClique = cliques.sortBy(_._2)
    for(val i <- 0 until orderedClique.size){
      val options = orderedClique.slice(0,i)
      val currClique = orderedClique(i)

      //find the factors that belong to the clique
      val currCliqueFactors = new ListBuffer[Factor]
      factors.foreach((f)=>{
        if(f.getFactorNames.subsetOf(currClique._1)){
          currCliqueFactors.append(f)
        }
      })
      val cliqueNode = Clique(currClique._1,FactorOfFactors(currCliqueFactors.toList))
      retGraph.addNode(cliqueNode)

      println(orderedClique(i))
      println(options)
      if(options.size > 0){
        var closestClique = options.head
        options.tail.foreach((clique)=>{
          if(clique._1.intersect(currClique._1).size > closestClique._1.intersect(currClique._1).size){
            closestClique = clique
          }else if(clique._1.intersect(currClique._1).size == closestClique._1.intersect(currClique._1).size){
            val nameClique = clique._1.toList.sortWith((x,y)=>x.compare(y)<0).mkString(",")
            val nameClosestClique = closestClique._1.toList.sortWith((x,y)=>x.compare(y)<0).mkString(",")
            if(nameClique < nameClosestClique){
              closestClique = clique
            }
          }
        })
        val neighbor:Clique = retGraph.nodes.find((c:Clique)=>{
          c.values.subsetOf(closestClique._1) && closestClique._1.subsetOf(c.values)
        }).get

        retGraph.addUndirectedEdge(cliqueNode,neighbor,CliqueEdge(closestClique._1.intersect(neighbor.values),new HashSet[Factor]))

      }

    }
    return  retGraph
  }


  case class CliqueEdge(common:Set[String],var sepset:HashSet[Factor]){
  }

  case class Clique(values:Set[String],factor:Factor) extends Comparable[Clique]{
    var workingFactors:HashSet[Factor] = HashSet(factor)
    var updated = true
    def compareTo(o: Clique) = factor.toString.compareTo(o.factor.toString)
  }


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
  }}


  */