package values

/**
 * Los operadores usan esta clase para hacer referencias
 * a variables y parametros con nombre, cuyos valores se
 * necesitan para resolver la condicion booleana.
 * 
 * Esta clase tiene todos los valores necesarios para
 * conseguir el valor de la varaible o parametro correcto
 * desde el Registry.
 * 
 * @author pab
 *
 */
class NamedValueRef {

   /**
    * Nombre de la variable o parametro al que hace referencia.
    */
   String name
   
   /**
    * Identificador de la regla que contiene la variable o parametro.
    */
   String ruleId
   
   /**
    * Identificador de la unidad que contiene la regla ruleId
    */
   String unitId
   
   public NamedValueRef()
   {
   }
   
   public NamedValueRef(String unitId, String ruleId, String name)
   {
      this.unitId = unitId
      this.ruleId = ruleId
      this.name = name
   }
}