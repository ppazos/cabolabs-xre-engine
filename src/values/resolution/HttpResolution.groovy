package values.resolution

import groovyx.net.http.*
import parser.Caster

// FIXME: deberia haber una interfaz IResolution con getType y getValue
//        que esta clase implemente.
/*
 * // Luego podrian haber otros metodos de resolucion del valor de la variable, asi que estos campos podrian estar por fuera de esta clase.
   // Ejemplo de extractor para JSON: http://goessner.net/articles/JsonPath/
   // http://code.google.com/p/jsonpath/
   // http://code.google.com/p/json-path/
   // http://jsonpath.curiousconcept.com/
 */
// TODO: hay que tomar una decision para saber que hacer cuando el request falla.
class HttpResolution {

   String type = "http"
   
   // Informacion para el request
   String locator // URL
   //Map params = [:]
   List params = [] // List<Variable>: se declara en el resolutor, se resuelven por Rule al evaluar
   
   // XML obtenido del request
   //String xml // se guarda en el cache
   
   // Para procesar el xml
   //  http://www.ericonjava.com/?tag=groovy-xpath-gpath
   //  http://groovy.codehaus.org/GPath
   //  http://docs.codehaus.org/display/GROOVY/Reading+XML+with+Groovy+and+XPath
   //  http://stackoverflow.com/questions/2268006/how-do-i-create-an-xpath-function-in-groovy
   String extractor // XML gpath
   
   String sort // previo al aggregator cuando es una coleccion [asc, desc]
   String aggregator // TODO: ENuM
   
   
   /**
    * 
    * @return en realidad es un string pero lo dejo como object para poder implementar otros resolvedores que devuelvan otros tipos.
    */
   public Object getValue()
   {
      // ===============================
      // Creo el query map para el request
      def queryMap = [:]
      this.params.each { var ->
         queryMap[var.name] = var.value
      }
      //println "queryMap: " + queryMap
      // ===============================
      
      //println "http resultor locator: " + this.locator + " " + queryMap
      
      // ===============================
      // Verificacion de si esta en cache para no tener que hacer el mismo request 2 veces
      def cache = HttpCache.getInstance()
      def reqData = new HttpRequestData(this.locator, queryMap)
      def res = cache.get(reqData.toString())

      //println "cache key: " + reqData.toString()
      
      if (res)
      {
         println "CACHE HIT"
         return getValueFromXML(res)
      }
      
      println "CACHE MISS"
      
      
      /*
      println "resolution from: " + this.locator + " curr thrd id: " + Thread.currentThread().getId()
      println "params: " + this.params
      this.params.each { entry -> println entry.value.class } // Son todos strings
      */
      
      
      
      // FIXME
      // Si aca tira una except nunca se agarra porque
      // esta dentro del thread que se crea en Rule.resolveVars
      // Es necesario buscar una forma de notificar la except
      // al thread padre.
      //
      // Ya puse un exception handler en Rule.
      
      // http://coderberry.me/blog/2012/05/07/stupid-simple-post-slash-get-with-groovy-httpbuilder/
      def http = new HTTPBuilder(this.locator)

      // perform a GET request, expecting TEXT response data
      http.request( Method.GET, ContentType.XML ) {
        
        //uri.path = '/ajax/services/search/web'
        uri.query = queryMap // [ v:'1.0', q: 'Calvin and Hobbes' ]
      
        headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'
      
        // response handler for a success response code:
        response.success = { resp, xml ->
        
           // xml es groovy.util.slurpersupport.NodeChild
           
           //println 'XML: ' + xml.name()
           
           //println 'extracted value: '
           //println xml.name() // list o statuses segun el http://api.twitter.com/1/ppazos/lists/openehr.xml
           //println xml.user.name.text() // Pablo Pazos para 'list'
           
           // No funciona
           //println xml."${xpath}"
           //println xml."$xpath".text() // ver si el .text() es necesario
           
           // No se puede obtener el valor si la path tiene mas de un elemento,
           // se tiene que ir navegando de a uno.
           // http://wittykeegan.blogspot.com/2009/09/not-so-answered-variables-in.html
           
           
           /*
           def node = xml
           
           // Extrae valor navegando por el XML si hay extractor definido en el XRL
           if (gpath) // Evita error cuando el XML es un valor simple: <xml>valor</xml>
           {
              gpath.split("\\.").each { pi ->
                 //println "path item: " + pi
                 node = node."$pi"
              }
           }
           
           print " - value: "
           print "extracted(" + node.text() + ") "
           
           def prevalue
           
           // Usar el agregador
           // TODO: verificar si setearon agregador, sino, usar FIRST por defecto.
           if (node.size() > 1)
           {
              prevalue = Aggregator.aggregate(this.aggregator, node.collect { it.text() }) // La lista de valores son nodos XML, quiero los valores simples.
           }
           else
           {
              prevalue = node.text()
           }
           
           print "prevalue(" + prevalue + ")"
           
           return prevalue
           */
           
           // ===============================
           // Agrega a cache
           cache.add(reqData.toString(), xml)
           // ===============================
           
           
           // extrae y agrega
           return getValueFromXML(xml)
           
           /* se castea afuera en Rule.resolveVars()
            * 
           // Castea segun el tipo de la variable,
           // el problema es que el valor de comparacion, si es constante,
           // tambien se va a cargar desde un XML (la definicion de la unit)
           // entonces sera de tipo String, tal vez comparando todo como string
           // es mas facil que estar transformando todo al tipo correcto.
           Object castedValue = Caster.cast(prevalue, this.type) // Variable.type
           
           println " - casted value: " + castedValue.class.simpleName
           
           return castedValue
           */
        }
      
        // handler for any failure status code:
        response.failure = { resp ->
           
           println "response: " + resp.getAllHeaders() + " curr thrd id: " + Thread.currentThread().getId()
           
           //println "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
           throw new Exception("Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}")
        }
      }
   }
   
   // groovy.util.slurpersupport.NodeChild o
   // groovy.util.slurpersupport.NodeChildren (no parece haber doc de esta clase)
   //private Object getValueFromXML(groovy.util.slurpersupport.NodeChild xml)
   private Object getValueFromXML(Object xml)
   {
      //println "resolutor get value from XML"
      
      // Extrae valor navegando por el XML si hay extractor definido en el XRL
      if (this.extractor) // Evita error cuando el XML es un valor simple: <xml>valor</xml>
      {
         this.extractor.split("\\.").each { pi ->
            //println "path item: " + pi
            xml = xml."$pi"
         }
      }
      
      print " - value: extracted(" + xml.text() + ") "
      
      def prevalue
      
      // Usar el agregador
      // TODO: verificar si setearon agregador, sino, usar FIRST por defecto.
      
      // FIXME: agrgar solo si el tipo de variable NO es collection
      if (xml.size() > 1)
      {
         prevalue = xml.collect { it.text() } // lista de valores como string
         // Ahora el aggregator lo esta haciendo la Rule
         //prevalue = Aggregator.aggregate(this.aggregator, xml.collect { it.text() }) // La lista de valores son nodos XML, quiero los valores simples.
      }
      else
      {
         prevalue = xml.text()
      }
      
      print "prevalue(" + prevalue + ")"
      
      return prevalue
   }
}