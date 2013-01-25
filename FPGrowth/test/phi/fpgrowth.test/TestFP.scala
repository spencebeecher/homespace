package phi.fpgrowth

import org.junit.Test

/**
 * Created by IntelliJ IDEA.
 * User: phi
 * Date: 9/16/11
 * Time: 11:12 PM
 * To change this template use File | Settings | File Templates.
 */

class FPTest{
  @Test def testPermute ={
    val perm = new Permutation[Int]()
    assert(perm.allPermutations((0 to 2).toArray,List(4,5,6)).size == 7)
  }
def stringToData(s:String) :List[List[Tuple2[String,Int]]]={
  return s.split(";").toList.map((y)=>y.split(",").toList.map((z)=>(z,1)))
}
@Test def testInit ={
  val tree = new FPTree()
  val data = stringToData("a,b,c;a,b,f;a,b,c,d,f;f,g,h;x,y,z;f,c,d,z")
  tree.init(data,1)
  println(tree)
 }

}