package com.cabolabs.xre.engine.parser

import groovy.util.slurpersupport.GPathResult
import java.lang.reflect.Constructor

import com.cabolabs.xre.core.definitions.*
import com.cabolabs.xre.core.resolution.VariableResolution
import com.cabolabs.xre.core.logic.*
import com.cabolabs.xre.core.logic.bool.BooleanFunction
import com.cabolabs.xre.engine.resolution.file.FileVariableResolution
import com.cabolabs.xre.engine.resolution.function.FunctionVariableResolution
import com.cabolabs.xre.engine.resolution.http.HttpVariableResolution
import com.cabolabs.xre.engine.util.*

import com.cabolabs.xre.engine.resolution.predefined.*

import com.cabolabs.xre.engine.logic.numbers.*
import com.cabolabs.xre.engine.logic.bool.*
import com.cabolabs.xre.engine.logic.common.*

class RuleDefinitionParser {

   // Map nombre de funcion -> clase que implementa la funcion
   // Se utiliza en parseLogic para instanciar funciones
   // Aqui se ponen las acciones fijas que vienen con el engine,
   // otras acciones pueden estar declaradas en la regla utilizando
   // el nombre de la clase que las implementa y estar implementadas
   // por fuera del engine, pero accesibles desde el classpath, por lo
   // que se puede usar Class.forName(nombre) => Class y luego .newInstance()
   private static functionFactories = [
      
      // funciones numericas
      "sum":        Sum.class,
      "minus":      Minus.class,
      "mult":       Mult.class,
      "div":        Div.class,
      
      "print":      Print.class,
      "assign":     Assign.class,
      
      // funciones con fechas
      "dateDiff":   com.cabolabs.xre.engine.logic.dates.DateDiff.class,
      "parseDate":  com.cabolabs.xre.engine.logic.dates.ParseDate.class,
      "now":        com.cabolabs.xre.engine.logic.dates.Now.class,
      
      // funciones booleanas complejas
      "and":        com.cabolabs.xre.core.logic.bool.AndFunction.class,
      "or":         com.cabolabs.xre.core.logic.bool.OrFunction.class,
      "not":        com.cabolabs.xre.core.logic.bool.NotFunction.class,
      "xor":        com.cabolabs.xre.core.logic.bool.XorFunction.class,
      
      // funciones booleanas simples
      "gt":         GreaterThan.class,
      "equals":     com.cabolabs.xre.engine.logic.bool.Equals.class,
      "true":       com.cabolabs.xre.engine.logic.bool.True.class,
      "false":      com.cabolabs.xre.engine.logic.bool.False.class,
      
      // funciones sobre colecciones
      "contains":   com.cabolabs.xre.engine.logic.collections.Contains.class,
      "intersect":  com.cabolabs.xre.engine.logic.collections.Intersect.class,
      
      // funciones booleanas sobre strings
      "startsWith": com.cabolabs.xre.engine.logic.strings.StartsWith.class,
      "endsWith":   com.cabolabs.xre.engine.logic.strings.EndsWith.class,
      "matches":    com.cabolabs.xre.engine.logic.strings.Matches.class
   ]
   
   
   /**
    * Se van agregando las variables declaradas en declarations,
    * input o returnName de functions.
    */
   private static Map<String, VariableDeclaration> declaredVars = [:]
   
   
   
   static RuleDefinition parse(File rule)
   {
      return parse(rule.getText())
   }
   
   static RuleDefinition parse(String ruleText)
   {
      def xml = new XmlSlurper().parseText(ruleText)
      
      // TODO: validar contra XSD
      
      // Header
      String ruleId   = xml.header.id.text()
      String name     = xml.header.name.text()
      String desc     = xml.header.description.text()
      String keywords = xml.header.keywords.text()
      
      
      RuleDefinition rule = new RuleDefinition(ruleId)
      rule.name = name
      rule.description = desc
      rule.keywords = keywords
      
      
      // Declarations
      List<VariableDeclaration> declarations = parseDeclarations(xml.declarations)
      
      rule.setDeclarations(declarations)
      
      // Input
      List<VariableDeclaration> input = parseInput(xml.input)
      
      rule.setInput(input)
      
      // Resolutions
      // Ya los setea en la rule
      parseResolutions(xml.resolutions, rule)
      

      // Logic
      //AbstractBlock logic = parseAbstractBlock(xml.logic.children()[0], rule) // le paso el primer item dentro de rule.logic, puede ser if, do o funcion
      AbstractBlock logic = parseLogicBlock(xml.logic, rule) // le paso el primer item dentro de rule.logic, puede ser if, do o funcion
      
      rule.setLogic(logic)
      
      
      return rule
   }
   
