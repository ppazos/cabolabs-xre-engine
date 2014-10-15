package parser

import conditions.BooleanNode
import conditions.complex.AndNode
import conditions.complex.NotNode
import conditions.complex.OrNode
import conditions.operators.Contains
import conditions.operators.EqualsNode
import conditions.operators.GreaterThan
import conditions.operators.MatchNode
import conditions.operators.True
import values.NamedValueRef
import logic.actions.Action
import logic.ActionBlock
import logic.IfElse
import logic.LogicBlock
import rules.Rule
import rules.Unit
import values.Variable
import values.ValueType
import values.resolution.ActionResolution
import values.resolution.HttpResolution

// Importa los parsers de las acciones
import parser.actions.*

class Parser {

   private Map variables = [:]
   private Map resolutors = [:] // resolvedores de valores de variables name=>resolvedor
   
   private Map params = [:]
   private List returnNames = [] // nombres de valores de retorno de las acciones 
   
   public Parser()
   {
   }
   
   public Unit parse(File unitfile)
   {
      //String xmlst = unitxml.text
      def unitxml = new XmlSlurper().parse(unitfile)
      
      Unit unit = new Unit()
      
      // TODO: parsear el header
      unit.id = unitxml.header.id.text()
      unit.name = unitxml.header.name.text()
      
      Rule rule
      unitxml.rules.rule.each { rulexml ->
         
         rule = parseRule(rulexml)
         unit.addRule(rule)
      }
      
      return unit
   }
   
   private Rule parseRule(rulenode)
   {
      Rule rule = new Rule()
      
      // TODO: id, name, desc, events ...
      rule.id = rulenode.id.text()
      rule.name = rulenode.name.text()
      
      // Variables
      def var
      rulenode.definitions.var.each { varxml ->
         var = parseVariable(varxml)
         rule.addVariable(var)
      }
      
      rule.resolutors.putAll(resolutors)
      
      // FIXME: si parseo 2 reglas tengo que limpiar el resolutors
      
      // FIXME: el parser de parametros de entrada es solo para los nombres, lso valores se van a setear en la evaluaci�n de la regla.
      // Parametros
      // TODO: los params seguramente van a terminar siendo variables...
      def param
      rulenode.input.param.each { paramxml ->
         
         //param = parseParameter(paramxml)
         param = parseVariable(paramxml)
         rule.addParameter(param)
      }
      
      rule.logic = parseLogic( rulenode.logic )
      
      
      return rule
   }
   
   /*
    * <var name="var_rnd" type="integer">
         <!-- si no tiene valor debe tener un resolvedor asociado -->
         <resolutor type="http"><!-- TODO: enum de types: soap, tcp, db, file(txt, csv, xls) -->
           <locator url="http://localhost/YuppPHPFramework/services/xml/random" />
           <extractor path="a.b.c" />
           <aggregator type="first" sort="asc" /><!-- el tipo del resultado de la agregacion es el tipo de la variable que se esta resolviendo --> 
           <param name="par1" /><!-- usa esta variable declarada en el input como param del request -->
         </resolutor>
       </var>
    */
   private Variable parseVariable(varnode)
   {
      // varnode es NodeChild
      //println "parseVariable " + varnode.getClass().getSimpleName() + " " + varnode.name()
      
      Variable var = new Variable()
      var.name = varnode.@name.toString()
      var.type = ValueType.get( varnode.@type.text() ) // enum factory
      
      //println "parent: " + varnode.parent().name()
      println "parseVariable "+ var.name
      
      // parent es input o definitions
      if (varnode.parent().name() == "definitions") // para input no necesito resolutors
      {
         // Si no tiene value, debe tener un resolvedor
         // Si tiene value es una cte
         // A estas reglas hay que ponerles nombre
         if (varnode.@value.isEmpty())
         {
            if (varnode.resolutor.isEmpty())
            {
               throw new Exception("The variable "+ var.name + " should have value or a value resolver")
            }
            
            // Tiene resolutor
            def xresolutor = varnode.resolutor
            def resolutor
            
            // xresolutor es NodeChildren aunque sea uno solo
            //println "parseVariable " + xresolutor.getClass().getSimpleName() + " " + xresolutor.name()
            
            if (xresolutor.@type.toString() == "http")
            {
               // TODO: el tipo de resolver se determina por xresolutor.@type
               resolutor = new HttpResolution()
               resolutor.locator = xresolutor.locator.@url.toString()           // obligatorio
               
               if (!xresolutor.extractor.isEmpty())
                  resolutor.extractor = xresolutor.extractor.@path.toString()   // opcional (vacio es que el valor esta en la razi <a>valor</a>
               
               if (!xresolutor.aggregator.isEmpty())
               {
                  resolutor.aggregator = xresolutor.aggregator.@type.toString() // opcional
                  resolutor.sort = xresolutor.aggregator.@sort.toString() // opcional
               }
               
               // Parametros para el resolver
               def param
               xresolutor.param.each { xparam ->
               
                  // Referencia a una variable, no se usa Ref porque luego
                  // Rule.evaluate le pone el valor
                  param = new Variable(
                     name: xparam.@name.toString() // obligatorio
                  )
                  
                  // Agrega el param a la lista de parametros para el request de resolucion
                  resolutor.params << param
               }
            }
            else if (xresolutor.@type.toString() == "action")
            {
               // xresolutor.action es NodeChildren
               def action = parseAction(xresolutor.action) // xres.action me da NodeChildren
               resolutor = new ActionResolution(action: action)
            }
            else
            {
               throw new Exception("Resolutor type '"+ xresolutor.@type.toString() +"' not supported")
            }
            
            resolutors[var.name] = resolutor
         }
         else
         {
            // Value en string
            def strvalue = varnode.@value.toString()
            
            // Convertirlo al tipo de la variable
            var.value = Caster.cast(strvalue, var.type)
         }
      
      }
      
      /*
      if (!varnode.@locator.isEmpty())
      {
         // Faltan los params: se setean en Rule.resolveVars()
         HttpResolution resol = new HttpResolution()
         resol.locator = varnode.@locator.toString()
         resol.extractor = varnode.@extractor.toString()
         resol.aggregator = varnode.@aggregator.toString()
         
         // TODO: deberia definir el tipo de variable
         // TODO: definir los parametros necesarios para resolver la variable
         // TODO: segudametne los atributo de resolucion de la variable dependan
         //       de una clase aparte a Variable, para poder implementar distintos
         //       metodos de resolucion, asi que los atributos de resolucion se iran
         //       (locator, extractor, aggregator, ...)
         
         resolutors[var.name] = resol
      }
      */
      
      variables[var.name] = var
      
      return var
   }
   
