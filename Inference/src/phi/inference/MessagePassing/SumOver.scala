package phi.inference.MessagePassing

import phi.inference.MessagePassing.Cliques.{CliqueEdge, Clique}
import collection.mutable.{HashSet, ListBuffer, HashMap}
import phi.inference.Graph


/**
 * Created by IntelliJ IDEA.
 * User: phi
 * Date: 11/11/11
 * Time: 3:03 PM
 * To change this template use File | Settings | File Templates.
 */

object SumOver {
  def main(args: Array[String]) {
    // test some things about Factors and summing over variables
    val f = (a: Fact) => {
      println(a.sumOver("a"))
      println(a.sumOver("a0"))
      println(a.sumOver("a1"))
      println(a.sumOver("b0"))
      println(a.sumOver("b1"))
      println(a.sumOver("b"))
      println(a.sumOver("c"))
      println()
    }
    val a = Fact(List(HashMap("a0" -> .7, "a1" -> .3)))
    f(a)
    val b = Fact(List(HashMap("a0b0" -> .8, "a0b1" -> .2, "a1b0" -> .3, "a1b1" -> .7)))
    f(b)
    val ab = Fact(List(HashMap("a0" -> .7, "a1" -> .3), HashMap("a0b0" -> .8, "a0b1" -> .2, "a1b0" -> .3, "a1b1" -> .7)))
    f(ab)
  }


  /**
   * Run the BU message initalization
   */
  def SimulateBUMessage(cliqueTree: Graph[Clique, CliqueEdge]) {
    //BU message loop this is Initialize-CTree and BU-Message combined
    //Initialized CTree is passed in

    //while we have a clique that gets an updated message continue the BU init
    while (cliqueTree.nodes.filter((x: Clique) => x.updated).size > 0) {

      //loop over all edges and send a message from src to dst
      // keep in mind this is undirected so src->dst and dst->src are in the graph
      cliqueTree.edges.foreach((edge) => {

        //find the difference between the source and dst
        //  send the items but sum over Factor(Src) - Factor(Dst)

        var factorsToSend = edge._1.workingFactors.values.toList

        val dstVariables = edge._2.getFactorNames

        //sum over the variables that src has but dst does not
        edge._1.getFactorNames.filter((s: String) => !dstVariables.contains(s)).foreach((variable) => {
          factorsToSend = factorsToSend.map((f) => f.sumOver(variable))
        })


        edge._2.updated = false

        //divide by sepset

        //remove the factors that have already been sent across the edge
        factorsToSend = factorsToSend.filter((sendFact) => {
          !edge._3.sepset.contains(sendFact.originalFactors)
        })



        //get the workingSize of dst to see if it grows in size
        val workingSize = edge._2.workingFactors.size

        //add
        factorsToSend.foreach((fts: Fact) => edge._2.workingFactors.put(fts.originalFactors, fts))

        edge._3.sepset = new HashMap[String, Fact]
        factorsToSend.foreach((x) => edge._3.sepset.put(x.originalFactors, x))

        edge._2.updated = workingSize < edge._2.workingFactors.size
      })
    }
  }


  case class Fact(val distribs: List[HashMap[String, Double]]) {

    /**
     * On initialization combine distribs by joining on common terms
     */
    val productDistribution: HashMap[String, Double] = {
      val dists = new ListBuffer[HashMap[String, Double]]
      dists.appendAll(distribs)

      while (dists.size > 1) {
        var combined = new HashMap[String, Double]
        //for each dist in 0 that contains factorName multiply it by all distributions in dist1 that has factorName
        dists(0).foreach(prob0 => {
          dists(1).foreach(prob1 => {
            if (prob1._1.contains(prob0._1) || prob0._1.contains(prob1._1)) {
              var key = ""
              if (prob1._1.size > prob0._1.size) {
                key = prob1._1
              } else {

                key = prob0._1
              }
              combined.put(key, prob0._2 * prob1._2)
            }
          })
        })
          dists.remove(0)
          dists.remove(0)
          dists.append(combined)
      }
      dists(0)
    }

    /**
     * get the unique key for this Factor
     */
    var originalFactors = distribs.map(x => x.keySet.toList.sortWith((a, b) => a.compare(b) > 0).mkString("[", ",", "]")).mkString(",")


    /**
     * Get all variables that contribute to this factor
     */
    def getFactorNames: HashSet[String] = {
      val ret = new HashSet[String]

      distribs.foreach(dist => dist.foreach((x) => {
        x._1.replace("0", "").replace("1", "").foreach((c) => ret.add(c.toString))
      }))
      return ret
    }


    /**
     * Remove @sumfactor from @factor
     */
    def replaceFactor(factor: String, sumfactor: String) =
      factor.replace(sumfactor + "0", "").replace(sumfactor + "1", "").replace(sumfactor, "")


    /**
     * Sum over factor for all distributioins in this factor returning a new factor representing this sum
     */
    def sumOver(factor: String): Fact = {

      var factorSum = new HashMap[String, Double]

      //sum over @factor
      productDistribution.foreach(f => {
          if (f._1.contains(factor)) {

            //remove factor from f._1
            var str = replaceFactor(f._1, factor)
            //sum over the remains from f._1 - factor
            factorSum.put(str, factorSum.getOrElse(str, 0.0) + f._2)
          } else {
            //if f._1 has no common variables with factor then add it to the distribution to be retured
            if (!f._1.contains(factor.replace("0", "").replace("1", ""))) {
              factorSum.put(f._1, f._2)
            }
          }
        })


      //if we have a situation where the factor we summed over was the only factor in the distribution
      //  then scale by that amount
      //  IE distribution = {a0=>.1 , a1=>.9,  a0b0 => .5,  a1b0 => .5,  a0b1 => .5 a1b1=> .5} and summing over a
      //   we get:    {a0b0 => .05,  a1b0 => .95,  a0b1 => .05 a1b1=> .95}
      if (factorSum.contains("") && factorSum.size > 1) {
        val mult = factorSum("")
        factorSum.remove("")
        factorSum= factorSum.map((x) => x._1 -> x._2 * mult)
      }

      val f = new Fact(List(factorSum))
      f.originalFactors = this.originalFactors
      return f
    }
  }

}
