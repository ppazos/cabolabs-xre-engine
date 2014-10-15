package com.cabolabs.xre.engine.resolution.function

import java.util.List

import com.cabolabs.xre.core.definitions.VariableDeclaration;
import com.cabolabs.xre.core.execution.VariableInstance;
import com.cabolabs.xre.core.logic.Function
import com.cabolabs.xre.core.resolution.VariableResolution;

class FunctionVariableResolution extends VariableResolution {

   /**
    * TODO: se podria generalizar a un action block con UN valor de retorno.
    */
   Function resolutor
   

   public FunctionVariableResolution(VariableDeclaration declaration)
   {
      super(declaration)
   }
   
   @Override
   public VariableInstance getValue(String sessionId, List<VariableInstance> params)
   {
      if (resolutor == null)
      {
         this.errors.add( new Exception("No se ha especificado la funcion para resolver la variable "+ this.declaration.name) )
         return null // El retorno null marca que hay error
      }
      
      List<VariableInstance> res = resolutor.execute(sessionId)
      
      res[0].declaration.name = this.declaration.name
      
      /*
      println ""
      println "function resolution res name: " + res[0].declaration.name
      println "function resolution res type: " + res[0].declaration.type
      println "function resolution res value: " + res[0].value
      */
      
      return res[0]
   }
   
   public void setFunction(Function resolutor)
   {
      this.resolutor = resolutor;
   }
   
}