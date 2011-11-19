package phi.inference.MessagePassing

import collection.mutable.{ListBuffer, HashSet, HashMap}
import phi.inference.Inference.InfGraph
import phi.inference.Graph
import phi.inference.MessagePassing.SumOver.Fact


/**
 * Created by IntelliJ IDEA.
 * User: phi
 * Date: 11/6/11
 * Time: 8:53 AM
 *
 * Do all computations for clique finding
 */

object Cliques {

  /**
   * Undirectionalify and create edges between parent nodes
   */
  def moralize(g: InfGraph): Graph[String, String] = {
    val und = g.createUndirected
    val und2 = g.createUndirected

    //For each node add edges between parents in the new undirected graph
    g.nodes.foreach((x) => {
      //for each node add edges between parents
      g.parents(x, und).foreach((p1) => {
        g.parents(x, und).foreach((p2) => {
          if (!p1.equals(p2)) {
            und2.addEdge(p1, p2)
          }
        })
      })
    })

    return und2
  }


  /**
   *  Triangulate the graph by Pearson's Fill in alg
   *
   *  for n = |V| to 1:
   *    fill in edges between any two non adjacent neighbors of n that have lower ranks than n
   */
  def triangulate(und: Graph[String, String], order: HashMap[String, Int]): Graph[String, String] = {
    val orderedNodes = und.nodes.toList.map(_.toString).sortWith((x, y) => order(x) > order(y))
    var ret = und.createUndirected

    orderedNodes.foreach((x: String) => {
      //for each node add edges between parents

      //find non adjacent neighbors of n
      ret.neighbors(x).toList.foreach((c1) => {
        ret.neighbors(x).toList.foreach((c2) => {

          //if c1 and c2 are different and both have lower order then add an edge between them
          //  (it is ok to add an edge that already exists)
          if (!c1.equals(c2) && order(c1) < order(x) && order(c2) < order(x)) {
            ret.addUndirectedEdge(c1, c2)
          }
        })
      })
    })

    return ret
  }


  /**
   * Get the nodes that n is connected to that have higher order than n
   */
  def parentSet(n: String, und: Graph[String, String], order: HashMap[String, Int]): Set[String] = {
    return und.neighbors(n).filter((x) => order(x) < order(n)).toSet + n
  }


  /**
   * Given a chordal graph, and an ordering of cliques created from that graph
   * Create the JoinTree for those cliques
   *
   * Form the join tree by connecting each Ci to a predecessor Cj (j<i) sharing the highest number of verticies
   * with Ci
   */
  def createJoinTree(cliques: List[Tuple2[Set[String], Int]], factors: List[Fact]): Graph[Clique, CliqueEdge] = {
    val retGraph = new Graph[Clique, CliqueEdge]

    //Sort the cliques by their order
    var orderedClique = cliques.sortBy(_._2)

    for (val i <- 0 until orderedClique.size) {

      //This is the list of cliques that are available to join Ci with (cliques with lower order)
      //  IE the (j<i) condition
      val options = orderedClique.slice(0, i)

      //Ci
      val currClique = orderedClique(i)

      //find the factors that belong to the clique
      val currCliqueFactors = new ListBuffer[HashMap[String, Double]]
      factors.foreach((f) => {
        if (f.getFactorNames.subsetOf(currClique._1)) {
          currCliqueFactors.appendAll(f.distribs)
        }
      })

      //assign the factors (that were passed in)  to the clique that they belong to
      val cliqueNode = Clique(currClique._1, Fact(currCliqueFactors.toList))

      //add the clique to the clique tree
      retGraph.addNode(cliqueNode)

      //If we have options to connect this clique to then look through those options
      if (options.size > 0) {

        //assume the first element in options is the closest
        var closestClique = options.head

        options.tail.foreach((clique) => {


          if (clique._1.intersect(currClique._1).size > closestClique._1.intersect(currClique._1).size) {
            //if clique intersects with more variables than our closestClique make current our closes
            closestClique = clique
          } else if (clique._1.intersect(currClique._1).size == closestClique._1.intersect(currClique._1).size) {

            //if they share the same amount in common then pick the one that is lexagraphically smallest
            val nameClique = clique._1.toList.sortWith((x, y) => x.compare(y) < 0).mkString(",")
            val nameClosestClique = closestClique._1.toList.sortWith((x, y) => x.compare(y) < 0).mkString(",")
            if (nameClique < nameClosestClique) {
              closestClique = clique
            }
          }
        })

        //find that clique in the graph, it must have been added in a previous step due to the j<i constraint
        val neighbor: Clique = retGraph.nodes.find((c: Clique) => {
          c.values.subsetOf(closestClique._1) && closestClique._1.subsetOf(c.values)
        }).get

        //add an edge between the Ci and its closest match
        retGraph.addUndirectedEdge(cliqueNode, neighbor, CliqueEdge(closestClique._1.intersect(neighbor.values), new HashMap[String, Fact]))

      }

    }
    return retGraph
  }


