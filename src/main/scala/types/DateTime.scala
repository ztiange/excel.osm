package osm.tuplelike.types
import java.util.Date
import java.text.SimpleDateFormat

opaque type DateTimeFormat = String
object DateTimeFormat:
  def apply(format: String): DateTimeFormat = format
  extension (format: DateTimeFormat) 
      def format(v: Date | DateTime) = new SimpleDateFormat(format).format(v)
      def parse(v: String) = new SimpleDateFormat(format).parse(v)

opaque type DateTime = Date
object DateTime:
  def apply(value: Date): DateTime = value