   /**
    * 
    * @param paramnode http://groovy.codehaus.org/api/groovy/util/slurpersupport/NodeChild.html
    * @return
    */
   /*
   private Parameter parseParameter(paramnode)
   {
      Parameter param = new Parameter()
      
      param.name = paramnode.@name.toString()
      param.type = ValueType.get( paramnode.@type.text() ) // enum factory
      
      // FIXME: el onlyFor deberia estar definido en la variable que usa este parametro para resolverse.
      
      params[param.name] = param
      
      return param
   }
   */
   
   
   private LogicBlock parseLogic(logicnode)
   {
      LogicBlock logic = new IfElse()

      // ------------------------------------
      // Resolver ifcond
      //
      // <logic>
      //   <if>
      //     <and><!-- and binario -->
      //       <or>
      //         <eq in1="par2" in2="una_constante" /><!-- en este caso uso una constante para la comparacion -->
      //         <contains in1="var3" in2="par3" /><!-- contains es un is_sub_string in2.isSubstringOf(in1) -->
      //       </or>
      //       <gt in1="var1" in2="par1" /><!--  var1 > par1 ? -->
      //     </and>
      //     <do>
      //     ...
      //     </do>
      //   </if>
      logic.setIfCond( parseBoolean(logicnode.'if'.children()[0]) ) // children 0 es la cond
      
      // Esta accion es la que se ejecuta si se cumple la condicion
      // FIXME: podrian ser varias acciones
      logic.setIfBlock( parseActionBlock(logicnode.'if'.children()[1]) ) // children 1 es el do
      
      
      
      //println logicnode.name() // logic
      //println logicnode.'else'.name() // else
      //println logicnode.'else'.getClass()
      
      // imprime if, else, asi que no es do...
      //logicnode.'else'.children().each { it -> println it.name() }
      
      // else: puede ser nulo, un DO (Action) o un IfElse
      // Para ver de que tipo es, es necesario usar isEmpty
      // http://stackoverflow.com/questions/480431/how-can-i-check-for-the-existence-of-an-element-with-groovys-xmlslurper
      if (!logicnode.'else'.'do'.isEmpty())
      {
         //println "ELSE es una accion"
         
         // FIXME: podrian ser varias acciones
         logic.setElseBlock( parseActionBlock(logicnode.'else'.'do') )
      }
      else if (!logicnode.'else'.'if'.isEmpty())
      {
         logic.setElseBlock( parseLogic(logicnode.'else') ) // Recursion: else contiene IfElse
         
         //println "ELSE es un IfElse"
      }
      else
      {
         //println "ELSE es vacio"
      }
      
      return logic
   }

   
   private BooleanNode parseBoolean(boolnode)
   {
      // FIXME: hacerlo sin switch para poder mantenerlo mas facilmente.
      switch (boolnode.name())
      {
         // Condiciones complejas
         case 'and':
            return parseAnd(boolnode) // Parsea condicion AND
         case 'or':
            return parseOr(boolnode) // Parsea condicion OR
         case 'not':
            return parseNot(boolnode) // Parsea condicion NOT
         
         // Condiciones simples
         case 'gt':
            return parseGt(boolnode)
         case 'eq':
            return parseEq(boolnode)
         case 'match':
            return parseMatch(boolnode)
         case 'true':
            return parseTrue(boolnode)
         case 'contains':
            return parseContains(boolnode)
         default:
            throw new Exception('tipo de condicion '+ boolnode.name() + ' no soportado')
      }
   }
   
