package main.scala

import htsjdk.samtools.fastq.FastqRecord
import transforms.{ReadPosition, TransforedReadAndDimension}
import transforms.ReadPosition.ReadPosition

import scala.collection.mutable

case class ReadContainer(read1: FastqRecord, read2: Option[FastqRecord], index1: Option[FastqRecord], index2: Option[FastqRecord]) {

  // a container to map additional metadata to the read set
  val metaData = new mutable.HashMap[String,String]()
}

object ReadContainer {
  /**
    * copy metadata over from one read container to another
    * @param in the input read container with the annotations of interest
    * @param out the output read container to add to
    * @return the modified read container
    */
  def copyMetaData(in: ReadContainer, out: ReadContainer): ReadContainer = {
    in.metaData.foreach{case(k,v) => out.metaData(k) = v}
    out
  }

  /**
    * extract a subsequence out of the specified read
    * @param toSlice the fastq record to extract from
    * @param start the starting position
    * @param length the length of the sequence to slice out
    * @param keepSequence should we keep the sequence within the read
    * @return a tuple of fastqrecord, barcode string, and barcode qual
    */
  def sliceFromFastq(toSlice: FastqRecord, start: Int, length: Int, keepSequence: Boolean): Tuple3[FastqRecord,String,String] = {
    val barcode = toSlice.getReadString.slice(start,start + length)
    val barcodeRemaining = toSlice.getReadString.slice(0, start) +
      toSlice.getReadString.slice(start + length,toSlice.getReadString.size)

    val bcQuals = toSlice.getBaseQualityString.slice(start,start + length)
    val bcQualRemaining = toSlice.getBaseQualityString.slice(0, start) +
      toSlice.getBaseQualityString.slice(start + length,toSlice.getReadString.size)


    val replacement =
      if (!keepSequence)
        new FastqRecord(toSlice.getReadHeader,barcodeRemaining,toSlice.getBaseQualityHeader, bcQualRemaining)
      else
        toSlice

    (replacement,barcode,bcQuals)
  }

  /**
    * extract out a sequence from a collection of reads
    * @param read the container to slice from
    * @param start the starting position
    * @param length how long of a sequence to slice out
    * @param name the annotation name for storing the extracted sequence in the metadata storage
    * @param readDim which of the reads to extract
    * @param keepSequence do we keep the sequence within the read
    */
  def sliceAndAnnototate(read: ReadContainer, start: Int, length: Int, name: String, readDim: ReadPosition, keepSequence: Boolean): TransforedReadAndDimension = {
    // we strip out the index from the read and it's quality score
    val toSlice = readDim match {
      case ReadPosition.Read1 =>  ReadContainer.sliceFromFastq(read.read1, start, length, !keepSequence)
      case ReadPosition.Read2 =>  ReadContainer.sliceFromFastq(read.read2.get, start, length, !keepSequence)
      case ReadPosition.Index1 => ReadContainer.sliceFromFastq(read.index1.get, start, length, !keepSequence)
      case ReadPosition.Index2 => ReadContainer.sliceFromFastq(read.index2.get, start, length, !keepSequence)
    }

    read.metaData(name) = "=" + toSlice._2 + "," + toSlice._3

    val newRead = readDim match {
      case ReadPosition.Read1 => ReadContainer(toSlice._1,read.read2,read.index1,read.index2)
      case ReadPosition.Read2 =>  ReadContainer(read.read1,Some(toSlice._1),read.index1,read.index2)
      case ReadPosition.Index1 => ReadContainer(read.read1,read.read2,Some(toSlice._1),read.index2)
      case ReadPosition.Index2 => ReadContainer(read.read1,read.read2,read.index1,Some(toSlice._1))
    }

    ReadContainer.copyMetaData(read,newRead)
    TransforedReadAndDimension(newRead,toSlice._2)
  }
}