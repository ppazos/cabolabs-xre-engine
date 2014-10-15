package values

public enum ValueType {

   BOOLEAN(Boolean.class),
   INTEGER(Integer.class),
   REAL(Float.class),
   STRING(String.class),
   DATE(Date.class),
   COLLECTION(Collection.class) // es una interfaz, si quiero una clase concreta: HashSet o ArrayList (es la que usa Groovy)
   
   // TODO: otros tipos soportados?
   
   private ValueType(Class clazz) { this.clazz = clazz }
   
   // Para el parser
   // Factory
   static public ValueType get(String type)
   {
      switch (type)
      {
         case 'boolean':
            return BOOLEAN
         case 'integer':
            return INTEGER
         case 'float':
            return REAL
         case 'string':
            return STRING
         case 'date':
            return DATE
         case 'collection':
            return COLLECTION
         default:
            throw new Exception('el tipo "'+ type +'" no es soportado') // seria bueno decir el nombre del param para el que el type se declaro mal
      }
   }
   
   public final Class clazz
   //public Class clazz() { return clazz }
   
   public String toString()
   {
      switch (this)
      {
         case BOOLEAN:
            return 'boolean'
         case INTEGER:
            return 'integer'
         case REAL:
            return 'float'
         case STRING:
            return 'string'
         case DATE:
            return 'date'
         case COLLECTION:
            return 'collection'
      }
   }
}