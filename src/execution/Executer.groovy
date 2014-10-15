package execution

import groovy.transform.Synchronized

import rules.Context
import rules.Registry
import rules.Rule

/**
 * Crea instancias de ejecucion de reglas y las ejecuta de forma independiente y en paralelo.
 * @author pab
 *
 */
@Singleton // http://stackoverflow.com/questions/7612520/groovy-singleton-pattern
class Executer {

   // TODO: considerar unidades.
   // TODO: considerar evento? no se si eso iria aca.
   
   // TODO: debe sincronizarse porque la ejecucion es en paralelo
   private Map rules = [:]
   //private Map contexts = [:] // el context lo tiene cada regla
   //private Map results = [:] // el resultado se guarda en la regla ejecutada
   
   
   /*
   // Singleton -----------------------------------------------
   private static Executer instace = new Executer()
   
   private Executer()
   {
      println "Executer"
   }
   
   public static Executer getInstance()
   {
      return instance
   }
   // ---------------------------------------------------------
   */
   
   // Execution session id
   private String getSessionID()
   {
      return java.util.UUID.randomUUID() as String
   }
   // ---------------------------------------------------------
   
   
   /**
    * Hace una copia y ejecuta la regla de forma sincronica, devolviendo resultados si los hay. 
    * @param rule
    * @return
    */
   /*
   public Map addAndExecute(Rule rule, Map params)
   {
      // Auxiliar para poder pasar parametros
      //def worker = { prms, ress ->
      def worker = { //ress ->
         
         //println "worker!"
         
         try {
            
            // Contexto de ejecucion, en el anterior se la setea unit
            rule.context = new Context( getSessionID() )
            
            //println "worker!2"
            
            def ret = rule.evaluate(params)
            
            //println "worker!3"
            
            synchronized(this) {
               //println "worker!4"
               this.results[rule.id] = ret // pone el resultado en la coleccion compartida entre el thread y el padre
            }
            
            //println "worker!5"
            //return ret // No se si puedo retornar desde el thread
         }
         catch (Exception e)
         {
            println "EXCEPT!: " + e.message // con threads me dice que ress es null ...
            e.printStackTrace()
         }
         
         //Thread.sleep(1000)
      }
      // FIXME: no estoy agregando nada a rules o context, ejecuto derecho...
      //def th = Thread.start {
      
      //worker.curry(this.results) // con esto me dice que ress es null ...
      //worker(this.results)
      //worker()
      
      // FIXME:
      // Se tranca y no termina de ejecutar y en cada ejecucion es distinto!
//      def th = Thread.start( worker() ) // TODO: ver como retorno el valor
      
      // prueba sin threading
      worker()
      
      //}
      
      // TODO: borrar el resultado considerando concurrencia
      // TODO: cuidado, otro thread puede sobreescribir el valor porque se usa el ruleid para indexar! deberia usarse un indice generado u otra cosa ej timestamp
      
      // FIXME
      // Que tenga que esperar aca es como sino ejecutara el thread porque no dejo entrar a otro thread que ejecute en paralelo.
      // La joda en realidad es que los threads sean creados afuera (ej. una interfaz http o tcp), que desde cada thread se llame
      // al Executer singleton, y que aqui se asegure que todo esta sincronizado para que las ejecuciones no se choquen. La otra es que cada thread cree un nuevo executer y listo.
      // El threading aqui se necesita cuando UN thread necesita ejecutar varias reglas al mismo tiempo, ej. el evento levanto mas de una regla registrada.
      // En este contexto, varias de las reglas podrian retornar valores (sincronas) y se deberia garantizar de que todas las reglas terminan y que se tienen todos los resutlados antes de retornar. 
//      th.join() // asegurar de que el thread termino antes de retornar! sino no tengo valor...
      
      println this.results
      
      return results[rule.id]
   }
   */
   
   
   /**
    * Agrega todas las reglas para el evento y devuelve la lista
    * de identificadores para ejecutarlas luego.
    * @param event
    * @return
    */
   @Synchronized
   public List addAll(String event)
   {
      // TODO:
   }
   
   /**
    * Consulta por reglas ejecutandose.
    */
   @Synchronized
   public List getRules(String ruleId)
   {
      def list = []
      this.rules.each { sessId, rule ->
         if (rule.id == ruleId) list << rule
      }
      
      return list
   }
   
   @Synchronized
   public Rule getRule(String sessionId)
   {
      return this.rules[sessionId]
   }
   
   /**
    * Agrega una regla sin ejecutarla
    * @param ruleId
    * @return identificador de la sesion de ejecucion
    */
   @Synchronized
   public String add(String unitId, String ruleId)//(Rule rule)
   {
      def reg = Registry.instance
      def copyRule = reg.getRule(unitId, ruleId) // Tira excepcion sino encuentra la regla
      
      // Identificador de la sesion de ejecucion, para referencia posterior
      String sessId = getSessionID()
      
      // Contexto de ejecucion
      copyRule.context = new Context( sessId )
      
      // Agrega la regla
      this.rules[sessId] = copyRule

      //println "add sessId " + sessId
      
      return sessId
   }
   
   /**
    * Quita una regla y la devuelve.
    * @param sessId
    * @return la regla removida del executer
    */
   @Synchronized
   public Rule remove(String sessId)
   {
      //def rule = this.rules[sessId]
      
      return this.rules.remove(sessId)
   }
   
   /**
    * Quita todas las reglas que se estan ejecutando.
    * @return
    */
   @Synchronized
   public List removeAll()
   {
      def list = []
      def delMap = [:]
      delMap.putAll(this.rules)
      delMap.each { key, value ->
         list << this.rules.remove(key)
      }
      
      return list
   }
   
   /**
    * Lanza la resolucion de variables que se pueda hacer con los params que se le pasan.
    * FIXME: verificar que solo se resuelven las variables para las que hay params y que
    *       las demas quedan pendientes de resolucion si no tienen los parametros requeridos
    *       para resolverse. Si en execute no se pasan esos parametros, ahi si se lanza una excepcion.
    * La idea es acelerar la ejecucion resolviendo antes las variables (la resolucion http es lenta).
    * @param sessionId
    * @param params parametros para resolver variables
    * @return lista de errores o null si no hubieron errores
    */
   @Synchronized
   public List init(String sessionId, Map params)
   {
      Rule rule = this.rules[sessionId]
      
      if (!rule) throw new Exception("Rule with session id '$sessionId' not found")
      
      // false si hubieron errores
      if (!rule.resolveVars(params))
      {
         return rule.errors
      }
      
      return null
   }
   
   /**
    * Ejecuta una regla agregada previamente
    * @param sessionId
    * @param params valores de input + parametros para resolver variables no resueltas
    * @return
    */
   @Synchronized
   public Map execute(String sessionId, Map params)
   {
      Rule rule = this.rules[sessionId]
      
      if (!rule) throw new Exception("Rule with session id '$sessionId' not found")
      
      // Solo intenta resolver las variables que NO tienen valor
      Map ret = rule.evaluate(params)
      
      // TODO: crear un trash collector que limpie las reglas ya ejecutadas
      // no sacarla en seguida permite consultar información luego de que
      // es ejecutada, ej. valores de variables.
      // para tener un log completo se podria bajar a disco con XStream.
      
      // TODO: deberia haber un estado, para cambiarlo cuando llego aca,
      // asi el getRules ruleId puede tirar solo las reglas activas.
      
      // Resultado de la ejecucion
      return ret
   }
}