package com.cabolabs.xre.engine.resolution.file

import groovy.util.slurpersupport.GPathResult

// TODO: deberia ser todo sincronizado para que distintos threads puedan guardar y pedir info.
//
// TODO: permitir crear un observador de cambios en el filesystem sobre cada archivo para recargar su contenido.
//       - http://stackoverflow.com/questions/494869/file-changed-listener-in-java
//       - http://docs.oracle.com/javase/tutorial/essential/io/notification.html
//       - https://jamieechlin.atlassian.net/wiki/display/GRV/Listeners
//       - http://jnotify.sourceforge.net/
//       - http://twit88.com/blog/2007/10/02/develop-a-java-file-watcher/
//       - http://chamaras.blogspot.com/2011/04/how-to-write-java-code-to-listen.html
//
class FileCache {

   /**
    * Key es la path (String), value es contenido del archivo XML y es
    * groovy.util.slurpersupport.GPathResult que es lo
    * que parsea el XMLSlurper.
    * 
    * http://grepcode.com/file/repo1.maven.org/maven2/org.codehaus.groovy/groovy/2.0.5/org/codehaus/groovy/runtime/memoize/LRUCache.java
    */
   private Map cache = Collections.synchronizedMap([:])
   
   /**
    * Key es la path, value es el timer que va a sacar el valor del cache
    */
   private Map timers = new HashMap<String, Timer>()
   
   
   // Singleton
   private static FileCache instance = new FileCache()
   
   private FileCache() {}
   
   public static FileCache getInstance()
   {
      return instance
   }
   
   /**
    * Agrega request http y response xml al cache
    * @param req
    * @param res el XML recibido en HttpResolution
    */
   public void add(String path, GPathResult res)
   {
      this.cache[path] = res
      
      // Crea timer para dar de baja el response del cache
      // TODO: el timer deberia ser parametro del FileResolution y
      //       asignar un delay por defecto sino esta seteado en la regla.
      
      // Mantenimiento del cache on thread aparte,
      // hacer el reqres Synchronized.
      
      // Crear timer para el nuevo response
      //http://pleac.sourceforge.net/pleac_groovy/processmanagementetc.html
      def t = new Timer()
      
      t.runAfter(3500) { // TODO: delay configurable
         
         remove(path) // Aunque haya otras invocaciones a timers, req queda en el valor actual de la variable local 
      }
      
      // t.cancel() // Para apagar el timer si fuera necesario, ej. cancelarlo para reiniciarlo
   }
   

   public GPathResult get(String path)
   {
      //println "cache get " + this.reqres
      
      // TODO: deberia ser configurable que cada vez que se accede al response se resetee el timer o no.
      //       por ejemplo si la regla se basa en datos que no cambian, el delay puede ser muy grande y
      //       resetearse en caso de acceder de nuevo al response, asi se sigue alargando.
      
      return this.cache[path]
   }
   
   
   /**
    * Elimina el elemento chosenKey, se llama desde el thread del timer.
    * @param chosenKey
    */
   public void remove(String chosenKey) {
      
      // Necesitaria esto si hubieran mas operaciones pero como reqres es synchronizedMap las ops atomicas son thread safe.
      //synchronized (cache) {
         
         this.cache.remove(chosenKey)
         
      //}
   }
}