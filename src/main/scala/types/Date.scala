package osm.tuplelike.types
import java.util.Date
import java.text.SimpleDateFormat

opaque type DateFormat = String

object DateFormat:
  def apply(format: String): DateFormat = format
  extension (format: DateFormat) 
      def format(v: Date | DateTime) = 
          new SimpleDateFormat(format).format(v)
      def parse(v: String) = 
          new SimpleDateFormat(format).parse(v)
