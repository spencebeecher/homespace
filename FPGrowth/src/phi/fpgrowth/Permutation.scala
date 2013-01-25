package phi.fpgrowth

/**
 * Created by IntelliJ IDEA.
 * User: phi
 * Date: 9/10/11
 * Time: 5:02 PM
 * To change this template use File | Settings | File Templates.
 */

class Permutation[T] {


  def createMaxMask(size:Int):Int={
    //need to return the max
    var ret = 1
    for(x <- 0 until size){
       ret = ret << 1
    }
    return ret
  }

  def createList(optional:Array[T],mask:Int) : List[T] = {
    var i : Int = 1
    var ret : List[T] = Nil
    for(x <- 0 until optional.size){
      if((i&mask)!=0){
        ret =  optional(x)::ret
      }
      i = i << 1
    }
    return ret
  }

//ripped from http://graphics.stanford.edu/~seander/bithacks.html
  def permuteBit(current: Int):Int ={
      val t = (current | (current - 1)) + 1
      return t | ((((t & -t) / (current & -current)) >> 1) - 1)

  }

  def allPermutations(optional:Array[T],required:List[T]) : List[List[T]]= allPermutationsLimit(optional,required ,optional.size+required.size)

  def allPermutationsLimit(optional:Array[T],required:List[T], limit:Int) : List[List[T]]= permuteAll(optional).map((x:List[T])=>x:::required).toList

  def permuteAll(optional:Array[T]) : List[List[T]]={
    var ret : List[List[T]] = Nil
    for(i <- 1 to optional.size) {
      ret = permute(optional,i):::ret
    }
    return ret
  }
  def permute(optional:Array[T],numItems:Int)  : List[List[T]] = {
    if(numItems > optional.size || numItems > 31 || numItems < 1){
      return Nil
    }
    val maxMask = createMaxMask(optional.size)
    var ret = List[List[T]]()

    var twiddle = 0
    for(i <- 0 until numItems){
      twiddle += 1 << i
    }
    while(twiddle < maxMask){
      ret = createList(optional,twiddle)::ret
      twiddle = permuteBit(twiddle)

    }


    return ret
  }
}