   /**
    * Parsea y devuelve una lista de acciones
    * @param donode nodo que contiene una lista de acciones
    * @return
    */
   //private List<Action> parseActionBlock(donode)
   private ActionBlock parseActionBlock(donode)
   {
      //def list = []
      def block = new ActionBlock()
      
      // Action es abstracta, tengo que devolver el tipo
      //correcto node es el 'do' que contiene acciones
      donode.children().each { actionnode ->
      
         /* tests...
         println 'parseAction ationnode ' + actionnode.@type.text()
         println 'in1: ' + actionnode.@in1.text()
         println 'inc1: ' + actionnode.@inc1.text()
         if (actionnode.@in1.isEmpty()) println 'in1 empty'
         if (actionnode.@inc1.isEmpty()) println 'inc1 empty'
         */
         
         if (actionnode.name() == 'action')
         {
            /*
            * <if>
               <gt inc1="2" inc2="1" />
               <do>
                <action type="sum" v1="par1" v2="par2" return="sum1" /> <<<===
                <action type="print" in1="sum1" />                      <<<===
                <action type="print" inc1="accion print hola mundo" />  <<<===
                <return name="sum1" />
               </do>
              </if>
            */
            
            // REFS
            // http://stackoverflow.com/questions/576955/groovy-way-to-dynamically-invoke-a-static-method
            // http://groovy.codehaus.org/Influencing+class+loading+at+runtime
            // http://groovy.codehaus.org/groovy-jdk/java/lang/Class.html
            
            // TODO: try catch por si hay errores
            // Print, Sum, DateDiff, Assign ...
            def action = parseAction(actionnode)
            block.addAction( action )
         }
         else if (actionnode.name() == 'return')
         {
            /*
             * <if>
                <gt inc1="2" inc2="1" />
                <do>
                 <action type="sum" v1="par1" v2="par2" return="sum1" />
                 <action type="print" in1="sum1" />
                 <action type="print" inc1="accion print hola mundo" />
                 <return name="sum1" /> <<<===
                </do>
               </if>
             */
            block.addReturnValue(actionnode.@name.text())
         }
      }
      
      return block
   }
   
   
   private Action parseAction(actionnode)
   {
      // actionnode es NodeChildren
      //println "parseAction " + actionnode.getClass().getSimpleName() + " " + actionnode.name()+ " " + actionnode.size()
      
      def actionType = actionnode.@type.text().capitalize()
      def actionParserName = 'parser.actions.'+ actionType +'Parser'
      def actionParser = Class.forName(actionParserName).newInstance() // porque no puedo hacer new $name
      def action = actionParser.parse(actionnode, this.variables, this.params)
      
      return action
   }
   
   
   // ======================================================================
   // Parse de condiciones complejas
   // ======================================================================
   
   /**
    * 
    * @param andnode http://groovy.codehaus.org/api/groovy/util/slurpersupport/GPathResult.html
    * @return
    */
   private AndNode parseAnd(andnode)
   {
      AndNode and = new AndNode(
         parseBoolean(andnode.children()[0]),
         parseBoolean(andnode.children()[1]))
      
      // El and tiene 2 subnodos que son las condiciones para hacer and
      // Cada condicion puede ser un and, or, not o una condicion simple (gt, eq, match, ...) 
      
      return and
   }
   
   private OrNode parseOr(ornode)
   {
      OrNode or = new OrNode(
         parseBoolean(ornode.children()[0]),
         parseBoolean(ornode.children()[1]))
      
      return or
   }
   
   private NotNode parseNot(notnode)
   {
      NotNode not = new NotNode( parseBoolean(notnode.children()[0]) )
      
      return not
   }
   
   
   // ======================================================================
   // Parse de condiciones simples
   // ======================================================================
   
   private True parseTrue(node)
   {
      println 'parseTrue'
      return new True()
   }
   
