package rules

import conditions.BooleanNode
import logic.LogicBlock
import parser.Caster
import values.Variable
import values.ValueType
import values.resolution.Aggregator
import groovy.transform.Synchronized

class Rule implements Cloneable {

   String id
   String name
   String description
   String keywords

   // Lista de las excepciones de resolver variables con http
   // Se guarda la lista porque los threads que se usan no pueden tirar excepciones
   List errors = []
   
   @Synchronized
   void reportError(Exception e)
   {
      this.errors << e
   }
   
   // TODO: faltan los eventos para los que se debe ejecutar la regla
   
   // nombre de variable -> atributos de la variable
   Map definitions = [:]
   Map resolutors = [:] // resuelven valores de variables name->resolutor
   
   // nombre del parametro -> atributos del parametro
   Map input = [:]
   
   // Para guardar el resultado de la ejecucion y poder pedirlo
   // varias veces luego de ejecutarse la regla sin necesidad
   // de ejecutarla de nuevo.
   Map result
   
   // Logica de la regla, con IF-COND-ACCIONES/ELSE-ACCIONES
   // La evaluacion toma como entrada las definiciones de variables y los parametros de entrada
   LogicBlock logic
   
   
   /**
    * Contexto de ejecucion, guarda todos los valores declarados y calculados.
    * Lo setea la unit. 
    * FIXME: poner los parametros y variables en el contexto.
    */
   Context context
   
   
   public Rule() {}
   
   /*
   public Rule(String id, List vars, LogicBlock logic)
   {
      this.id = id
      
      // FIXME: si alguna var no tiene valor, debe tener un resolutor.
      vars.each { var ->
         
         this.definitions[var.name] = var
      }
      
      this.logic = logic
   }
   */
   
   
   /**
    * Necesario para implementar el clone
    */
   boolean equals(Object other)
   {
      other instanceof Rule && this.id == other.id
   }
   
   
   /**
    * TODO: necesito una funcion que busque tanto variables como parametros por nombre (no es necesario saber si es param o var porque solo interesa su value).
    */
   public Variable getParameter(String name)
   {
      return this.input[name]
   }
   public void addParameter(Variable param)
   {
      this.input[param.name] = param
   }
   
   
   public Variable getVariable(String name)
   {
      return this.definitions[name]
   }
   public void addVariable(Variable var)
   {
      this.definitions[var.name] = var
   }
   