   private static List<VariableDeclaration> parseDeclarations(GPathResult declarations)
   {
      List<VariableDeclaration> ret = []
      VariableDeclaration var
      declarations.children().each { xvar ->
         
         var = parserVariableDeclaration(xvar)
         ret << var
         
         println "declaration: "+ var.name
         
         // Marca la variable como declarada
         declaredVars[var.name] = var
      }
      return ret
   }
   
   private static List<VariableDeclaration> parseInput(GPathResult input)
   {
      List<VariableDeclaration> ret = []
      VariableDeclaration var
      input.children().each { xvar ->
         
         var = parserVariableDeclaration(xvar)
         ret << var
         
         println "input: "+ var.name
         
         // Marca la variable como declarada
         declaredVars[var.name] = var
      }
      return ret
   }
   
   private static void parseResolutions(GPathResult xresolutions, RuleDefinition rule)
   {
      // TODO: hay que implementar esta para que los valores de declarations se pongan en los values de RuleExecution y se puedan usar

      VariableResolution resolution
      
      xresolutions.children().each { xres ->
         
         // Function Resolution
         if (xres.name() == "function")
         {
            // Nombre de la variable que resuelve la resolution
            String varName = xres.@var.text()
            VariableDeclaration vard = declaredVars[varName]
            
            // Si la variable no fue declarada previamente, error
            if (!vard)
            {
               throw new Exception("La variable "+ varName +" referenciada desde la resolution function no fue declarada previamente, verifique los nombres")
            }
            
            resolution = new FunctionVariableResolution(vard)
            resolution.setFunction( parseFunction(xres.children()[0], null, rule) ) // La funcion es el primer y unico nodo hijo del resolution
            
            println "  Function Resolution Declaration: " + vard + " " + vard.name
            
            rule.addToResolutions( resolution )
         }
         else if (xres.name() == "http")
         {
            // TODO: poder resolver varias variables con un mismo XML (locator)
            //       pero varios extractor y aggregator.
            
            // Nombre de la variable que resuelve la resolution
            String varName = xres.@var.text()
            VariableDeclaration vard = declaredVars[varName]
            
            // Si la variable no fue declarada previamente, error
            if (!vard)
            {
               throw new Exception("La variable "+ varName +" referenciada desde la resolution function no fue declarada previamente, verifique los nombres")
            }
            
            resolution = new HttpVariableResolution(vard)
            
            // TODO: setear params si los tiene, para saber que params son para esta resolution
            // se usa el nombre de la variable a resolver como namespace, entonces lo parametros
            // externos que vienen al 'resolve' deben usar ese valor como namespace si son
            // especificos para resolver cierta variable y ser utilizandos en la resolution de
            // esa variable y no quedar disponibles para toda la ejecucion de la regla
            // (ej. otras resolutions). 
            
            // locator, extractor, aggregator
            resolution.locator = xres.locator.@url.text() // locator es obligatoria
            resolution.extractor = xres.extractor.@path.text() // extractor es obligatoria
            if(!resolution.aggregator) // aggregator es opcional
            {
               resolution.aggregator = xres.aggregator.@type.text() // TODO: enum
               
               if (!xres.aggregator.@sort.isEmpty())
               {
                  resolution.sort = xres.aggregator.@sort.text() // TODO: enum
               }
            }
            
            
            // TODO: soportar especificacion del metodo de HTTP (POST/GET)
            
            
            // TODO: Idem al codigo de parseFunction, ver si se puede reutilizar
            // Params del request, es un map si esta presente
            Map params
            if (!xres.@params.isEmpty())
            {
               params = new GroovyShell().evaluate( xres.@params.text() ) // "[key1:value1,key2:value2]")
            }
            if (params)
            {
               // Transforma los valores en declaraciones de variables (ctes o predefinidas tambien)
               Map varParams = [:]
               String name
               VariableDeclaration predefVarDec // Auxiliar para variables predefinidas
               VariableResolution preDefResolution // Auxiliar para variables predefinidas
               
               params.each { keyval -> // nombre de variable o valor constante
                  
                  // El valor puede ser el nombre de una variable o un valor constante
                  // name se usa para verificar si es un nombre valido
                  name = keyval.value.toString()
                  
                  // 1. Variable
                  // 2. Variable Predefinida
                  // 3. Valor Constante
                  
                  if (variableExists(name))
                  {
                     // Usa la misma instancia de VariableDeclaration en function.params
                     // que en el lugar donde se declaro la variable
                     varParams[keyval.key] = declaredVars[name]
                  }
                  else if (name.startsWith("\$"))
                  {
                     // Puede ser el nombre de una variable predefinida como
                     // $now, $today, $tomorrow, $yesterday, $ruleId
                     try
                     {
                        // FIXME: si ya hay una referencia a la variable predefinda,
                        //        no poner otra declaracion de la misma variable en
                        //        varParams.
                        
                        name = name[1..-1] // Saca el $
                        
                        // VariableDeclaration, los ValueInstance se resuelven en el
                        // RuleExecution.evaluate usando funcion resolutions inyectados
                        predefVarDec = PredefinedDeclarations."$name"()
                        varParams[keyval.key] = predefVarDec
                        
                        println "  Predefined Variable Declaration: " + predefVarDec + " " + predefVarDec.name
                        
                        // Crea funcion resolution para que resuelva el valor de la
                        // declaracion de cada variable predefinida
                        // TODO: si la variable ya tiene un resolutor, no agregarlo de nuevo
                        
                        preDefResolution = PredefinedResolutions."$name"(predefVarDec)
                        
                        println "  - Predefined Variable resolution: " + preDefResolution
                        
                        rule.addToResolutions( preDefResolution )
                     }
                     catch (Exception e)
                     {
                        println e.message
                        
                        // Puede haber un error si hay una variable que
                        // empieza con $ pero no tiene un nombre valido.
                        throw new Exception("La variable predefinda $name no existe, verifique su nombre o quite el prefijo \$")
                     }
                  }
                  else
                  {
                     // Si no existe la variable con ese nombre, el valor representa
                     // una constante de cierto tipo.
                     
                     // Notar que si hay un 3.14159 Groovy lo parsea a BigDecimal
                     // 1. se puede castear a float (el valor flotante puede cambiar
                     //    el decimal.
                     // 2. se puede agregar soporte para decimales como tipo basico.
                     
                     println "param type: "+ keyval.value.class.simpleName // BigDecimal, String, Integer, Float
                     
                     
                     // Genera un ConstantValue a partir de un valor Java, y asigna el DataType correcto.
                     varParams[keyval.key] = ConstantValue.fromValue(keyval.value)
                  }
               } // params.each
               
               resolution.params = varParams
            }
            
            println "  HTTP Resolution Declaration: " + vard + " " + vard.name
            
            rule.addToResolutions( resolution )
         }
         else if (xres.name() == "file")
         {
            // Nombre de la variable que resuelve la resolution
            String varName = xres.@var.text()
            VariableDeclaration vard = declaredVars[varName]
            
            // Si la variable no fue declarada previamente, error
            if (!vard)
            {
               throw new Exception("La variable "+ varName +" referenciada desde la resolution function no fue declarada previamente, verifique los nombres")
            }
            
            resolution = new FileVariableResolution(vard)
            
            resolution.locator = xres.locator.@path.text() + File.separator +
                                 xres.locator.@filename.text() + "." +
                                 xres.locator.@extension.text()
            
            resolution.extractor = xres.extractor.@path.text()
            
            if(!resolution.aggregator) // aggregator es opcional
            {
               resolution.aggregator = xres.aggregator.@type.text() // TODO: enum
               
               if (!xres.aggregator.@sort.isEmpty())
               {
                  resolution.sort = xres.aggregator.@sort.text() // TODO: enum
               }
            }
            
            println "  File Resolution Declaration: " + vard + " " + vard.name
            
            rule.addToResolutions( resolution )
         }
         else
         {
            println "resolution "+ xres.name() + " aun no soportado por el parser"
         }
      }
   }
   
