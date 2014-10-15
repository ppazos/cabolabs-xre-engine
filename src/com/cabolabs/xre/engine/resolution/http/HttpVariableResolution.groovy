package com.cabolabs.xre.engine.resolution.http

import com.cabolabs.xre.core.definitions.ConstantValue
import com.cabolabs.xre.core.definitions.DataType;
import com.cabolabs.xre.core.definitions.VariableDeclaration
import com.cabolabs.xre.core.execution.RuleExecution
import com.cabolabs.xre.core.execution.VariableInstance;
import com.cabolabs.xre.core.resolution.VariableResolution
import com.cabolabs.xre.core.execution.Memory
import groovy.util.slurpersupport.GPathResult
import groovyx.net.http.*

import com.cabolabs.xre.engine.resolution.Aggregator;
import com.cabolabs.xre.engine.util.Caster

// FIXME: referencia al codigo viejo

/*
 * // Luego podrian haber otros metodos de resolucion del valor de la variable, asi que estos campos podrian estar por fuera de esta clase.
   // Ejemplo de extractor para JSON: http://goessner.net/articles/JsonPath/
   // http://code.google.com/p/jsonpath/
   // http://code.google.com/p/json-path/
   // http://jsonpath.curiousconcept.com/
 */
// TODO: hay que tomar una decision para saber que hacer cuando el request falla.
class HttpVariableResolution extends VariableResolution {

   // Informacion para el request
   String locator // URL

   // La key es el nombre del parametro que se va a usar en el request, y puede ser distinto al nombre de la variable declarada.
   Map<String, VariableDeclaration> params = [:]
   
   // Para procesar el xml
   //  http://www.ericonjava.com/?tag=groovy-xpath-gpath
   //  http://groovy.codehaus.org/GPath
   //  http://docs.codehaus.org/display/GROOVY/Reading+XML+with+Groovy+and+XPath
   //  http://stackoverflow.com/questions/2268006/how-do-i-create-an-xpath-function-in-groovy
   String extractor // XML gpath
   
   String sort // previo al aggregator cuando es una coleccion [asc, desc]
   String aggregator // TODO: enum de los strings declarados en Aggregator
   
   
   public HttpVariableResolution(VariableDeclaration declaration)
   {
      super(declaration)
   }
   
   // FIXME: los params de parametro de getValue no se usan, tengo los params adentro en
   //        this.params y hacen referencias a variables que deberian estar declaradas en
   //        la regla y con valores seteados en el momento de realizar esta resolucion.
   @Override
   public VariableInstance getValue(String sessionId, List<VariableInstance> params)
   {
      //RuleExecution re = Memory.getInstance().get(sessionId)
      
      // =========================================
      def start = System.currentTimeMillis()
      
      // query params
      Map<String, String> queryMap = createQueryMap(sessionId)
      
      println " + HTTP QUERY MAP: " + queryMap
      
      /////////////////////////////////////
      // TODO: soportar resultados JSON. //
      /////////////////////////////////////
      
      // el xml esta cacheado?
      GPathResult xml = cacheLookup(queryMap)
      
      if (!xml)
      {
         xml = httpQueryXML(queryMap)
         addToCache(xml, queryMap)
      }
      
      VariableInstance value = getValueFromXML(xml)
      
      // =========================================
      def now = System.currentTimeMillis()
      println " - Http Resolution getValue() execution time: " + (now - start) + "ms"
      
      return value
   }
   
