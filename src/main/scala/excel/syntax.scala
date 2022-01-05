package osm.tuplelike.excel

import collection.JavaConverters._
import osm.tuplelike._
import cats.data._
import cats.implicits._
import org.apache.poi.ss.usermodel.*
import scala.compiletime.{summonInline}
import java.io.InputStream
import scala.deriving.Mirror

inline def RowCodec[T] = summonInline[osm.tuplelike.Codec[T,Row]]

given rowToTupleLike:Conversion[Row, TupleLike[Cell]] with
  def apply(r: Row): TupleLike[Cell] = new TupleLike[Cell]() {
    def length(): Int = r.cellIterator.asScala.length
    def components(): Seq[Cell] = r.cellIterator.asScala.toSeq

  }

extension (s: Sheet)

  def rows = s.rowIterator.asScala.toSeq

  /**
   * 
   */
  inline def decode[T <: Product: scala.deriving.Mirror.ProductOf](
      dropTitle: Int = 1
  ):Validated[NonEmptyList[(Int, String)], Seq[T]] =
    lazy val codec = summonInline[osm.tuplelike.Codec[T,Row]]
    lazy val rows = s.rows.iterator.drop(dropTitle).toSeq
    rows.mapWithIndex(
      (row,index) => codec.decode(row)
        .leftMap(msgs=>msgs.map(msg=>(index+dropTitle,msg)))
    ).sequence

extension (r: Row)
  def cells = r.cellIterator.asScala.toSeq

  inline def decodeAs[T : Mirror.ProductOf] = {
    RowCodec[T].decode(r)
  }

  inline def encodeFrom[T:Mirror.ProductOf](v:T) : Row = {
    RowCodec[T].encode(v)(r)
  }

  def copy(newRow: Row) = {
    // Get the source / new row
    val sourceCells = r.cells;
    sourceCells.zipWithIndex.map((c, i) => {
      val newCell = newRow.createCell(i)
      val newCellStyle = r.getSheet.getWorkbook.createCellStyle()
      newCellStyle.cloneStyleFrom(c.getCellStyle)
      newCell.setCellStyle(newCellStyle)
      c.getCellType match {
        case CellType._NONE   =>
        case CellType.BLANK   => newCell.setCellValue(c.getStringCellValue)
        case CellType.BOOLEAN => newCell.setCellValue(c.getBooleanCellValue)
        case CellType.ERROR   => newCell.setCellValue(c.getErrorCellValue)
        case CellType.FORMULA => newCell.setCellFormula(c.getCellFormula)
        case CellType.NUMERIC => newCell.setCellValue(c.getNumericCellValue)
        case CellType.STRING  => newCell.setCellValue(c.getStringCellValue)
      }
    })
  }

extension (filePathName: String)
  def openAsWorkbook(clazz: Class[?]) =
    // val filePath = Paths.get(clazz.getResource(filePathName).toURI)
    val is = clazz.getResourceAsStream(filePathName)
    WorkbookFactory.create(is)

extension (is : InputStream)
  def openAsWorkbook() =  WorkbookFactory.create(is)