   private static AbstractBlock parseAbstractBlock(GPathResult logic_item, RuleDefinition rule)
   {
      AbstractBlock block
      switch (logic_item.name())
      {
         case "if": // IfElse
         
           //println "parseLogic if"
           //lb = parseIfElse(logic."if", logic."else")
           block = parseIfElse(logic_item, logic_item.parent()."else", rule)
           
         break
         case "do": // LogicBlock
         
            block = parseLogicBlock(logic_item, rule)
         
         break
         default: // Function
            
            block = parseFunction(logic_item, null, rule)
      }
      
      return block
   }
   
   private static IfElse parseIfElse(GPathResult _if, GPathResult _else, RuleDefinition rule)
   {
      println "parseIfElse: "+
              _if.children()[0].name() +" "+
              _if.children().size() + " " + // 2 por la condicion y el do del if
              _else.children().size() // 1 por el do del else
      
      //BooleanFunction condition = parseBooleanFunction(_if.children()[0]) // La condicion es el primer subnodo
      
      BooleanFunction condition = parseFunction(_if.children()[0], "boolean", rule)
      
      LogicBlock ifBlock = parseAbstractBlock(_if.children()[1], rule) // puede tener otro if adentro o un actionblock
      
      LogicBlock elseBlock
      if (_else && !_else.isEmpty())// puede no haber else
      {
         elseBlock = parseAbstractBlock(_else.children()[0], rule) 
      }
      
      return new IfElse(condition, ifBlock, elseBlock)
   }
   
