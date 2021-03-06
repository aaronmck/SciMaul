package transforms

import main.scala.ReadContainer
import recipe.sequence.Sequence
import recipe.{Dimension, ResolvedDimension}
import transforms.ReadPosition.ReadPosition

/**
  * transform a read through the constraints, producing a filtered read
  */
trait ReadTransform {
  def name: String
  def description: String
  def transform(reads: ReadContainer): String
  def dimension: Option[ResolvedDimension]
  def isDimensioned: Boolean
}
