package net.liftweb.http

/*                                                *\
  (c) 2007 WorldWide Conferencing, LLC
  Distributed under an Apache License
  http://www.apache.org/licenses/LICENSE-2.0
\*                                                 */
  
import scala.collection.immutable.TreeMap
import javax.servlet.http.{HttpServlet, HttpServletRequest , HttpServletResponse, HttpSession}

/**
  * The base trait of Controllers that handle pre-view requests
  */
trait SimpleController {
  var request: RequestState = _
  // private var session: TreeMap[String, Any] = _
  var httpRequest: HttpServletRequest = _
                          
  def params(name: String): Option[String] = {
    request.params.get(name) match {
      case None => None
      case Some(nl) => nl.take(1) match {
        case Nil => None
        case l => Some(l.head)
      }
    }
  }
  
  def post_? : boolean = request.post_?
  
  /*
  def apply(name: String): Option[Any] = {
    session.get(name)
  }*/
  
  def apply[T](name: String): Option[T] = {
    if (httpRequest == null) None
    else {
      httpRequest.getSession.getAttribute(name) match {
      case null => None
      case n => {
        Console.println("Got n "+n)
        if (n.isInstanceOf[T]) Some(n.asInstanceOf[T])
        else None
      }
    }
    }
  }
  
  def update(name: String, value: Any) {
    value match {
      case null => httpRequest.getSession.removeAttribute(name)
      case _ => {Console.println("Setting attribute "+name+" "+value);  httpRequest.getSession.setAttribute(name, value)}
    }
  }
}