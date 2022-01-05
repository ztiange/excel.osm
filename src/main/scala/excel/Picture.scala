package osm.tuplelike.excel

import cats._
import cats.instances._

import org.apache.poi.hssf.usermodel.HSSFPicture
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.hssf.usermodel.HSSFPatriarch
import collection.JavaConverters._
import org.apache.poi.hssf.usermodel.HSSFClientAnchor

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
      if (anchor.getCol2 == col && anchor.getRow2 == row)
        return Some(shape.asInstanceOf[HSSFPicture])
    }
  }
  return None
}
