package rules

/**
 * Unidad logica de reglas.
 * @author pab
 */
class Unit {

   String id
   String name
   String description
   String keywords
   
   // TODO: faltan campos de autor y contribuyentes, log de cambios, etc.
   
   // id de regla -> Rule
   Map rules = [:] // reglas de esta unidad
   
   
   public Unit()
   {
   }
   
   public Unit(String id, List rules)
   {
      this.id = id
      
      rules.each { rule ->
         this.rules[rule.id] = rule
      }
   }
   
   /**
    * FIXME: la ejecucion de reglas para eventos la debe hacer el Executer no la Unit,
    *        la unit funciona nomas como contenedor.
    *        
    * @param event
    * @param params paramName->value para todos los parametros declarados en el XRL
    * @return Map ruleid -> ruleReturnValues (es un map name->value)
    */
   public Map evaluate(String event, Map params)
   {
      /*
       * FIXME: la unidad y las reglas estan instanciadas mediante el parser,
       *  eso se deberia hacer una sola vez mientras no se baje la regla del cache.
       *  Pero la ejecucion de las reglas deberian hacerse sobre una copia de la unit
       *  porque se escriben datos de ejecucion en la instancia cacheada.
       *  La alternativa es que todos los datos que se escriban sean puestos en el
       *  contexto de ejecucion en lugar de guardarlos en cada objeto, de esa forma
       *  los objetos no tienen estado de ejecucion, y a las mismas instancias de
       *  objetos se les puede pasar distintos contextos para ejecutar distintas
       *  reglas. Hay que tener cuidado con esto si se necesitan ejecutar reglas
       *  en paralelo (ahi hay que bloquear la instancia para que un contexto no
       *  sobreescriba otro contexto), esto va a pasar cuando se le ponga un punto
       *  de acceso HTTP  al gestor de unidades (clase que todavia no existe pero
       *  va a ser singleton).
       *  Lo mas facil en todos los contextos es clonar las clases e instanciar
       *  las reglas para cada nueva ejecucion. 
       */
      
      // Contexto de ejecucion de las evaluaciones donde guarda todos los valores declarados o calculados.
      def context = new Context()
      
      // devuelve: [rule_4_1.v1:[sum1:rules.NamedValue@b32ed4]]
      // TODO: capaz habria que retornar el valor en lugar del NamedValue
      def result = [:]
      
      // TODO: considerar el event
      // Por ahora evaluo todas las reglas
      this.rules.each { entry ->
         
         // Asocia el contexto a la regla
         entry.value.context = context
         
         // evaluate de Rule
         // los valores resultado de las acciones se registran en el contexto de ejecucion de la evaluacion actual 
         result[entry.value.id] = entry.value.evaluate(params)
      }
      
      println "unit.evaluate() context: "+ context
      
      return result
   }
   
   
   /**
    * Usada desde Registry para obtener una regla.
    * @param ruleId
    * @return
    */
   public Rule getRule(String ruleId)
   {
      return this.rules[ruleId]
   }
   
   public void addRule(Rule rule)
   {
      this.rules[rule.id] = rule
   }
}