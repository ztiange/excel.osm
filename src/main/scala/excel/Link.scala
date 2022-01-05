package osm.tuplelike.excel
import cats.data._
import cats.implicits._
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType

case class Link(href: String, label: String)

given Codec[Link] with {
  def encode(v: Link) = ???
  def decode(cell: Cell) = {
    if (cell.getCellType == CellType.FORMULA) {
      cell.getCellFormula match
        case s"""HYPERLINK("${href}","${label}")""" => Link(href, label).valid
        case _ => NonEmptyList.one("Invalid Link").invalid
    } else if (cell.getHyperlink != null) {
      Link(cell.getHyperlink.getAddress, cell.getHyperlink.getLabel).valid
    } else
      NonEmptyList
        .one(s"Invalid Link for cell(${cell.getColumnIndex})")
        .invalid
  }
}
