package recipe

import java.io.File

import barcodes.FastBarcode
import org.json4s._
import org.json4s.native.JsonMethods._
import recipe.sequence.Sequence

import scala.collection.mutable
import scala.io.Source

class RecipeContainer(input: String) {

  // parse a default format
  implicit val formats = DefaultFormats

  // read in the input file as a string
  val inputFile = Source.fromFile(input).getLines().mkString("\n")

  // and parse it into a recipe object
  val recipe = parse(inputFile).extract[Recipe]

  // for each of the barcodes, check if it has a list of sequences that we need to load. We use the
  // path of the recipe as the base for the sequence files if it's a relative path
  val basePath = (new File(input)).getParent

  // convert the dimensions into resolved dimensions
  // .filter(p => p.sequences.isDefined)
  val resolvedDimensions = recipe.barcodes.map { bc => {
    val barcodeArray = if (bc.sequences.get startsWith File.separator)
      (bc.name, RecipeContainer.toSequenceArray(bc.sequences.get))
    else
      (bc.name, RecipeContainer.toSequenceArray(basePath + File.separator + bc.sequences.get))

    ResolvedDimension(bc, barcodeArray._2)
  }
  }
}

object RecipeContainer {

  // convert a file containing barcodes into a list of sequences
  def toSequenceArray(file: String): Array[Sequence] = {
    Source.fromFile(file).getLines().map { line =>
      Sequence(
        line.split("\t")(0),
        line.split("\t")(1),
        FastBarcode.toFastBarcode(line.split("\t")(1)))
    }.toArray
  }
}





