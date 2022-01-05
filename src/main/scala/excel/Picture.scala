package osm.tuplelike.excel

import cats._
import cats.implicits._
import cats.data._

import org.apache.poi.hssf.usermodel.HSSFPicture
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.hssf.usermodel.HSSFPatriarch
import collection.JavaConverters._
import org.apache.poi.hssf.usermodel.HSSFClientAnchor


/**
 * pictures in excel are decorded as byte array
 */
case class Picture(data: Array[Byte])
  
def getPicture(cell: Cell): Option[HSSFPicture] = {
  val col = cell.getColumnIndex
  val row = cell.getRow.getRowNum
  val sheet = cell.getRow.getSheet
  return getPicture(col, row, sheet)
}

def getPicture(col: Int, row: Int, sheet: Sheet): Option[HSSFPicture] = {
  val patriarch = sheet.getDrawingPatriarch().asInstanceOf[HSSFPatriarch]
  val shapeList = patriarch.getChildren().asScala;
  for (shape <- shapeList) {
    if (shape.isInstanceOf[HSSFPicture]) {
      val anchor = shape.getAnchor.asInstanceOf[HSSFClientAnchor]
      if (anchor.getCol1 == col && anchor.getRow1 == row)
        return Some(shape.asInstanceOf[HSSFPicture])
    }
  }
  return None
}


given Codec[Picture] with{
  def encode(v: Picture) = ???
  def decode(c: Cell) = 
    getPicture(c)
      .map(p => Picture(p.getPictureData.getData))
      .toValidNel[String](s"${c.getColumnIndex}:Not a valid picture")
}