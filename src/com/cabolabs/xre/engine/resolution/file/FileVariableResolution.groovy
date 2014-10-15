package com.cabolabs.xre.engine.resolution.file

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

class FileVariableResolution extends VariableResolution {

   // Informacion para el request
   String locator // path + filename + .+ extension

   // Para procesar el xml
   //  http://www.ericonjava.com/?tag=groovy-xpath-gpath
   //  http://groovy.codehaus.org/GPath
   //  http://docs.codehaus.org/display/GROOVY/Reading+XML+with+Groovy+and+XPath
   //  http://stackoverflow.com/questions/2268006/how-do-i-create-an-xpath-function-in-groovy
   String extractor // XML gpath
   
   String sort // previo al aggregator cuando es una coleccion [asc, desc]
   String aggregator // TODO: enum de los strings declarados en Aggregator
   
   
   public FileVariableResolution(VariableDeclaration declaration)
   {
      super(declaration)
   }
   
   
   @Override
   public VariableInstance getValue(String sessionId, List<VariableInstance> params)
   {
      //RuleExecution re = Memory.getInstance().get(sessionId)
      
      // =========================================
      def start = System.currentTimeMillis()
      
      // no tiene params
      
      /////////////////////////////////////
      // TODO: soportar resultados JSON. //
      /////////////////////////////////////
      
      // el xml esta cacheado?
      GPathResult xml = cacheLookup()
      
      if (!xml)
      {
         xml = readXML()
         addToCache(xml)
      }
      
      VariableInstance value = getValueFromXML(xml)
      
      // =========================================
      def now = System.currentTimeMillis()
      println " - File Resolution getValue() execution time: " + (now - start) + "ms"
      
      return value
   }
   
   
   private GPathResult cacheLookup()
   {
      def cache = FileCache.getInstance()
      def res = cache.get(this.locator)
   }
   
   
   private void addToCache(GPathResult xml)
   {
      def cache = FileCache.getInstance()
      cache.add(this.locator, xml)
   }
   
   
   private GPathResult readXML()
   {
      File file = new File(this.locator)
      //String text = file.getText()
      //XmlSlurper xml = new XmlSlurper().parseText(text)
      //return xml.getDocument()
      
      // http://groovy.codehaus.org/api/groovy/util/XmlSlurper.html
      XmlSlurper xml = new XmlSlurper()
      return xml.parse(file)
   }
   

   private Object getValueFromXML(GPathResult xml)
   {
      //println "resolutor get value from XML"
      
      // Extrae valor navegando por el XML si hay extractor definido en el XRL
      if (this.extractor) // Evita error cuando el XML es un valor simple: <xml>valor</xml>
      {
         /*
         this.extractor.split("\\.").each { pi ->
            println "path item: " + pi
            xml = xml."$pi"
         }
         */
         
         // NECESITO QUE LO QUE ESTA ENTRE COMILLAS NO SE SPLITEE POR EL .
         //def pars = "A.B.C.'C.1'.'C.1.2'.D.E.'E.1'".split("'")
         //println pars // [A.B.C., C.1, ., C.1.2, .D.E., E.1]
         
         /**
          * Solucion al problema de tener . en el nombre de la tag:
          *  - Si se tienen tags con . y aparece en el extractor, debe estar entre comillas simples
          *  - El . se usa para dividir cada parte del extractor (GPath)
          *  - Para no dividir el nombre de la tag que tiene . se transforman los . dentro de comillas simples a *
          *    - Se evita colocar las comillas simples dentro del nombre de la tag usando it[1], it[0] incluye las comillas
          *  - Luego se divide en partes por el . (ahora los . de las tags son * asi que no se cortan)
          *    - Split es un array, se usa toList para poder invocar la transformacion de cada elemento con *.remplaceAll(...)
          *  - Y sobre cada parte (nombre de tag) se vuelven los * a . para que sea el nombre correcto de la tag
          *  - Ahora se procede como antes, quedando los nombres correctos de las tags, incluyendo las que contienen .
          *  
          */
         this.extractor.replaceAll("'(((.*)\\.)*?.*?)'", {
            
            it[1].replaceAll("\\.", "*")
         
         }).split("\\.").toList()*.replaceAll("\\*", ".").each { pi ->
      
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
      
      
      /////////////////////////////////////////////////////////////////
      // FIXME: esta es una version mejorada de lo que hay en
      //        HttpVariableResolution, hacer las correcciones ahi.
      /////////////////////////////////////////////////////////////////
      
      
      if (xml.size() > 1)
      {
         // Collection<String>
         prevalue = xml.collect { it.text() } // lista de valores como string
         
         // Si la variable es una collection, se deja la collection, no se ejecuta aggregation.
         if (this.declaration.type != DataType.COLLECTION)
         {
            if (this.aggregator)
            {
               // TODO: log warning
               println "WARNING: FileVariableResolution de " + this.declaration.name + " es de tipo COLLECTION pero se define un aggregator"
            }
            
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
            print " el tipo es collection, no se hace agregacion y se deja la collection "+ prevalue
         }
      }
      else
      {
         // FIXME: el valor es siempre String, habria que castear en funcion del type de la variable (para eso era el caster!!!)
         prevalue = xml.text()
         
         print " single value: " + prevalue + " " + prevalue.class
      }

      
      // FIXME: si es una coleccion, sus elementos son Strings
      //        seria deseable castear los elementos a algun tipo
      //        declarado, por ejemplo COLLECTION_OF_DATE intentaria
      //        castear cada elemento a DATE.
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