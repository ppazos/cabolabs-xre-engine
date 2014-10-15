package com.cabolabs.xre.engine.loader

/**
 * Esta clase se utiliza para cargar la definicion de una regla
 * desde su ubicacion, para luego pasarle el contenido del archivo
 * al parser.
 * 
 * @author Pablo Pazos Gutierrez <pablo.pazos@cabolabs.com>
 *
 */
class RuleDefinitionLoader {

   // TODO: podria haber un metodo para cargar reglas desde una base de datos
   //       ¿que clase se usa para representar una conexion a DB?
   //       ¿o le paso el conextion string como location? deberia incluir la db y la tabla...
   
   /* 
   String loadRule(String id, String location)
   {
      
   }
   */
   
   String loadRule(URI location)
   {
      // http://weblogs.java.net/blog/kohsuke/archive/2007/04/how_to_convert.html
      File f
      try
      {
        f = new File(url.toURI())
      }
      catch(URISyntaxException e)
      {
        f = new File(url.getPath())
      }
      
      return loadRule(f)
   }
   
   String loadRule(File location)
   {
      return location.getText("UTF-8")
   }
}