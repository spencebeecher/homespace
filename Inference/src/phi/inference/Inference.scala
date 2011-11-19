package phi.inference

import collection.mutable.{ListBuffer, HashSet, HashMap}
import MessagePassing.Cliques.{Clique, CliqueEdge}
import MessagePassing.SumOver.Fact
import MessagePassing.{SumOver, Cliques}
import util.Random


/**
 * Created by IntelliJ IDEA.
 * User: phi
 * Date: 11/5/11
 * Time: 10:48 AM
 * To change this template use File | Settings | File Templates.
 */


object Inference {


  class InfGraph extends Graph[String, String] {

  }


  def main(args: Array[String]) {
    runBU()
    println("###########################################")
    println("Forward Sample")
    runForwardSample()

  }

  def productOfSums(cliqueTree:Graph[Clique,CliqueEdge], cliqueContaining: String, sumOverVar: String): HashMap[String, Double] = {

      var retDist = new HashMap[String, Double]


        var cliqueOfInterest:Clique = cliqueTree.nodes.toList.filter((c: Clique) => {
          c.factor.originalFactors.contains(cliqueContaining)
        }).toList.head


        var currDist = new HashMap[String, Double]

        //multiply across cliques
         cliqueOfInterest.workingFactors.values.map(_.sumOver(sumOverVar)).foreach((factor: Fact) => {
          factor.productDistribution.foreach(v => currDist.put(v._1, currDist.getOrElse(v._1, 1.0) * v._2))
        })

        //scale back if you have completely summed over a variable
        if (currDist.contains("")) {
          val scale = currDist("")
          currDist.remove("")
          currDist = currDist.map(prob => (prob._1, prob._2 * scale))
        }

        currDist.foreach(v => retDist.put(v._1, retDist.getOrElse(v._1, 0.0) + v._2))


      return retDist
    }

  def runBU() {
    val net = new InfGraph
    net.addEdge("a", "b")
    net.addEdge("b", "c")
    net.addEdge("b", "d")
    println("The graph:")
    println(net)
    println()
    println()

    var undNet = Cliques.moralize(net)
    println("The graph: moralized")
    println(net)
    println()
    println()

    //step 1 of pearls:  assign i to the vertex with the most marked neighbors
    val nodeOrder = Cliques.getOrder(net)

    val orderedNodes = net.nodes.toList.map(_.toString).sortWith((x, y) => nodeOrder(x) > nodeOrder(y))
    println("Ordered Nodes (step 1 of pearls):  " + orderedNodes)

    //step 2 of pearls: fill in edges between any two nonadjacent parents of n
    undNet = Cliques.triangulate(undNet, nodeOrder)
    println("Triangulated graph:")
    println(undNet.toString)
    println()
    println()

    var cliques = Cliques.createCliques(undNet, nodeOrder)
    cliques = cliques.sortBy(_._2)
    println("Cliques:")
    println(cliques)
    println()
    println()

    val aFact = new Fact(List(HashMap("a0" -> .7, "a1" -> .3)))

    val bGivenAFact = Fact(List(HashMap("a0b0" -> .8, "a0b1" -> .2, "a1b0" -> .3, "a1b1" -> .7)))

    val dGivenBFact = Fact(List(HashMap("d0b0" -> .1, "d0b1" -> .6, "d1b0" -> .9, "d1b1" -> .4)))

    val cGivenBFact = Fact(List(HashMap("c0b0" -> .75, "c0b1" -> .1, "c1b0" -> .25, "c1b1" -> .9)))


    val cliqueTree = Cliques.createJoinTree(cliques, List(aFact, bGivenAFact, dGivenBFact, cGivenBFact))

    println("Clique tree: => denotes edges to other cliques")
    println(cliqueTree)
    println()
    println()

    println("BU Update")
    SumOver.SimulateBUMessage(cliqueTree)

    println()

    println("P(B)")
    println(productOfSums(cliqueTree,"a","a"))
    println()


    println("P(B|a0)")
    val bAndA0Dict = productOfSums(cliqueTree,"a","a0")
    val a0  = bAndA0Dict.values.sum
    println("b1: %f" format (bAndA0Dict("b1")/a0))
    println("b0: %f" format (bAndA0Dict("b0")/a0))
    println()
    println()

    println("P(B|d0)")
    val bAndD0Dict = productOfSums(cliqueTree,"d","d0")
    val d0 = bAndD0Dict.values.sum
    println("b1: %f" format (bAndD0Dict("b1")/d0))
    println("b0: %f" format (bAndD0Dict("b0")/d0))
    println()
    println()



  }


