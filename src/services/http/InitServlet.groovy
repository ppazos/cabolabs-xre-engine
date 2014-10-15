package services.http

import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler

// http://wiki.eclipse.org/Jetty/Tutorial/Embedding_Jetty
class InitServlet extends HttpServlet
{
   private String greeting="Hello World";
   
   public InitServlet(){}
   public InitServlet(String greeting)
   {
      this.greeting=greeting;
   }
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
   {
      println request.parameterMap // nombre -> lista de valores
      
      response.setContentType("text/html");
      response.setStatus(HttpServletResponse.SC_OK);
      response.getWriter().println("<h1>"+greeting+"</h1>");
      response.getWriter().println("session=" + request.getSession(true).getId());
      
      // TODO: necesito algo que cargue las reglas en el registry (e.g. comandos desde consola interactiva en otro thread)
      
      //def reg = Registry.instance
      //def rule = reg.getRule(unitId, ruleId) // TODO: sacar de params
   }
}