   /**
    * @param xfunct
    * @param returnType para funciones booleanas viene "boolean" para otras funcioes,
    *        se espera el tipo declarado en la regla, o tambien se puede implementar
    *        la derivacion del tipo usando los tipos de los parametros y la semantica
    *        de la funcion para adivinar el tipo.
    * @return
    */
   private static Function parseFunction(GPathResult xfunct, String returnType, RuleDefinition rule)
   {
      def functionClass = functionFactories[xfunct.name()]
      if (!functionClass)
      {
         throw new Exception("Function "+ xfunct.name() +" not supported")
      }
      
      // http://docs.oracle.com/javase/tutorial/reflect/member/ctorInstance.html
      // FIXME: Class.newInstance() can only invoke the zero-argument constructor
      //        entonces no me sirve para crear instnacias de funciones a no ser
      //        que defina un constructor sin argumentos y le setee los atributos
      //        con metodos.
      // Se puede usar Constructor.newInstance()
      // Para obtener el constructor correcto de una clase se usa getDeclaredConstructor
      // http://docs.oracle.com/javase/6/docs/api/java/lang/Class.html#getDeclaredConstructor%28java.lang.Class...%29
      // y se le pasan los tipos de los parametros para que sepa cual constructor elegir.
      
      Constructor ctor = functionClass.getDeclaredConstructor(String.class, DataType.class)
      
      // function returnType puede ser null, pero se asigna STRING por defecto, cuidado...
      DataType type
      if (returnType) type = DataType.get(returnType)
      else if (!xfunct.@type.isEmpty()) type = DataType.get(xfunct.@type.text())
      
      // Sino esta declarado, el DataType se adivina en la ejecucion
      // usando la clase del resultado.
      
      String returnName = xfunct.@return.text()
      
      
      /////////////////////////////////////////////////////////////////////////////////////////
      // sino hay una declaracion para la variable returnName, con returnName y type,
      // si estan presentes en la function, se debe crear una declaracion implicita de
      // variable, para que las funciones que se ejecutan luego de esta puedan hacer
      // referencia al nombre de la variable y que el nombre no se tome como una
      // constante string.
      /////////////////////////////////////////////////////////////////////////////////////////
      
      VariableDeclaration vd = this.declaredVars[returnName]
      if (!vd)
      {
         vd = new VariableDeclaration(returnName, type)
         this.declaredVars[returnName] = vd
      }
      else
      {
         // Si ya se ha declarado la variable con nombre returnName, se verifica que el tipo
         // de retorno de la function sea compatible con el tipo de la declaracions.
         // Si el type es null, no se pudo adivinar el tipo en el parse y depende de la ejecucion.
         // Esta misma verificacion debe ocurrir en la ejecucion tambien.
         // TODO: vd.type puede ser != type pero ser tipos compatibles, ej. type mas generico
         //       y vd.type (decimal) uno particular (int)
         if (type != null && vd.type != type)
         {
            throw new Exception("La funcion "+ xfunct.name() +" asigna un tipo de retorno ("+ type +") distinto al tipo ("+ vd.type +") de la variable declarada '"+ vd.name +"'")
         } 
      }
      
      
      // type puede ser null
      Function func = ctor.newInstance( returnName, type )
      
      
      
      // TODO: Soportar named params (Map), ahora se soporta ordered params (List), ver el parse de Http Resolutions que los params son Map.
      // Params: parsea el string groovy
      List params = []
      if (!xfunct.@params.isEmpty())
      {
         params = new GroovyShell().evaluate( xfunct.@params.text() ) // "[key1:value1,key2:value2]")
      }
      
      // FIXME: aqui los params deben ser parseados a ConstantValue o VariableDeclaration
      //        si es variable declaratios, se usa como puntero a una declaracion hecha
      //        previamente, tando en declarations como en returnName de una function
      
      List varParams = []
      String name
      VariableDeclaration predefVarDec // Auxiliar para variables predefinidas
      VariableResolution resolution // Auxiliar para variables predefinidas
      
      params.each { val -> // nombre de variable o valor constante
         
         // El valor puede ser el nombre de una variable o un valor constante
         // name se usa para verificar si es un nombre valido
         name = val.toString()
         
         // 1. Variable
         // 2. Variable Predefinida
         // 3. Valor Constante
         
         if (variableExists(name))
         {
            // Usa la misma instancia de VariableDeclaration en function.params
            // que en el lugar donde se declaro la variable
            varParams << declaredVars[name]
         }
         else if (name.startsWith("\$"))
         {
            // Puede ser el nombre de una variable predefinida como
            // $now, $today, $tomorrow, $yesterday, $ruleId
            try
            {
               // FIXME: si ya hay una referencia a la variable predefinda,
               //        no poner otra declaracion de la misma variable en
               //        varParams.
               
               name = name[1..-1] // Saca el $
               
               // VariableDeclaration, los ValueInstance se resuelven en el
               // RuleExecution.evaluate usando funcion resolutions inyectados
               predefVarDec = PredefinedDeclarations."$name"()
               varParams << predefVarDec
               
               println "  Predefined Variable Declaration: " + predefVarDec + " " + predefVarDec.name
               
               // Crea funcion resolution para que resuelva el valor de la
               // declaracion de cada variable predefinida
               // TODO: si la variable ya tiene un resolutor, no agregarlo de nuevo
               
               resolution = PredefinedResolutions."$name"(predefVarDec)
               
               println "  - Predefined Variable resolution: " + resolution
               
               rule.addToResolutions( resolution )
            }
            catch (Exception e)
            {
               // DEBUG
               println e.message
               
               // Puede haber un error si hay una variable que
               // empieza con $ pero no tiene un nombre valido.
               throw new Exception("La variable predefinda $name no existe, verifique su nombre o quite el prefijo \$")
            }
         }
         else
         {
            // Si no existe la variable con ese nombre, el valor representa
            // una constante de cierto tipo.
            
            // Notar que si hay un 3.14159 Groovy lo parsea a BigDecimal
            // 1. se puede castear a float (el valor flotante puede cambiar
            //    el decimal.
            // 2. se puede agregar soporte para decimales como tipo basico.
            
            println "param type: "+ val.class.simpleName // BigDecimal, String, Integer, Float
            
            // Genera un ConstantValue a partir de un valor Java, y asigna el DataType correcto. 
            varParams << ConstantValue.fromValue(val)
         }
      }
      
      func.setParams(varParams)
      
      println "params: "+ params
      
      
      // =========================================================
      // AND, OR, NOT
      if (xfunct.name() == "and" || xfunct.name() == "or")
      {
         List<BooleanFunction> fs = new ArrayList<BooleanFunction>()
         
         // Los hijos de AND/OR son funciones booleanas
         xfunct.children().each { subfunct ->
            
            fs.add( parseFunction(subfunct, "boolean", rule) )
         }
         
         func.setBooleanFunctions(fs)
      }
      else if (xfunct.name() == "not")
      {
         func.setBooleanFunction( parseFunction(xfunct.children()[0], "boolean", rule) )
      }
      else if (xfunct.name() == "xor")
      {
         func.setBooleanFunction( parseFunction(xfunct.children()[0], "boolean", rule) )
      }
      
      return func
   }
   
