package com.cabolabs.xre.engine.resolution

class Aggregator {

   /**
    * Obtener ciertos valores de una lista de valores
    */
   final static String FIRST = "first"
   final static String LAST = "last"
   final static String GET = "get" // obtiene en un indice
   
   /**
    * Agregadores de listas de numeros
    */
   final static String SUM = "sum"
   final static String AVG = "avg"
   
   /**
    * Cantidad de elementos de la coleccion
    */
   final static String COUNT = "count"
   
   
   /**
    * Agrega los valores en la lista de items, obteniendo un elemento simple.
    * Si no se especifica el agregador, utiliza FIRST por defecto.
    * 
    * @param agg
    * @param items
    * @param params
    * @return
    */
   static public Object aggregate(String agg, List items, Map params = [:])
   {
      println " Aggregate $agg " + items
      switch (agg)
      {
         case FIRST:
           return items.get(0)
         case LAST:
           //println "aggregator last"
           return items.get(items.size()-1)
         case GET:
           // TODO: asegurar que viene i
           return items.get(params['i'])
         case SUM:
           // TODO: asegurar que son todos numeros
           // Probar lo que hace Groovy cuando no son numeros...
           return items.sum()
         case AVG:
           // TODO: asegurar que son todos numeros
           // Probar lo que hace Groovy cuando no son numeros...
           return items.sum() / items.size()
         case COUNT:
           return items.size()
         default: // Si no se especifica el agregador, usa FIRST por defecto.
           println "aggregator default first"
           return items.get(0)
      }   
   }
   
}