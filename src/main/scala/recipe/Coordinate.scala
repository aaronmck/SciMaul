package recipe

import recipe.sequence.Sequence

/**
  * a matched set of dimensions and coordinates
 *
  * @param dimensions the dimensions to map the coordinates into
  * @param coordinates the coordinates
  */
case class Coordinate(dimensions: Array[ResolvedDimension], coordinates: Array[Sequence]) {

  def coordinateString(): String = coordinates.map{c => c.name}.mkString(Coordinate.seperator)
}

object Coordinate {
  val seperator = "."
}