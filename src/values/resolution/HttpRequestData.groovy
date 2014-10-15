package values.resolution

// NO FUNCIONA BIEN COMO INDICE DE UN MAP, dice que 2 objetos distintos son iguales...
//import groovy.transform.EqualsAndHashCode

// http://docs.codehaus.org/display/GROOVY/Groovy+1.8+release+notes#Groovy18releasenotes-EqualsAndHashCode
//@EqualsAndHashCode
class HttpRequestData {

   private String url
   private Map params = [:] // ordena por clave = nombre del param
   
   public HttpRequestData(String url, Map params)
   {
      this.url = url
      this.params = params.sort {a,b -> a.key <=> b.key} // ordenados por nombre
   }
   
   // no se puede modificar
   public void setParams(Map params)
   {
   }
   public void setUrl(String url)
   {
   }
   
   // Uso esto como key en el cache
   public String toString()
   {
      def sparams = ""
      this.params.each { key, value ->
         sparams += key + "=" + value
      }
      return this.url + "?" + sparams
   }
}