   /**
    * Devuelve true si la variable con ese nombre fue
    * declarada en declarations, input o como returnName
    * de una funcion previa.
    * @param varname
    * @return
    */
   private static boolean variableExists(String varname)
   {
      return declaredVars.containsKey(varname)
   }
   
   /* BooleanFunction se parsea en parseFunction tambien...
   private static BooleanFunction parseBooleanFunction(GPathResult boolfunc)
   {
      println "parseBooleanFunction: "+ boolfunc.name()
      
      def functionClass = functionFactories[boolfunc.name()]
      if (!functionClass)
      {
         throw new Exception("Function "+ boolfunc.name() +" not supported")
      }
      
      Constructor ctor = functionClass.getDeclaredConstructor(String.class, DataType.class)
      BooleanFunction func = ctor.newInstance(
         boolfunc.@return.text(),
         DataType.get("boolean")
      )
      
      // FIXME: puede no tener params
      // Params: parsea el string groovy
      List params = new GroovyShell().evaluate( boolfunc.@params.text() ) // "[key1:value1,key2:value2]")
      func.setParams(params)
      
      println "params: "+ params
      
      // FIXME: aqui los params deben ser parseados a ConstantValue o VariableDeclaration
      //        si es variable declaratios, se usa como puntero a una declaracion hecha
      //        previamente, tando en declarations como en returnName de una function
      
      return func
   }
   */
   
