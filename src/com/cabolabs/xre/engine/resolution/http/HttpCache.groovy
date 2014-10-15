package com.cabolabs.xre.engine.resolution.http

import groovy.util.slurpersupport.GPathResult

// TODO: deberia ser todo sincronizado para que distintos threads puedan guardar y pedir info.
class HttpCache {

   /**
    * Key es request (HttpRequestData), value es response y es
    * groovy.util.slurpersupport.GPathResult que es lo
    * que parsea el HttpBuilder en HttpResolution.
    * 
    * http://grepcode.com/file/repo1.maven.org/maven2/org.codehaus.groovy/groovy/2.0.5/org/codehaus/groovy/runtime/memoize/LRUCache.java
    */
   private Map reqres = Collections.synchronizedMap([:])
   
   /**
    * Key es el request, value es el timer que va a sacar el valor del cache
    */
   private Map timers = new HashMap<String, Timer>()
   
   // Singleton
   private static HttpCache instance = new HttpCache()
   
   private HttpCache() {}
   
   public static HttpCache getInstance()
   {
      return instance
   }
   
   /**
    * Agrega request http y response xml al cache
    * @param req
    * @param res el XML recibido en HttpResolution
    */
   //public void add(HttpRequestData req, NodeChild res)
   public void add(String req, GPathResult res)
   {
      this.reqres[req] = res
      
      // Crea timer para dar de baja el response del cache
      // TODO: el timer deberia ser parametro del HttpResolution y
      //       asignar un delay por defecto sino esta seteado en la regla.
      
      // Mantenimiento del cache on thread aparte,
      // hacer el reqres Synchronized.
      
      // Crear timer para el nuevo response
      //http://pleac.sourceforge.net/pleac_groovy/processmanagementetc.html
      def t = new Timer()
      
      t.runAfter(3500) { // TODO: delay configurable
         
         remove(req) // Aunque haya otras invocaciones a timers, req queda en el valor actual de la variable local 
      }
      
      // t.cancel() // Para apagar el timer si fuera necesario, ej. cancelarlo para reiniciarlo
   }
   
   //public NodeChild get(HttpRequestData req)
   public GPathResult get(String req)
   {
      //println "cache get " + this.reqres
      
      // TODO: deberia ser configurable que cada vez que se accede al response se resetee el timer o no.
      //       por ejemplo si la regla se basa en datos que no cambian, el delay puede ser muy grande y
      //       resetearse en caso de acceder de nuevo al response, asi se sigue alargando.
      
      return this.reqres[req]
   }
   
   
   /**
    * Elimina el elemento chosenKey, se llama desde el thread del timer.
    * @param chosenKey
    */
   public void remove(String chosenKey) {
      
      // Necesitaria esto si hubieran mas operaciones pero como reqres es synchronizedMap las ops atomicas son thread safe.
      //synchronized (cache) {
         
         this.reqres.remove(chosenKey)
         
      //}
   }
}