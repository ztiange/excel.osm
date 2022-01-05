package osm.tuplelike.test
import org.junit.Test
import org.junit.Assert.*
import java.nio.file.Files
import java.nio.file.Paths
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.Sheet
import java.util.Date
import java.text.SimpleDateFormat
import cats.data.Validated.Valid

//TODO
import osm.tuplelike.excel.{given}
import osm.tuplelike.text.{given}
import osm.tuplelike.types.{given,_}
import osm.tuplelike.types.DefaultValueFormats.{given}

import osm.tuplelike.excel._


import org.apache.poi.ss.usermodel.Cell
class ExcelTest{

  def getSheet(filePathName: String, sheetIndex: Int = 0): Sheet = {
    val filePath = Paths.get(classOf[ExcelTest].getResource(filePathName).toURI)
    val is = Files.newInputStream(filePath)
    WorkbookFactory.create(is).getSheetAt(sheetIndex)
  }


  @Test def decodeSimpleTypes(): Unit = {
    
    case class C(
        数值_浮点数_ : Double,
        数值_整数_ : Int,
        数值_字符串_ : Int,
        数值_空白_ : Option[Int],
        日期: Date,
        日期_字符串_ : Date,
        日期_空白_ : Option[Date],
        链接: Link,
        公式链接: Link,
        链接_空白_ : Option[Link],
        字符串_空白_ : Option[String],
        字符串: String
    )
    val firstRow = getSheet("/test.xls", 1).getRow(1)
    val record = firstRow.decodeAs[C]
    val D1 = summon[DateFormat].parse("2000-01-01")
    val D2 = summon[DateFormat].parse("2001-01-01")
    assertEquals(
      Valid(
        C(
          1.0,
          2,
          3,
          None,
          D1,
          D2,
          None,
          Link("http://www.baidu.com", "普通链接"),
          Link("http://www.baidu.com", "公式链接"),
          None,
          None,
          "字符串"
        )
      ),
      record
    )
  }

  @Test def rowEncodeDecodeTest(): Unit = {
    case class C(name: String, value: Int)
    val c = C("1221",1)

    val sheet = getSheet("/test.xls", 1)
    val targetRow = sheet.createRow(3)

    targetRow.encodeFrom(c)
    assertEquals(targetRow.decodeAs[C],Valid(C("1221",1)))
  }  
}