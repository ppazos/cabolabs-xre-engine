package values

class Variable {

   /**
    * Nombre de la variable o parametro.
    */
   String name
   
   /**
    * Valor de la varaible o parametro.
    * Para la variable, el valor se establece cuando se resuelve la variable.
    * Para el parametro, el valor se establece al crear la instancia.
    */
   Object value
   
   /**
    * El tipo se usa para castear los valores de variables resueltos
    * usando servicios web. El problema es que siempre se obtiene un
    * valor String y el valor contra el cual se quiere comparar puede
    * no se String, entonces hay que transformar el String en el mismo
    * tipo del valor contra el cual se quiere comparar.
    */
   ValueType type = ValueType.STRING // por defecto es string
   
}