   /**
    * Genera mapa de parametros para el request HTTP con valores string.
    * @return
    */
   private Map<String, String> createQueryMap(String sessionId)
   {
      println params
      
      Map<String, String> queryMap = [:]
      
      // Para pedir valores de los params
      RuleExecution re = Memory.getInstance().get(sessionId)
      
      VariableInstance param
      
      this.params.each { nameInstance ->
         
         // TODO: si el valor es una Date, habria que definir un formato para
         //       pasarle al request, porque el servicio HTTP podria esperar
         //       un formato diferente de fecha a la serializacion por defecto
         //       de Java.
         // TODO: si el valor es una Collection, el valor no deberia ser string,
         //       creo que sus valores si, o sea deberia ser Collection<String>
         //       y ver si el request ya toma el param multivaluado o hay que
         //       hacer algun otro procesamiento. Probar!
         //
         // El nombre del parametro para el request no tiene porque ser el nombre
         // de la variable declarada (nameInstance.value.declaration.name), es el
         // nombre que le asigna la regla al param del request.
         //
         // nameInstace.value es VariableDeclaration
         //
         
         if (nameInstance.value instanceof ConstantValue)
         {
            queryMap[nameInstance.key] = nameInstance.value.value.toString() // La constante tiene su valor, no se busca en la memoria
         }
         else
         {
            // re.getValue() es VariableInstance
            queryMap[nameInstance.key] = re.getValue(nameInstance.value.name).getValue().toString() // Si es una declaracion, se busca el valor en la memoria
         }
      }
      
      return queryMap
   }
   
   
   private GPathResult cacheLookup(Map queryMap)
   {
      def cache = HttpCache.getInstance()
      def reqData = new HttpRequestData(this.locator, queryMap)
      def res = cache.get(reqData.toString())
   }
   
   
   private void addToCache(GPathResult xml, Map queryMap)
   {
      def cache = HttpCache.getInstance()
      def reqData = new HttpRequestData(this.locator, queryMap)
      cache.add(reqData.toString(), xml)
   }
   
   
   private GPathResult httpQueryXML(Map queryMap)
   {
      // http://coderberry.me/blog/2012/05/07/stupid-simple-post-slash-get-with-groovy-httpbuilder/
      def http = new HTTPBuilder(this.locator)

      // TODO: soportar method POST si se especifica en la regla
      // perform a GET request, expecting TEXT response data
      http.request( Method.GET, ContentType.XML ) {
        
         //uri.path = '/ajax/services/search/web'
         uri.query = queryMap // [ v:'1.0', q: 'Calvin and Hobbes' ]
      
         headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'
      
         // response handler for a success response code:
         response.success = { resp, xml ->
           
            return xml
         }
      
         // handler for any failure status code:
         response.failure = { resp ->
           
            // TODO: soportar retries si falla el request por timeout
            
            println "response: " + resp.getAllHeaders() + " curr thrd id: " + Thread.currentThread().getId()
           
            //println "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
            throw new Exception("Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}")
         }
      }
   }
   
   
   /**
    * 
    * @return en realidad es un string pero lo dejo como object para poder implementar otros resolvedores que devuelvan otros tipos.
    */
   /*
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
      
      
      //println "resolution from: " + this.locator + " curr thrd id: " + Thread.currentThread().getId()
      //println "params: " + this.params
      //this.params.each { entry -> println entry.value.class } // Son todos strings
      

      // http://coderberry.me/blog/2012/05/07/stupid-simple-post-slash-get-with-groovy-httpbuilder/
      def http = new HTTPBuilder(this.locator)

      // perform a GET request, expecting TEXT response data
      http.request( Method.GET, ContentType.XML ) {
        
        //uri.path = '/ajax/services/search/web'
        uri.query = queryMap // [ v:'1.0', q: 'Calvin and Hobbes' ]
      
        headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'
      
        // response handler for a success response code:
        response.success = { resp, xml ->
        
           // ===============================
           // Agrega a cache
           cache.add(reqData.toString(), xml)
           // ===============================
           
           // extrae y agrega
           return getValueFromXML(xml)
        }
      
        // handler for any failure status code:
        response.failure = { resp ->
           
           println "response: " + resp.getAllHeaders() + " curr thrd id: " + Thread.currentThread().getId()
           
           //println "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
           throw new Exception("Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}")
        }
      }
   }
   */

   private Object getValueFromXML(GPathResult xml)
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
      
      print " - getValueFromXML: extracted(" + xml.text() + ")"
      
      // antes se llamaba prevalue porque luego se hacia la agregacion y el cast,
      // eso ahora no es necesario se puede hacer la agregacion aca.
      def prevalue
      
      // Usar el agregador
      // TODO: verificar si setearon agregador, sino, usar FIRST por defecto.
      
      //println "extracted size " + xml.size()
      
      // FIXME: agrgar solo si el tipo de variable NO es collection
      if (xml.size() > 1)
      {
         // Collection<String>
         prevalue = xml.collect { it.text() } // lista de valores como string
         
         // Si ya es collection, dejo los valores sin agregar
         // TODO: deberia castear sus valores internos al tipo correcto porque
         // son strings, pero se necesitan tipos COLLECTION_OF_X para castear a X.
         if (this.declaration.type != DataType.COLLECTION)
         {
            // Sort
            if (this.sort)
            {
               if (this.sort == 'asc')
               {
                  prevalue = prevalue.sort() // ordena de menor a mayor
               }
               else if (this.sort == 'desc')
               {
                  prevalue = prevalue.sort{ a, b -> (a < b) ? 1 : 0 }
               }
               // FIXME: otro caso deberia tirar error en parser
               
               print " sort: " + this.sort + " " + prevalue
            }
            
            // Aggregate
            // FIXME: el valor es siempre String, habria que castear en funcion del type de la variable (para eso era el caster!!!)
            prevalue = Aggregator.aggregate(this.aggregator, prevalue) // La lista de valores son nodos XML, quiero los valores simples.
            
            print " aggregated value: " + prevalue
         }
         else
         {
            print "type collection (no aggregation)"
         }
      }
      else
      {
         // FIXME: el valor es siempre String, habria que castear en funcion del type de la variable (para eso era el caster!!!)
         prevalue = xml.text()
      }

      
      Object castedValue = Caster.cast(prevalue, this.declaration.type)
      
      print " prevalue(" + prevalue + ")"
      print " casted(" + castedValue +")"
      print " type(" + castedValue.class.simpleName + ")"
      println ""
      
      
      return returnValue(castedValue)
   }

   /**
    * Idem a Function.returnValue para convertir de un tipo Java a una VariableInstance de XRE.
    * 
    * @param value valor extraido del XML para transformar a VariableInstance
    * @return
    */
   private VariableInstance returnValue(Object value)
   {
      // FIXME: value es siempre un string extraido del XML,
      //        para que VariableInstance sea valida, value deberia
      //        ser del tipo Java correspondiente al DataType XRE
      //        declarado en la variable que se esta resolviendo (this.declaration.type)
      //        sino siempre se genera una instancia STRING.
      
      // Obtiene el datatype desde la class del value
      DataType type = DataType.get(value.getClass());
      
      return new VariableInstance(new VariableDeclaration(this.declaration.name, type), value);
   }
}