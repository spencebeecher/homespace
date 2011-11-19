package phi.inference

import collection.mutable.HashMap


/**
 * Created by IntelliJ IDEA.
 * User: phi
 * Date: 11/5/11
 * Time: 10:48 AM
 *
 * This is a basic graph class that is generic on nodes and edges
 */
class Graph[T <: Comparable[T],E >: Null](){
  val map = new HashMap[T, HashMap[T,E]]

  def nodes = map.keySet ++ map.map((node)=>{
      node._2.map(edge=>{
        edge._1
      })
  }).flatten.toSet[T]

  def edges = map.map((node)=>{
      node._2.map(edge=>{
        (node._1,  edge._1, edge._2)
      })
  }).flatten

  /**
   * Get the nodes that n can see
   */
  def neighbors(n:T):Set[T] = {
    if(map.contains(n)){
      return map(n).map((node)=>{
        node._1
      }).toSet
    }else{
      return Nil.toSet
    }
  }


  /**
   * Get the parents of n
   */
  def parents(n:T, undirected: Graph[T, E]) : Set[T]= {
    //dont look at this method
    val nbr = neighbors(n)
    return undirected.neighbors(n).filter((t:T)=>{!nbr.contains(t)}).toSet[T]

  }

  def addNode(n:T){
    if(!map.contains(n)) map.put(n,new HashMap[T, E])
  }
  def addEdge(src:T, dst:T,  value:E = null){
    addNode(src)
    map(src).put(dst,value)
  }

  def addUndirectedEdge(src:T, dst:T,  value:E = null){
    addEdge(src,dst,value)
    addEdge(dst,src,value)
  }
  def node(n:T) = map(n)
  def edge(src:T, dst:T) = map(src)(dst)

  /**
   * Create an undirected graph from this graph
   */
  def createUndirected:Graph[T, E] ={
    val udG = new Graph[T, E]
    edges.foreach((x)=>{
      udG.addUndirectedEdge(x._1,x._2,x._3)
    })
    return udG
  }


  override def toString:String={
    map.map(x=>{
      x._1 + "   ==>   "+ x._2.keys.mkString(", ")
    }).mkString("\n")
  }

}