   // FIXME: que params sea List porque el nombre lo tiene adentro, no necesito las keys.
   public Map evaluate(Map params)
   {
      //println "rule.evaluate " + params
      //params.each { entry -> println entry.value.class } // DICE QUE ESTO ES STRING!!!!
      

      def castedInputValue
      
      // Setea valor simple de Variables input (que se crea sin valor en el parseo de la regla)
      // TODO: verificar el tipo de la variable
      this.input.each { name, var ->
         
         //println "declared param " + name
         
         // Si en el execute falta algun param para resolver una variable, tirar except.
         if (!params[name]) throw new Exception('No viene valor para el param '+ name)
         
         // http://code.google.com/p/xre-ui/issues/detail?id=9
         // Verifica el tipo del valor pasado, casteando al tipo correcto
         // Ej. si el tipo de la variable es integer, y pasan un string,
         // el valor del string debería ser parseable a integer.
         castedInputValue = Caster.cast(params[name], var.type)
         
         // TODO: verificar que el tipo del Parameter coincida con el valor pasado en params
         // Setea el valor del parametro declarado usando el valor en params
         var.value = castedInputValue //params[name]
      }
      
      
      // FIXME: input y definitions no deberian tener variables con el mismo nombre, esto se deberia verificar en el parseo.
      // agrego mis variables y parametros al contexto
      context.addAllValues(definitions)
      context.addAllValues(input)
      
      
      // setea el contexto de ejecucion actual a la logica
      // si hay acciones que calculan valores, se registran en el contexto
      logic.context = context
      
      
      
      // =========================================================================================
      // FIXME: para evaluate/exec las variables ya deberian estar resueltas/init
      //        y los params para execute no deberian ser los mismos que para init
      //        porque pueden haber variables de mismo nombre pero distinto tipo y
      //        la verificacion del tipo por el nombre de la variable va a fallar miserablemente.
      // ==========================================================================================
      
      
      
      // Cuando se hace evaluate (exec), todas las variables deberian estar ya resueltas (init).
      // resolucion de variables
      //this.resolveVars(params) // (params) ya no se usan los params se usa input
      
      
      
      //println "rule.evaluate input " + this.input
      //this.input.each { entry -> println entry.value.class } // DICE QUE ESTO ES STRING!!!!
      
      
      // TODO: deberia registrar variables resueltas en el context
      
      //println "rule.evaluate 4"
      
      // ejecuta logica verificando condiciones y ejecutando acciones
      //println "Ejecuta logica de la regla:"
      
      this.result = logic.execute()
      return this.result
      
      
      // La evaluacion no devuelve valor (aun), la idea es que esto se pueda hacer
      // de forma asincronica con quien llama, el problema es que hay reglas que
      // deben ejecutarse en tiempo real y se espera algun resultado, por ejemplo
      // saber si la regla se cumple o no (tal vez con retornar el booleano de la
      // condicion alcance por ahora...
   }
   
   
   /**
    * Resuelve variables en el init de la ejecucion.
    * @param params
    * @return true si no hay errores, false en caso contrario.
    */
   public boolean resolveVars(Map params)
   {
      //println "Rule.resolveVars"
      
      // TEST THREADS
      // http://mrhaki.blogspot.com/2011/05/groovy-goodness-apply-read-and-write.html
      def thrs = []
      
      //println "resolutors " + this.resolutors
      
      
      // ================= Asignacion de valores a los params de los reolutors =================
      // ================= Esto podria hacerse en el bucle de abajo para cada variable =========
      //
      // Cada resolutor tiene declaradas las variables que va a usar
      // Ahora necesito pasarle los valores
      // Map
      def castedParamValue // http://code.google.com/p/xre-ui/issues/detail?id=9
      
      this.resolutors.each { name, resolutor ->
         
         // Podria hacerse un metodo de inicializacion de todos los tipos de resolutors e invocarlos aca.
         if (resolutor.type == "http")
         {
            //println ">> " + resolutor.params
            // List<Variable> solo tienen nombre, no tienen valor, aca les pongo el valor
            resolutor.params.each { decvar ->
            
               // http://code.google.com/p/xre-ui/issues/detail?id=9
               // Verifica el tipo del valor pasado, casteando al tipo correcto
               // Ej. si el tipo de la variable es integer, y pasan un string,
               // el valor del string debería ser parseable a integer.
               castedParamValue = Caster.cast(params[decvar.name], decvar.type)
               
               // Para HTTPResolutor los params son strings
               // TODO: esto puede cambiar para otros resolutors
               //decvar.value = this.input[decvar.name].value.toString()
               decvar.value = castedParamValue.toString() // Asigna el valor simple como string al param del resolutor
               
               //println "Setea: " + decvar.name + " " + decvar.value
            }
         }
         // TODO otros tipos de resolutors
      }
      
      //println "resolve vars after resolutors"
      
      // OK, los params tienen los valores
//      println "resolutors: "
//      this.resolutors.each { entryresol ->
//         println ".>> " + entryresol.value.params.value
//      }
      //
      // ================= Asignacion de valores a los params de los reolutors =================
      
      
      // ==============================================================
      // Lista de variables
      // Primero se resuelven los http y luego el resto
      // Asi el resto puede usar los valores de http
      def todo_def_resolutors = []
      
         
      this.definitions.each { entry ->

         //println "resolve vars definitions.each"
         
         // Hay que resolver SOLO SI la variable no tiene valor
         // Si tiene valor puede no tener un resolutor asociado
         if (entry.value.value)
         {
            //println "YA TIENE VALOR " + entry.value.name
            
            //println "La variable "+ entry.value.name + " ya tiene valor: " + entry.value.value
            return // retorna del loop actual, sigue con el siguiente (closure no tiene continue)
         }
         
         //println "NO TIENE VALOR " + entry.value.name
         
         //println 'Rule resolveVars: ' + entry.key
         
         // Cada resolucion se hace en un thread aparte
         def th = Thread.start {
         
            //println "thread para resolver la cosa"

            // FIXME: se debe verificar si tira una except, y parar la ejecucion haciendo un log.
            //        luego se podria definir una politica de ejecucion de descartar la ejecucion o intentar ejecutar hasta que funcione o hasta X fallas.
            
            //println resolutors[entry.key]
            
            if (resolutors[entry.key].type != "http")
            {
               synchronized(this) {
                  todo_def_resolutors << entry.value
               }
               
               //println "luego resuelve " + entry.key
               
               return
            }
            
            println "resuelve " + entry.key
            
            // Resuelve el valor con getValue, hace el request internamente, etc.
            def value = resolutors[entry.key].getValue() // puede ser un string o una lista de strings
            
            def casted = false
            
            // ------------------------------------------------------------
            // FIXME: si es una lista de strings y el tipo NO es collection, tengo que ejecutar el aggregator!!!!
            // ------------------------------------------------------------
            if (value instanceof Collection && entry.value.type != ValueType.COLLECTION)
            {
               println "value collection"
               
               // Antes de agregar hay que pasar cada elemento de la lista al tipo correcto
               // Solo si no es STRING
               // Equivale a verificar los tipos de items en la lista contra el tipo de la variable
               if (entry.value.type != ValueType.STRING)
               {
                  def castedList = []
                  value.each { item ->
                     castedList << Caster.cast(item, entry.value.type)
                  }
                  
                  value = castedList
                  
                  // Para la agregacion ya castea, no deberia intentar castear de nuevo abajo
                  casted = true
               }
               
               // Sort
               if (resolutors[entry.key].sort)
               {
                  if (resolutors[entry.key].sort == 'asc')
                  {
                     value = value.sort() // ordena de menor a mayor
                  }
                  else if (resolutors[entry.key].sort == 'desc')
                  {
                     value = value.sort{ a, b -> (a < b) ? 1 : 0 }
                  }
                  // FIXME: otro caso deberia tirar error en parser
                  
                  println "sort: " + resolutors[entry.key].sort + " " + value
               }
               
               // Aggregate
               value = Aggregator.aggregate(resolutors[entry.key].aggregator, value)
               
               println "aggregated value: " + value
            }
            
            // Castea segun el tipo de la variable,
            // el problema es que el valor de comparacion, si es constante,
            // tambien se va a cargar desde un XML (la definicion de la unit)
            // entonces sera de tipo String, tal vez comparando todo como string
            // es mas facil que estar transformando todo al tipo correcto.
            Object castedValue
            if (!casted)
               castedValue = Caster.cast(value, entry.value.type) // Variable.type
            else
               castedValue = value
            
            print " casted(" + castedValue +") "
            print "type(" + castedValue.class.simpleName + ")"
            println ""
            
            entry.value.value = castedValue // resolutors[entry.key].getValue()

         } // th
         
         
         // Guarda el thread en el pool para sincronizar luego
         thrs << th
         
         
         // Agrega manejador de excepciones del thread
         th.setDefaultUncaughtExceptionHandler( {t, ex ->
            
            // FIXME: log a archivo
            //println 'ignoring: ' + ex.class.name
            //println ex.getMessage()
            
            // relanza la excepcion en el thread padre
            // esto es necesario para que desde la UI se puedan ver los errores
            // TODO: probar si detiene o no la ejecucion de los demas threads
            // NO SIRVE RELANZAR LA EXCEPCION, se ve que es dentro de cada thread
            // y no se agarra en el thread padre.
            //throw ex
            
            // Guardo los errores en una lista
            this.reportError(ex)
            
            //e.printStackTrace()
            //Thread.sleep(2000)
            
         } as Thread.UncaughtExceptionHandler)
      }
      
      thrs.each { it.join() }
      
      
      
      // ==============================================================
      // Variables que se resuelven luego de resolver
      // todas las otras variables por http
      todo_def_resolutors.each { var ->
         
         //println "== resuelve todo " + var.name
         
         // TODO: codigo copiado de arriba, hacer una funcion aparte
         
         // Resuelve el valor con getValue, hace el request internamente, etc.
         def value = resolutors[var.name].getValue() // puede ser un string o una lista de strings
         
         //println "== value: " + value
         
         // ------------------------------------------------------------
         // FIXME: si es una lista de strings y el tipo NO es collection, tengo que ejecutar el aggregator!!!!
         // ------------------------------------------------------------
         if (value instanceof Collection && var.type != ValueType.COLLECTION)
         {
            //println "value collection"
            
            // Antes de agregar hay que pasar cada elemento de la lista al tipo correcto
            // Solo si no es STRING
            // Equivale a verificar los tipos de items en la lista contra el tipo de la variable
            if (var.type != ValueType.STRING)
            {
               def castedList = []
               value.each { item ->
                  castedList << Caster.cast(item, var.type)
               }
               
               value = castedList
            }
            
            // FIXME: sort
            
            value = Aggregator.aggregate(resolutors[var.name].aggregator, value)
         }
         
         // Castea segun el tipo de la variable,
         // el problema es que el valor de comparacion, si es constante,
         // tambien se va a cargar desde un XML (la definicion de la unit)
         // entonces sera de tipo String, tal vez comparando todo como string
         // es mas facil que estar transformando todo al tipo correcto.
         Object castedValue = Caster.cast(value, var.type) // Variable.type
         
         print " casted(" + castedValue +") "
         print "type(" + castedValue.class.simpleName + ")"
         println ""
         
         //var.value = castedValue // resolutors[entry.key].getValue()
         
         this.definitions[var.name].value = castedValue
      }
      
      
      //println "Rule evaluateVars errors: " + this.errors
      
      // true si no hay errores
      return this.errors.size() == 0
   }
}