   private EqualsNode parseEq(node)
   {
      println 'parseEq'
      
      // TODO: Hay que asegurar que ambos son del mismo tipo,
      // sobre todo si se compara una variable o parametro con
      // una constante, la constante siempre se parsea como String.
      
      def v1 = parseNamedValue(node.@in1.toString(), node.@inc1.text())
      def v2 = parseNamedValue(node.@in2.toString(), node.@inc2.text())
      
      // Si v1 es cte STRING y v2 no es STRING, casteo el v1 al tipo de v2
      if (!node.@inc1.isEmpty() && v2.type != ValueType.STRING)
      {
         v1.value = Caster.cast(v1.value, v2.type)
      }
      
      // Idem para v2
      if (!node.@inc2.isEmpty() && v1.type != ValueType.STRING)
      {
         v2.value = Caster.cast(v2.value, v1.type)
      }
      
      return new EqualsNode(v1, v2)
   }
   
   private MatchNode parseMatch(node)
   {
      println 'parseMatch'
      return new MatchNode(
         parseNamedValue(node.@in1.toString(), node.@inc1.text()),
         node.@regex.toString())
   }
   
   private Contains parseContains(node)
   {
      println 'parseContains'
      return new Contains(
         parseNamedValue(node.@in.toString(), null),
         parseNamedValue(node.@value.toString(), null))
   }
   
   private GreaterThan parseGt(node)
   {
      println 'parseGT'
      
      // FIXME: las variables deberian ser siempre referencias
      //        a las instancias que se crean una sola vez dentro
      //        de rule, sino cada operador tiene sus propias
      //        instancias que pueden tener valores distintos
      //        al resolverse en tiempos distintos.
      // Lo que hace parseNamedValue es tomar la variable de la
      // coleccion global, para que siempre sea la referencia a
      // la misma variable sin crear instancias nuevas. Solo se
      // crean nuevas instancias cuando son constantes (valores
      // sin nombre que se usan localmente).
      
      // TODO: Hay que asegurar que ambos son del mismo tipo,
      // sobre todo si se compara una variable o parametro con
      // una constante, la constante siempre se parsea como String.
      
      def v1 = parseNamedValue(node.@in1.toString(), node.@inc1.text())
      def v2 = parseNamedValue(node.@in2.toString(), node.@inc2.text())
      
      // Si v1 es cte STRING y v2 no es STRING, casteo el v1 al tipo de v2
      if (!node.@inc1.isEmpty() && v2.type != ValueType.STRING)
      {
         v1.value = Caster.cast(v1.value, v2.type)
         v1.type = v2.type
      }
      
      // Idem para v2
      if (!node.@inc2.isEmpty() && v1.type != ValueType.STRING)
      {
         v2.value = Caster.cast(v2.value, v1.type)
         v2.type = v1.type
      }
      
      return new GreaterThan(v1, v2)
      /*
      return new GreaterThan(
         parseNamedValue(node.@in1.toString(), node.@inc1.text()),
         parseNamedValue(node.@in2.toString(), node.@inc2.text()))
      */
   }
   
   
   // ===================================================================
   // Valores en las condiciones, si se hace referencia a un parametro o
   // variable, la devuelve, y si hay value, crea un nuevo named value
   // con ese valor.
   // ===================================================================
   /**
    * Asegura que la referencia por el mismo nombre sea a la misma instancia
    * de la variable, asi no hay instancias distintas de una variable con
    * el mismo nombre. Las instancias distintas con mismo nombre pueden
    * dar problema por tener distintos valores en distintos momentos.
    * 
    * @param name
    * @param value
    * @return
    */
   private Variable parseNamedValue(String name, String value)
   {
      if (variables.containsKey(name))
      {
         return variables[name]
      }
      if (params.containsKey(name))
      {
         return params[name]
      }
      // Si es una constante
      if (value)
      {
         // Si hay valor, es una constante sin nombre
         return new Variable(value:value)
      }
      
      /* NO PUEDO DEVOLVER NVR porque no es NamedValue
      // Si es una referencia a un valor aun no declarado.
      // El valor puede ser la salida de una accion y solo se sabe el nombre.
      // Puede verse como una variable cuya resolucion es la ejecucion de una accion.
      // La diferencia es que la variable se declara en la regla y se resuelve luego,
      // el resultado de una accion no se declara en la regla, se declara cuando se
      // calcula y devuelve (declaracion en tiempo de ejecucion).
      if (name)
      {
         return new NamedValueRef(name:name)
      }
      */

      throw new Exception("El valor con nombre '$name' no esta declarada en la definicion de la regla, tampoco tiene valor constante")
   }
}