  case class CliqueEdge(common: Set[String], var sepset: HashMap[String, Fact]) {
  }

  case class Clique(values: Set[String], factor: Fact) extends Comparable[Clique] {

    //Factors that are used in the Belief Update algorithm
    //  a dictionary from factor variables to the factor
    var workingFactors: HashMap[String, Fact] = {
      val map = new HashMap[String, Fact]
      map.put(factor.originalFactors, factor)
      map
    }

    //Get the set of names for all factors in this clique
    var getFactorNames = factor.getFactorNames
    var updated = true

    def compareTo(o: Clique) = factor.toString.compareTo(o.factor.toString)
  }

  /**
   * Return a dictionary representing the order of all the nodes in a graph
   */
  def getOrder(g: InfGraph): HashMap[String, Int] = {
    val undNet = g.createUndirected

    //step 1:  assign i to the vertex with the most marked neighbors

    val nodeOrder = new HashMap[String, Int]
    var currStep1 = ""
    while (nodeOrder.size != undNet.nodes.size) {


      var candadates = undNet.nodes.toList.map(_.toString).filter((x) => {
        !nodeOrder.contains(x)
      })

      //Sort candadates by the number of adjacent nodes
      // If a tie exists break lexagraphically
      candadates = candadates.sortWith((x1: String, x2: String) => {
        //num marked adj nodes

        val x1Neighbor = undNet.neighbors(x1).filter(x => nodeOrder.contains(x)).size
        val x2Neighbor = undNet.neighbors(x2).filter(x => nodeOrder.contains(x)).size

        if (x1Neighbor - x2Neighbor == 0) {
          x1.compare(x2) < 0
        } else {
          (x1Neighbor - x2Neighbor) > 0
        }
      })


      currStep1 = candadates.head

      //add the node name with its order to the nodeOrder dictionary
      nodeOrder.put(currStep1, nodeOrder.size)
    }
    return nodeOrder
  }


  /**
   * Create cliques from the graph and their order
   * Return a list of cliques with their corresponding clique orders
   */
  def createCliques(und: Graph[String, String], order: HashMap[String, Int]): List[Tuple2[Set[String], Int]] = {

    //order from greatest to least
    val orderedNodes = und.nodes.toList.map(_.toString).sortWith((x, y) => order(x) > order(y))

    //nodes that belong to a clique
    val cliqued = new HashSet[String]()

    var ret = List[Tuple2[Set[String], Int]]()

    //for each ordered node: from |V| to 1
    //  assign that nodes parents (by order) to that nodes clique
    //    make that clique's order the max order of any node in that clique
    orderedNodes.foreach(n => {
      if (cliqued.size != und.nodes.size) {
        val pset = parentSet(n, und, order)
        ret = (pset, pset.map(order.apply).max) :: ret
        pset.foreach(cliqued.add)
      }
    })
    return ret
  }
}