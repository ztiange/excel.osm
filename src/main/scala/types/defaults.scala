package osm.tuplelike.types

object DefaultValueFormats:
  given dateFormat: DateFormat = 
    DateFormat("yyyy-MM-dd")
  given dateTimeFormat: DateTimeFormat = 
    DateTimeFormat("yyyy-MM-dd hh:mm:ss")