   private static LogicBlock parseLogicBlock(GPathResult xdo, RuleDefinition rule)
   {
      println "parseLogicBlock"
   
      List<AbstractBlock> blocks = []
      
      // Parsea los returns names
      List<String> returnNames = []
      
      // logic_item puede ser IfElse, returnName o una funcion.
      xdo.children().each { logic_item ->
         
         if (logic_item.name() == "return")
         {
            returnNames << logic_item.@name.text()
         }
         else if (logic_item.name() == "if")
         {
            // Ya se come el else si el if tenia un else luego
            blocks << parseIfElse(logic_item, logic_item.parent()."else", rule)
         }
         else if (logic_item.name() != "else") // Evita procesar else que estan luego del if (porque esta al mismo nivel que las funciones en el XML)
         {
            blocks << parseFunction(logic_item, null, rule)
         }
      }
   
      return new LogicBlock(blocks, returnNames)
   }
   
   private static VariableDeclaration parserVariableDeclaration(GPathResult var)
   {
      println "parserVariableDeclaration " + var.@name.text()

      DataType type = DataType.get(var.@type.text())
      
      if (var.@value.isEmpty())
      {
         return new VariableDeclaration(
            var.@name.text(),
            type,
            var.@namespace.text() // Puede ser vacio
         )
      }
      
      // Si viene value...
      
      // Si es una collection con valor, Groovy debe parsearla
      // TODO: soportar COLLECTION_OF_XXX
      if (type == DataType.COLLECTION)
      {
         // TODO: ver si es util que algunos valores de la coleccion puedan ser 
         //       nombres de variables o constantes previamente declaradas y resueltas.
         List value = new GroovyShell().evaluate( var.@value.text() )
         
         // Sino tiene nombre, se genera uno
         if (var.@name.isEmpty())
         {
            return new ConstantValue(
               type,
               var.@namespace.text(), // Puede ser vacio
               value
            )
         }
         return new ConstantValue(
            var.@name.text(),
            type,
            var.@namespace.text(), // Puede ser vacio
            value
         )
      }
      else
      {
         // Sino tiene nombre, se genera uno
         if (var.@name.isEmpty())
         {
            return new ConstantValue(
               type,
               var.@namespace.text(), // Puede ser vacio
               Caster.cast( var.@value.text(), type ) // Castea el string al tipo de la constante
            )
         }
         return new ConstantValue(
            var.@name.text(),
            type,
            var.@namespace.text(), // Puede ser vacio
            Caster.cast( var.@value.text(), type ) // Castea el string al tipo de la constante
         )
      }
   }
}