  object Fixed extends Enumeration {
    type FV = Value
    val T, F, Unset = Value
  }

  def runForwardSample() {
    val r = new Random

    //sample this many times
    val size = 1000000


    var bFalseCount = 0
    for (val i <- 0 until size) {
      //count the number of b0's
      if (forwardSample(r, Fixed.Unset, Fixed.Unset, Fixed.Unset, Fixed.Unset)(1) equals Fixed.F) {
        bFalseCount += 1
      }
    }
    println("P(B) => P(b0) = %f, P(b1) = %f" format(bFalseCount.toDouble / size, 1 - bFalseCount.toDouble / size))

    //count the number of b0 and a0
    bFalseCount = 0
    for (val i <- 0 until size) {
      if (forwardSample(r, Fixed.F, Fixed.Unset, Fixed.Unset, Fixed.Unset)(1) equals Fixed.F) {
        bFalseCount += 1

      }
    }

    println()
    println("P(B|a0) => P(b0|a0) = %f, P(b1|a0) = %f" format(bFalseCount.toDouble / size, 1 - bFalseCount.toDouble / size))

    //count the number of b0 and d0
    bFalseCount = 0
    for (val i <- 0 until size) {
      if (forwardSample(r, Fixed.Unset, Fixed.Unset, Fixed.Unset, Fixed.F)(1) equals Fixed.F) {
        bFalseCount += 1

      }
    }
    println()
    println("P(B|d0) => P(b0|d0) = %f, P(b1|d0) = %f" format(bFalseCount.toDouble / size, 1 - bFalseCount.toDouble / size))
  }

  /**
   * Take a forward sample
   */
  def forwardSample(r: Random, aVal: Fixed.FV, bVal: Fixed.FV, cVal: Fixed.FV, dVal: Fixed.FV): List[Fixed.FV] = {

    var a = aVal
    var b = bVal
    var c = cVal
    var d = dVal

    //order is a,b,c,d

    //a0 == .7  a1 == .3
    if (a == Fixed.Unset) {
      if (r.nextDouble() < .7) {
        a = Fixed.F
      } else {
        a = Fixed.T
      }
    }


    if (b == Fixed.Unset) {
      val brand = r.nextDouble()
      if (a == Fixed.F) {
        if (brand < .8) {
          b = Fixed.F
        } else {
          b = Fixed.T
        }
      } else {
        if (brand < .3) {
          b = Fixed.F
        } else {
          b = Fixed.T
        }
      }
    }

    if (c == Fixed.Unset) {
      val crand = r.nextDouble()
      if (b == Fixed.F) {
        if (crand < .75) {
          c = Fixed.F
        } else {
          c = Fixed.T
        }
      } else {
        if (crand < .1) {
          c = Fixed.F
        } else {
          c = Fixed.T
        }
      }
    }

    if (d == Fixed.Unset) {
      val drand = r.nextDouble()
      if (b == Fixed.F) {
        if (drand < .1) {
          d = Fixed.F
        } else {
          d = Fixed.T
        }
      } else {
        if (drand < .6) {
          d = Fixed.F
        } else {
          d = Fixed.T
        }
      }
    }
    return List(a, b, c, d)
  }
}